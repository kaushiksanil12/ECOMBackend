package com.e_commerce.e_commerce.repository;


import com.e_commerce.e_commerce.enums.OrderStatus;
import com.e_commerce.e_commerce.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    // User orders
    List<Order> findByUserUserIdOrderByCreatedDateDesc(Long userId);

    Page<Order> findByUserUserIdOrderByCreatedDateDesc(Long userId, Pageable pageable);

    // Guest orders by email
    List<Order> findByCustomerEmailOrderByCreatedDateDesc(String email);

    Page<Order> findByCustomerEmailOrderByCreatedDateDesc(String email, Pageable pageable);

    // Orders by status
    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    // Orders by date range
    @Query("SELECT o FROM Order o WHERE o.createdDate BETWEEN :startDate AND :endDate ORDER BY o.createdDate DESC")
    List<Order> findByDateRange(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    // Admin queries
    Page<Order> findAllByOrderByCreatedDateDesc(Pageable pageable);

    @Query("SELECT COUNT(o) FROM Order o WHERE o.status = :status")
    long countByStatus(@Param("status") OrderStatus status);

    @Query("SELECT SUM(o.totalAmount) FROM Order o WHERE o.status = 'DELIVERED' AND o.createdDate >= :startDate")
    BigDecimal getTotalRevenueFromDate(@Param("startDate") LocalDateTime startDate);

    boolean existsByOrderNumber(String orderNumber);
}
