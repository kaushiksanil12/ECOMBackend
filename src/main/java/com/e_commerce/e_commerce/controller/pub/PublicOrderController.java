package com.e_commerce.e_commerce.controller.pub;

import com.e_commerce.e_commerce.dto.request.GuestOrderRequest;
import com.e_commerce.e_commerce.dto.response.GuestOrderTrackingResponse;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/public/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicOrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createGuestOrder(@Valid @RequestBody GuestOrderRequest request) {
        OrderResponse response = orderService.createGuestOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/track")
    public ResponseEntity<GuestOrderTrackingResponse> trackOrder(
            @RequestParam String orderNumber,
            @RequestParam String email) {

        GuestOrderTrackingResponse response = orderService.trackOrderByNumberAndEmail(orderNumber, email);
        return ResponseEntity.ok(response);
    }
}
