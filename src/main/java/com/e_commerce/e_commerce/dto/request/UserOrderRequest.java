package com.e_commerce.e_commerce.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserOrderRequest {

    // Optional shipping address override
    private String shippingAddress;

    @NotBlank(message = "City is required")
    private String shippingCity;

    @NotBlank(message = "State is required")
    private String shippingState;

    @NotBlank(message = "ZIP code is required")
    private String shippingZipCode;

    @NotBlank(message = "Country is required")
    private String shippingCountry;

    // Order items
    @NotEmpty(message = "Order items cannot be empty")
    private List<OrderItemRequest> items;

    // Payment information
    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String specialInstructions;
}
