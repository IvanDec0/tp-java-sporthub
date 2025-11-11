package com.java.sportshub.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.java.sportshub.dtos.ProductDTO;
import com.java.sportshub.mappers.ProductMapper;
import com.java.sportshub.models.Product;
import com.java.sportshub.services.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        List<ProductDTO> products = productService.getAllProducts()
                .stream()
                .map(ProductMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return ResponseEntity.ok(ProductMapper.toDTO(product));
    }

    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@RequestBody Product product) {
        Product createdProduct = productService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(ProductMapper.toDTO(createdProduct));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @RequestBody Product productDetails) {
        Product updatedProduct = productService.updateProduct(id, productDetails);
        return ResponseEntity.ok(ProductMapper.toDTO(updatedProduct));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/coupons")
    public ResponseEntity<ProductDTO> attachCoupons(@PathVariable("id") Long id, @RequestBody List<Long> couponIds) {
        Product updated = productService.attachCoupons(id, couponIds);
        return ResponseEntity.ok(ProductMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}/coupons/{couponId}")
    public ResponseEntity<ProductDTO> detachCoupon(@PathVariable("id") Long id,
            @PathVariable("couponId") Long couponId) {
        Product updated = productService.detachCoupon(id, couponId);
        return ResponseEntity.ok(ProductMapper.toDTO(updated));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ProductDTO> uploadProductImage(
            @PathVariable("id") Long id,
            @RequestParam("file") MultipartFile image) {
        Product updated = productService.uploadProductImage(id, image);
        return ResponseEntity.ok(ProductMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}/image")
    public ResponseEntity<ProductDTO> deleteProductImage(@PathVariable("id") Long id) {
        Product updated = productService.deleteProductImage(id);
        return ResponseEntity.ok(ProductMapper.toDTO(updated));
    }
}
