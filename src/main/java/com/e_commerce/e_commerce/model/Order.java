package com.e_commerce.e_commerce.model;

import com.e_commerce.e_commerce.enums.OrderStatus;
import com.e_commerce.e_commerce.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ORDERS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "order_seq")
    @SequenceGenerator(name = "order_seq", sequenceName = "ORDER_SEQ", allocationSize = 1)
    @Column(name = "ORDER_ID")
    private Long orderId;

    @Column(name = "ORDER_NUMBER", unique = true, nullable = false)
    private String orderNumber;

    // Customer information (for guest orders, this stores guest info)
    @Column(name = "CUSTOMER_EMAIL", nullable = false)
    private String customerEmail;

    @Column(name = "CUSTOMER_FIRST_NAME", nullable = false)
    private String customerFirstName;

    @Column(name = "CUSTOMER_LAST_NAME", nullable = false)
    private String customerLastName;

    @Column(name = "CUSTOMER_PHONE")
    private String customerPhone;

    // For registered users (nullable for guest orders)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private User user;

    // Shipping address
    @Column(name = "SHIPPING_ADDRESS", nullable = false)
    private String shippingAddress;

    @Column(name = "SHIPPING_CITY", nullable = false)
    private String shippingCity;

    @Column(name = "SHIPPING_STATE", nullable = false)
    private String shippingState;

    @Column(name = "SHIPPING_ZIP_CODE", nullable = false)
    private String shippingZipCode;

    @Column(name = "SHIPPING_COUNTRY", nullable = false)
    private String shippingCountry;

    // Order details
    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private OrderStatus status = OrderStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "PAYMENT_METHOD", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(name = "SUBTOTAL", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "TAX_AMOUNT", precision = 10, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "SHIPPING_COST", precision = 10, scale = 2)
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "TOTAL_AMOUNT", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "SPECIAL_INSTRUCTIONS")
    private String specialInstructions;

    // Order items
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems = new ArrayList<>();

    // Shipment (optional)
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL)
    private Shipment shipment;

    @Column(name = "CREATED_DATE")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    @UpdateTimestamp
    private LocalDateTime updatedDate;

    // Helper methods
    public void addOrderItem(OrderItem orderItem) {
        orderItems.add(orderItem);
        orderItem.setOrder(this);
    }

    public void removeOrderItem(OrderItem orderItem) {
        orderItems.remove(orderItem);
        orderItem.setOrder(null);
    }

    public boolean isGuestOrder() {
        return user == null;
    }
}
