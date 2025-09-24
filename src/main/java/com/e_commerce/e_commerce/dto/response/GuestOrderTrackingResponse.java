package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GuestOrderTrackingResponse {
    private String orderNumber;
    private OrderStatus status;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    private String shippingCity;
    private String shippingState;
    private String shippingCountry;
    private LocalDateTime estimatedDelivery;
    private List<OrderItemTrackingResponse> items;

    // Note: No personal details, payment info, or full address for security
}
