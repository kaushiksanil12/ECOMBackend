package com.e_commerce.e_commerce.repository;

import com.e_commerce.e_commerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Basic queries
    Optional<Product> findBySku(String sku);

    List<Product> findByStatus(String status);

    Page<Product> findByStatus(String status, Pageable pageable);

    boolean existsBySku(String sku);

    // Search queries
    List<Product> findByNameContainingIgnoreCase(String name);

    Page<Product> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Product> searchProducts(@Param("searchTerm") String searchTerm);

    @Query("SELECT p FROM Product p WHERE (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND p.status = 'ACTIVE'")
    Page<Product> searchActiveProducts(@Param("searchTerm") String searchTerm, Pageable pageable);

    // Category-based queries
    List<Product> findByCategoriesCategoryId(Long categoryId);

    Page<Product> findByCategoriesCategoryId(Long categoryId, Pageable pageable);

    List<Product> findByCategoriesNameIgnoreCase(String categoryName);

    // ✅ Fixed: Added Pageable parameter
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    Page<Product> searchProductsInCategory(@Param("categoryId") Long categoryId, @Param("searchTerm") String searchTerm, Pageable pageable);

    // ✅ Added: Non-pageable version for backward compatibility
    @Query("SELECT p FROM Product p JOIN p.categories c WHERE c.categoryId = :categoryId AND (LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%')))")
    List<Product> searchProductsInCategory(@Param("categoryId") Long categoryId, @Param("searchTerm") String searchTerm);

    // Price range queries
    List<Product> findByPriceBetweenAndStatus(BigDecimal minPrice, BigDecimal maxPrice, String status);

    Page<Product> findByPriceBetweenAndStatus(BigDecimal minPrice, BigDecimal maxPrice, String status, Pageable pageable);

    // Brand queries
    List<Product> findByBrandIgnoreCaseAndStatus(String brand, String status);

    Page<Product> findByBrandIgnoreCaseAndStatus(String brand, String status, Pageable pageable);

    // Availability queries
    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.status = 'ACTIVE'")
    List<Product> findAvailableProducts();

    @Query("SELECT p FROM Product p WHERE p.quantity > 0 AND p.status = 'ACTIVE'")
    Page<Product> findAvailableProducts(Pageable pageable);

    // Advanced search with multiple filters
    @Query("SELECT DISTINCT p FROM Product p LEFT JOIN p.categories c WHERE " +
            "(:categoryId IS NULL OR c.categoryId = :categoryId) AND " +
            "(:searchTerm IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) AND " +
            "(:brand IS NULL OR LOWER(p.brand) = LOWER(:brand)) AND " +
            "(:minPrice IS NULL OR p.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR p.price <= :maxPrice) AND " +
            "p.status = 'ACTIVE' AND p.quantity > 0")
    Page<Product> findProductsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("searchTerm") String searchTerm,
            @Param("brand") String brand,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}
