package com.e_commerce.e_commerce.service;


import com.e_commerce.e_commerce.dto.request.ProductRequest;
import com.e_commerce.e_commerce.dto.response.CategoryResponse;
import com.e_commerce.e_commerce.dto.response.ProductResponse;
import com.e_commerce.e_commerce.dto.response.UserProductResponse;
import com.e_commerce.e_commerce.exception.BadRequestException;
import com.e_commerce.e_commerce.exception.ResourceNotFoundException;
import com.e_commerce.e_commerce.model.Category;
import com.e_commerce.e_commerce.model.Product;
import com.e_commerce.e_commerce.repository.CategoryRepository;
import com.e_commerce.e_commerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final ImageStorageService imageStorageService;

    // Admin operations
    public ProductResponse createProduct(ProductRequest request) {
        log.info("Creating new product with SKU: {}", request.getSku());

        if (productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU already exists: " + request.getSku());
        }

        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setQuantity(request.getQuantity());
        product.setBrand(request.getBrand());

        // Add categories
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            categories.forEach(product::addCategory);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Product created successfully with ID: {}", savedProduct.getProductId());

        return mapToProductResponse(savedProduct);
    }

    public ProductResponse updateProduct(Long id, ProductRequest request) {
        log.info("Updating product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        // Check if SKU is being changed and if new SKU already exists
        if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
            throw new BadRequestException("Product with SKU already exists: " + request.getSku());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setSku(request.getSku());
        product.setQuantity(request.getQuantity());
        product.setBrand(request.getBrand());

        // Update categories
        product.clearCategories();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            Set<Category> categories = categoryRepository.findAllById(request.getCategoryIds())
                    .stream().collect(Collectors.toSet());
            categories.forEach(product::addCategory);
        }

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated successfully with ID: {}", updatedProduct.getProductId());

        return mapToProductResponse(updatedProduct);
    }

    public void deleteProduct(Long id) {
        log.info("Deleting product with ID: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        product.setStatus("INACTIVE");
        productRepository.save(product);

        log.info("Product marked as inactive with ID: {}", id);
    }

    public ProductResponse uploadProductImages(Long productId, MultipartFile mainImage, MultipartFile[] additionalImages) throws IOException {
        log.info("Uploading images for product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        // Upload main image
        if (mainImage != null && !mainImage.isEmpty()) {
            String mainImageUrl = imageStorageService.storeImage(mainImage, product.getSku());
            product.setMainImageUrl(mainImageUrl);
        }

        // Upload additional images
        if (additionalImages != null) {
            List<String> imageUrls = new ArrayList<>();
            for (MultipartFile image : additionalImages) {
                if (!image.isEmpty()) {
                    String imageUrl = imageStorageService.storeImage(image, product.getSku());
                    imageUrls.add(imageUrl);
                }
            }
            product.setAdditionalImages(imageUrls);
        }

        Product savedProduct = productRepository.save(product);
        log.info("Images uploaded successfully for product ID: {}", productId);

        return mapToProductResponse(savedProduct);
    }

    // Add this method to ProductService
    public void deleteMainProductImage(Long productId) {
        log.info("Deleting main image for product ID: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        if (product.getMainImageUrl() != null) {
            try {
                imageStorageService.deleteImage(product.getMainImageUrl());
                product.setMainImageUrl(null);
                productRepository.save(product);
                log.info("Main image deleted successfully for product ID: {}", productId);
            } catch (IOException e) {
                log.error("Failed to delete main image for product ID: {}", productId, e);
                throw new RuntimeException("Failed to delete main image", e);
            }
        }
    }

    public void deleteProductImage(Long productId, Integer imageIndex) {
        log.info("Deleting image at index {} for product ID: {}", imageIndex, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + productId));

        List<String> images = product.getAdditionalImages();
        if (imageIndex >= 0 && imageIndex < images.size()) {
            String imageUrl = images.get(imageIndex);
            try {
                imageStorageService.deleteImage(imageUrl);
                images.remove(imageIndex.intValue());
                product.setAdditionalImages(images);
                productRepository.save(product);
                log.info("Image deleted successfully at index {} for product ID: {}", imageIndex, productId);
            } catch (IOException e) {
                log.error("Failed to delete image for product ID: {}", productId, e);
                throw new RuntimeException("Failed to delete image", e);
            }
        } else {
            throw new BadRequestException("Invalid image index: " + imageIndex);
        }
    }

    // Read operations
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatus("ACTIVE", pageable)
                .map(this::mapToProductResponse);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));
        return mapToProductResponse(product);
    }

    @Transactional(readOnly = true)
    public UserProductResponse getUserProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with ID: " + id));

        if (!"ACTIVE".equals(product.getStatus())) {
            throw new ResourceNotFoundException("Product not available");
        }

        return mapToUserProductResponse(product);
    }

    // Search operations
    @Transactional(readOnly = true)
    public Page<UserProductResponse> searchProducts(String searchTerm, Pageable pageable) {
        return productRepository.searchActiveProducts(searchTerm, pageable)
                .map(this::mapToUserProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProductResponse> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoriesCategoryId(categoryId, pageable)
                .map(this::mapToUserProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProductResponse> searchProductsInCategory(Long categoryId, String searchTerm, Pageable pageable) {
        return productRepository.searchProductsInCategory(categoryId, searchTerm, pageable)
                .map(this::mapToUserProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProductResponse> filterProducts(Long categoryId, String searchTerm, String brand,
                                                    BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findProductsWithFilters(categoryId, searchTerm, brand, minPrice, maxPrice, pageable)
                .map(this::mapToUserProductResponse);
    }

    @Transactional(readOnly = true)
    public Page<UserProductResponse> getAvailableProducts(Pageable pageable) {
        return productRepository.findAvailableProducts(pageable)
                .map(this::mapToUserProductResponse);
    }

    // Mapping methods
    private ProductResponse mapToProductResponse(Product product) {
        Set<CategoryResponse> categoryResponses = product.getCategories()
                .stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toSet());

        return new ProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getQuantity(),
                product.getStatus(),
                product.getBrand(),
                product.getMainImageUrl(),
                product.getAdditionalImages(),
                categoryResponses,
                product.getCreatedDate(),
                product.getUpdatedDate()
        );
    }

    private UserProductResponse mapToUserProductResponse(Product product) {
        Set<CategoryResponse> categoryResponses = product.getCategories()
                .stream()
                .map(this::mapToCategoryResponse)
                .collect(Collectors.toSet());

        return new UserProductResponse(
                product.getProductId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getSku(),
                product.getBrand(),
                product.getMainImageUrl(),
                product.getAdditionalImages(),
                categoryResponses,
                product.getQuantity() > 0
        );
    }

    private CategoryResponse mapToCategoryResponse(Category category) {
        return new CategoryResponse(
                category.getCategoryId(),
                category.getName(),
                category.getDescription(),
                category.getParentId(),
                new ArrayList<>(),
                0L,
                category.getCreatedDate()
        );
    }
}
