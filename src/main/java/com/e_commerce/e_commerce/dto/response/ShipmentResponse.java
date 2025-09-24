package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.enums.ShipmentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipmentResponse {
    private Long shipmentId;
    private String trackingNumber;
    private ShipmentStatus status;
    private String carrier;
    private LocalDateTime shippedDate;
    private LocalDateTime deliveredDate;
    private LocalDateTime estimatedDelivery;
    private LocalDateTime createdDate;
}
