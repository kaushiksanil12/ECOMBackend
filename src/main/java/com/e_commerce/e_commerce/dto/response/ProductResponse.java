package com.e_commerce.e_commerce.dto.response;

import com.e_commerce.e_commerce.dto.response.CategoryResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String sku;
    private Integer quantity;
    private String status;
    private String brand;
    private String mainImageUrl;
    private List<String> additionalImages;
    private Set<CategoryResponse> categories;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
