package com.e_commerce.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemTrackingResponse {
    private String productName;
    private String productSku;
    private Integer quantity;
    private String status;
}