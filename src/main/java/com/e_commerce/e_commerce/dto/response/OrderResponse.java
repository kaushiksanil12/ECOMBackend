package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.enums.OrderStatus;
import com.e_commerce.e_commerce.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private String customerEmail;
    private String customerFirstName;
    private String customerLastName;
    private String customerPhone;
    private Long userId; // null for guest orders
    private OrderStatus status;
    private PaymentMethod paymentMethod;

    // Shipping address
    private String shippingAddress;
    private String shippingCity;
    private String shippingState;
    private String shippingZipCode;
    private String shippingCountry;

    // Pricing
    private BigDecimal subtotal;
    private BigDecimal taxAmount;
    private BigDecimal shippingCost;
    private BigDecimal totalAmount;

    private String specialInstructions;
    private List<OrderItemResponse> orderItems;
    private ShipmentResponse shipment;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
