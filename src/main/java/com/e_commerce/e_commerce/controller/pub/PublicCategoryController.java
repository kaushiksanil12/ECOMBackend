package com.e_commerce.e_commerce.controller.pub;

import com.e_commerce.e_commerce.dto.response.CategoryResponse;
import com.e_commerce.e_commerce.service.CategoryService;
import org.springframework.web.bind.annotation.RequestMapping;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/public/categories")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PublicCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponse>> getBrowseCategories() {
        List<CategoryResponse> categories = categoryService.getRootCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategory(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ResponseEntity.ok(category);
    }

    @GetMapping("/{id}/subcategories")
    public ResponseEntity<List<CategoryResponse>> getSubCategories(@PathVariable Long id) {
        List<CategoryResponse> subCategories = categoryService.getSubCategories(id);
        return ResponseEntity.ok(subCategories);
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<CategoryResponse>> getCategoryPath(@PathVariable Long id) {
        List<CategoryResponse> path = categoryService.getCategoryPath(id);
        return ResponseEntity.ok(path);
    }

    @GetMapping("/search")
    public ResponseEntity<List<CategoryResponse>> searchCategories(@RequestParam String q) {
        List<CategoryResponse> categories = categoryService.searchCategories(q);
        return ResponseEntity.ok(categories);
    }
}
