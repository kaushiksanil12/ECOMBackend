package com.e_commerce.e_commerce.controller.user;


import com.e_commerce.e_commerce.dto.request.UserOrderRequest;
import com.e_commerce.e_commerce.dto.response.OrderResponse;
import com.e_commerce.e_commerce.security.UserPrincipal;
import com.e_commerce.e_commerce.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/orders")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('USER', 'ADMIN')")
public class UserOrderController {

    private final OrderService orderService;


    @PostMapping
    public ResponseEntity<OrderResponse> createUserOrder(
            @Valid @RequestBody UserOrderRequest request,
            Authentication auth) {
        Long userId = getCurrentUserId(auth);
        OrderResponse response = orderService.createUserOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> getUserOrders(Authentication auth, Pageable pageable) {
        Long userId = getCurrentUserId(auth);
        Page<OrderResponse> orders = orderService.getUserOrders(userId, pageable);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getUserOrder(@PathVariable Long orderId, Authentication auth) {
        Long userId = getCurrentUserId(auth);
        OrderResponse order = orderService.getUserOrder(orderId, userId);
        return ResponseEntity.ok(order);
    }

    private Long getCurrentUserId(Authentication auth) {
        UserPrincipal principal = (UserPrincipal) auth.getPrincipal();
        return principal.getId();
    }
}
