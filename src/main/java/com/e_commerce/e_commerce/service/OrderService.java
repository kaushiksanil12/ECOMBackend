package com.e_commerce.e_commerce.service;


import com.e_commerce.e_commerce.dto.request.GuestOrderRequest;
import com.e_commerce.e_commerce.dto.request.OrderItemRequest;
import com.e_commerce.e_commerce.dto.request.UserOrderRequest;
import com.e_commerce.e_commerce.dto.response.*;
import com.e_commerce.e_commerce.enums.OrderStatus;
import com.e_commerce.e_commerce.enums.PaymentMethod;
import com.e_commerce.e_commerce.exception.BadRequestException;
import com.e_commerce.e_commerce.exception.ResourceNotFoundException;
import com.e_commerce.e_commerce.model.*;
import com.e_commerce.e_commerce.repository.OrderRepository;
import com.e_commerce.e_commerce.repository.ProductRepository;
import com.e_commerce.e_commerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;


    // Add this method to OrderService.java
    public OrderResponse createUserOrder(Long userId, UserOrderRequest request) {
        log.info("Creating user order for user ID: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setUser(user); // ✅ Set the user relationship
        order.setCustomerEmail(user.getEmail());
        order.setCustomerFirstName(user.getFirstName());
        order.setCustomerLastName(user.getLastName());
        order.setCustomerPhone(user.getPhone());

        // Set shipping address (from request or user profile)
        order.setShippingAddress(request.getShippingAddress() != null ?
                request.getShippingAddress() : user.getAddress());
        order.setShippingCity(request.getShippingCity());
        order.setShippingState(request.getShippingState());
        order.setShippingZipCode(request.getShippingZipCode());
        order.setShippingCountry(request.getShippingCountry());

        // Set payment method
        order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Process order items
        processOrderItems(order, request.getItems());

        Order savedOrder = orderRepository.save(order);
        log.info("User order created successfully: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }


    // Guest order creation
    public OrderResponse createGuestOrder(GuestOrderRequest request) {
        log.info("Creating guest order for email: {}", request.getEmail());

        Order order = new Order();
        order.setOrderNumber(generateOrderNumber());
        order.setCustomerEmail(request.getEmail());
        order.setCustomerFirstName(request.getFirstName());
        order.setCustomerLastName(request.getLastName());
        order.setCustomerPhone(request.getPhone());

        // Set shipping address
        order.setShippingAddress(request.getAddress());
        order.setShippingCity(request.getCity());
        order.setShippingState(request.getState());
        order.setShippingZipCode(request.getZipCode());
        order.setShippingCountry(request.getCountry());

        // Set payment method
        order.setPaymentMethod(PaymentMethod.valueOf(request.getPaymentMethod()));
        order.setSpecialInstructions(request.getSpecialInstructions());

        // Process order items
        processOrderItems(order, request.getItems());

        Order savedOrder = orderRepository.save(order);
        log.info("Guest order created successfully: {}", savedOrder.getOrderNumber());

        return mapToOrderResponse(savedOrder);
    }

    // ✅ Guest tracking with email verification
    @Transactional(readOnly = true)
    public GuestOrderTrackingResponse trackOrderByNumberAndEmail(String orderNumber, String email) {
        log.info("Guest tracking order: {} for email: {}", orderNumber, email);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        // Verify email matches (security check)
        if (!order.getCustomerEmail().equalsIgnoreCase(email.trim())) {
            throw new BadRequestException("Order not found for the provided email");
        }

        // Return limited info for guests
        return mapToGuestTrackingResponse(order);
    }

    // ✅ Full order details for authenticated users
    @Transactional(readOnly = true)
    public OrderResponse getUserOrder(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        // Verify user owns this order (security check)
        if (order.getUser() == null || !order.getUser().getUserId().equals(userId)) {
            throw new BadRequestException("You don't have permission to view this order");
        }

        return mapToOrderResponse(order);
    }

    // Order tracking (legacy method - keeping for compatibility)
    @Transactional(readOnly = true)
    public OrderResponse trackOrderByNumber(String orderNumber) {
        log.info("Tracking order: {}", orderNumber);

        Order order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found: " + orderNumber));

        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        return mapToOrderResponse(order);
    }

    // User's orders
    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(Long userId, Pageable pageable) {
        return orderRepository.findByUserUserIdOrderByCreatedDateDesc(userId, pageable)
                .map(this::mapToOrderResponse);
    }

    // Admin operations
    @Transactional(readOnly = true)
    public Page<OrderResponse> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedDateDesc(pageable)
                .map(this::mapToOrderResponse);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getOrdersByStatus(OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable)
                .map(this::mapToOrderResponse);
    }

    public OrderResponse updateOrderStatus(Long orderId, OrderStatus newStatus) {
        log.info("Updating order {} status to {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated successfully: {} -> {}", orderId, newStatus);

        return mapToOrderResponse(updatedOrder);
    }

    public void cancelOrder(Long orderId) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found with ID: " + orderId));

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel order that has been shipped or delivered");
        }

        order.setStatus(OrderStatus.CANCELLED);

        // Restore product quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantity(product.getQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        orderRepository.save(order);
        log.info("Order cancelled successfully: {}", orderId);
    }

    // Private helper methods
    private void processOrderItems(Order order, List<OrderItemRequest> items) {
        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemRequest : items) {
            // ✅ Validate quantity is positive
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() <= 0) {
                throw new BadRequestException("Quantity must be greater than 0 for product ID: " + itemRequest.getProductId());
            }

            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + itemRequest.getProductId()));
            // Check stock availability
            if (product.getQuantity() < itemRequest.getQuantity()) {
                throw new BadRequestException("Insufficient stock for product: " + product.getName() +
                        ". Available: " + product.getQuantity() + ", Requested: " + itemRequest.getQuantity());
            }

            // Create order item
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(product);
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(product.getPrice());
            orderItem.setTotalPrice(product.getPrice().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));

            // Product snapshot
            orderItem.setProductName(product.getName());
            orderItem.setProductSku(product.getSku());
            orderItem.setProductImageUrl(product.getMainImageUrl());

            order.addOrderItem(orderItem);

            // Update product quantity
            product.setQuantity(product.getQuantity() - itemRequest.getQuantity());
            productRepository.save(product);

            subtotal = subtotal.add(orderItem.getTotalPrice());
        }

        // ✅ Calculate totals with proper rounding
        BigDecimal taxRate = BigDecimal.valueOf(0.10); // 10% tax
        BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal shippingCost = subtotal.compareTo(BigDecimal.valueOf(50)) >= 0 ?
                BigDecimal.ZERO : BigDecimal.valueOf(10.00);

        order.setSubtotal(subtotal);
        order.setTaxAmount(taxAmount);
        order.setShippingCost(shippingCost);

        // ✅ Total with proper rounding
        BigDecimal totalAmount = subtotal.add(taxAmount).add(shippingCost);
        order.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));
    }


    private String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String orderNumber;
        do {
            orderNumber = "ORD" + timestamp + String.format("%03d", (int)(Math.random() * 999));
        } while (orderRepository.existsByOrderNumber(orderNumber));

        return orderNumber;
    }

    // ✅ Guest tracking mapping method
    private GuestOrderTrackingResponse mapToGuestTrackingResponse(Order order) {
        List<OrderItemTrackingResponse> itemResponses = order.getOrderItems().stream()
                .map(this::mapToOrderItemTrackingResponse)
                .collect(Collectors.toList());

        // Calculate estimated delivery (7 days from order date as example)
        LocalDateTime estimatedDelivery = order.getCreatedDate() != null ?
                order.getCreatedDate().plusDays(7) : null;

        return new GuestOrderTrackingResponse(
                order.getOrderNumber(),
                order.getStatus(),
                order.getCreatedDate(),
                order.getTotalAmount(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingCountry(),
                estimatedDelivery,
                itemResponses
        );
    }

    // ✅ Guest tracking item mapping method
    private OrderItemTrackingResponse mapToOrderItemTrackingResponse(OrderItem orderItem) {
        return new OrderItemTrackingResponse(
                orderItem.getProductName(),
                orderItem.getProductSku(),
                orderItem.getQuantity(),
                "Processing" // You can make this dynamic based on order status
        );
    }

    private OrderResponse mapToOrderResponse(Order order) {
        List<OrderItemResponse> orderItems = order.getOrderItems().stream()
                .map(this::mapToOrderItemResponse)
                .collect(Collectors.toList());

        ShipmentResponse shipmentResponse = order.getShipment() != null ?
                mapToShipmentResponse(order.getShipment()) : null;

        return new OrderResponse(
                order.getOrderId(),
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getCustomerFirstName(),
                order.getCustomerLastName(),
                order.getCustomerPhone(),
                order.getUser() != null ? order.getUser().getUserId() : null,
                order.getStatus(),
                order.getPaymentMethod(),
                order.getShippingAddress(),
                order.getShippingCity(),
                order.getShippingState(),
                order.getShippingZipCode(),
                order.getShippingCountry(),
                order.getSubtotal(),
                order.getTaxAmount(),
                order.getShippingCost(),
                order.getTotalAmount(),
                order.getSpecialInstructions(),
                orderItems,
                shipmentResponse,
                order.getCreatedDate(),
                order.getUpdatedDate()
        );
    }

    private OrderItemResponse mapToOrderItemResponse(OrderItem orderItem) {
        return new OrderItemResponse(
                orderItem.getOrderItemId(),
                orderItem.getProduct().getProductId(),
                orderItem.getProductName(),
                orderItem.getProductSku(),
                orderItem.getProductImageUrl(),
                orderItem.getQuantity(),
                orderItem.getUnitPrice(),
                orderItem.getTotalPrice()
        );
    }

    private ShipmentResponse mapToShipmentResponse(Shipment shipment) {
        return new ShipmentResponse(
                shipment.getShipmentId(),
                shipment.getTrackingNumber(),
                shipment.getStatus(),
                shipment.getCarrier(),
                shipment.getShippedDate(),
                shipment.getDeliveredDate(),
                shipment.getEstimatedDelivery(),
                shipment.getCreatedDate()
        );
    }
}
