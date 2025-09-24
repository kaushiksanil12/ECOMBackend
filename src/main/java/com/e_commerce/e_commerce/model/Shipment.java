package com.e_commerce.e_commerce.model;


import com.e_commerce.e_commerce.enums.ShipmentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "SHIPMENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "shipment_seq")
    @SequenceGenerator(name = "shipment_seq", sequenceName = "SHIPMENT_SEQ", allocationSize = 1)
    @Column(name = "SHIPMENT_ID")
    private Long shipmentId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ORDER_ID", nullable = false)
    private Order order;

    @Column(name = "TRACKING_NUMBER", unique = true)
    private String trackingNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false)
    private ShipmentStatus status = ShipmentStatus.PREPARING;

    @Column(name = "CARRIER")
    private String carrier;

    @Column(name = "SHIPPED_DATE")
    private LocalDateTime shippedDate;

    @Column(name = "DELIVERED_DATE")
    private LocalDateTime deliveredDate;

    @Column(name = "ESTIMATED_DELIVERY")
    private LocalDateTime estimatedDelivery;

    @Column(name = "CREATED_DATE")
    @CreationTimestamp
    private LocalDateTime createdDate;

    @Column(name = "UPDATED_DATE")
    @UpdateTimestamp
    private LocalDateTime updatedDate;
}
