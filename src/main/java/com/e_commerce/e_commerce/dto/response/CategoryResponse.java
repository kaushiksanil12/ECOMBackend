package com.e_commerce.e_commerce.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long categoryId;
    private String name;
    private String description;
    private Long parentId;
    private List<CategoryResponse> subCategories;
    private Long productCount;
    private LocalDateTime createdDate;
}
