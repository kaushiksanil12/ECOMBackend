package com.e_commerce.e_commerce.service;


import com.e_commerce.e_commerce.dto.request.CategoryRequest;
import com.e_commerce.e_commerce.dto.response.CategoryResponse;
import com.e_commerce.e_commerce.exception.BadRequestException;
import com.e_commerce.e_commerce.exception.ResourceNotFoundException;
import com.e_commerce.e_commerce.model.Category;
import com.e_commerce.e_commerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Create operations
    public CategoryResponse createCategory(CategoryRequest request) {
        log.info("Creating new category: {}", request.getName());

        if (categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        }

        // Validate parent category exists if parentId is provided
        if (request.getParentId() != null) {
            categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        Category savedCategory = categoryRepository.save(category);
        log.info("Category created successfully with ID: {}", savedCategory.getCategoryId());

        return mapToResponse(savedCategory);
    }

    // Read operations
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        log.info("Fetching all categories");
        return categoryRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getRootCategories() {
        log.info("Fetching root categories");
        return categoryRepository.findByParentIdIsNull()
                .stream()
                .map(this::mapToResponseWithSubCategories)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        log.info("Fetching category with ID: {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));
        return mapToResponseWithSubCategories(category);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryByName(String name) {
        log.info("Fetching category with name: {}", name);
        Category category = categoryRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with name: " + name));
        return mapToResponseWithSubCategories(category);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getSubCategories(Long parentId) {
        log.info("Fetching subcategories for parent ID: {}", parentId);

        // Validate parent category exists
        categoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + parentId));

        return categoryRepository.findByParentId(parentId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> searchCategories(String searchTerm) {
        log.info("Searching categories with term: {}", searchTerm);
        return categoryRepository.searchByName(searchTerm)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoryHierarchy(Long categoryId) {
        log.info("Fetching category hierarchy for ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        List<CategoryResponse> hierarchy = new ArrayList<>();
        buildHierarchy(category, hierarchy);

        return hierarchy;
    }

    // Update operations
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        log.info("Updating category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(request.getName()) && categoryRepository.existsByName(request.getName())) {
            throw new BadRequestException("Category already exists with name: " + request.getName());
        }

        // Validate parent category exists if parentId is provided and different from current
        if (request.getParentId() != null && !request.getParentId().equals(category.getParentId())) {
            categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found with ID: " + request.getParentId()));

            // Prevent circular reference - category cannot be its own parent or descendant
            if (isCircularReference(id, request.getParentId())) {
                throw new BadRequestException("Circular reference detected. Category cannot be moved under its own descendant.");
            }
        }

        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setParentId(request.getParentId());

        Category updatedCategory = categoryRepository.save(category);
        log.info("Category updated successfully with ID: {}", updatedCategory.getCategoryId());

        return mapToResponse(updatedCategory);
    }

    // Delete operations
    public void deleteCategory(Long id) {
        log.info("Deleting category with ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if category has subcategories
        List<Category> subCategories = categoryRepository.findByParentId(id);
        if (!subCategories.isEmpty()) {
            throw new BadRequestException("Cannot delete category with subcategories. Please delete or move subcategories first.");
        }

        // Check if category has products
        if (!category.getProducts().isEmpty()) {
            throw new BadRequestException("Cannot delete category with associated products. Please remove products from this category first.");
        }

        categoryRepository.delete(category);
        log.info("Category deleted successfully with ID: {}", id);
    }

    public void deleteCategoryWithSubcategories(Long id) {
        log.info("Deleting category with subcategories, ID: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + id));

        // Check if any category in the hierarchy has products
        if (hasProductsInHierarchy(category)) {
            throw new BadRequestException("Cannot delete category hierarchy with associated products. Please remove all products first.");
        }

        // Delete all subcategories recursively
        deleteSubcategoriesRecursively(category);

        // Delete the category itself
        categoryRepository.delete(category);
        log.info("Category and subcategories deleted successfully with ID: {}", id);
    }

    // Utility methods
    public List<CategoryResponse> getCategoryPath(Long categoryId) {
        log.info("Getting category path for ID: {}", categoryId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        List<CategoryResponse> path = new ArrayList<>();
        buildCategoryPath(category, path);

        // Reverse to get path from root to current category
        java.util.Collections.reverse(path);

        return path;
    }

    @Transactional(readOnly = true)
    public long getCategoryProductCount(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + categoryId));

        return category.getProducts().size();
    }

    // Private helper methods
    private void buildHierarchy(Category category, List<CategoryResponse> hierarchy) {
        hierarchy.add(mapToResponse(category));

        List<Category> subcategories = categoryRepository.findByParentId(category.getCategoryId());
        for (Category subcategory : subcategories) {
            buildHierarchy(subcategory, hierarchy);
        }
    }

    private void buildCategoryPath(Category category, List<CategoryResponse> path) {
        path.add(mapToResponse(category));

        if (category.getParentId() != null) {
            Category parent = categoryRepository.findById(category.getParentId())
                    .orElse(null);
            if (parent != null) {
                buildCategoryPath(parent, path);
            }
        }
    }

    private boolean isCircularReference(Long categoryId, Long parentId) {
        if (categoryId.equals(parentId)) {
            return true;
        }

        Category parent = categoryRepository.findById(parentId).orElse(null);
        if (parent == null || parent.getParentId() == null) {
            return false;
        }

        return isCircularReference(categoryId, parent.getParentId());
    }

    private boolean hasProductsInHierarchy(Category category) {
        if (!category.getProducts().isEmpty()) {
            return true;
        }

        List<Category> subcategories = categoryRepository.findByParentId(category.getCategoryId());
        for (Category subcategory : subcategories) {
            if (hasProductsInHierarchy(subcategory)) {
                return true;
            }
        }

        return false;
    }

    private void deleteSubcategoriesRecursively(Category category) {
        List<Category> subcategories = categoryRepository.findByParentId(category.getCategoryId());

        for (Category subcategory : subcategories) {
            deleteSubcategoriesRecursively(subcategory);
            categoryRepository.delete(subcategory);
        }
    }

    // Mapping methods
    private CategoryResponse mapToResponse(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                new ArrayList<>(),
                (long) category.getProducts().size(),
                category.getCreatedDate()
        );
    }

    private CategoryResponse mapToResponseWithSubCategories(Category category) {
        List<CategoryResponse> subCategories = categoryRepository
                .findByParentId(category.getCategoryId())
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                subCategories,
                (long) category.getProducts().size(),
                category.getCreatedDate()
        );
    }
}
