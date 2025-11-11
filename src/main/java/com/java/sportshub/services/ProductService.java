package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.java.sportshub.daos.CouponDAO;
import com.java.sportshub.daos.ProductDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Coupon;
import com.java.sportshub.models.Product;

@Service
public class ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CouponDAO couponDAO;

    @Autowired
    private CloudflareR2Service cloudflareR2Service;

    public List<Product> getAllProducts() {
        return productDAO.findAll();
    }

    public Product getProductById(Long id) {
        return productDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", id));
    }

    @Transactional
    public Product createProduct(Product product) {
        validateProduct(product);

        if (productDAO.existsByName(product.getName())) {
            throw new AttributeExistsException("Product", "name", product.getName());
        }

        return productDAO.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);

        if (productDetails.getName() != null) {
            // Verificar si el nuevo nombre ya existe en otro producto
            if (productDAO.existsByName(productDetails.getName()) &&
                    !product.getName().equals(productDetails.getName())) {
                throw new AttributeExistsException("Product", "name", productDetails.getName());
            }
            product.setName(productDetails.getName());
        }

        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }

        if (productDetails.getPrice() > 0) {
            product.setPrice(productDetails.getPrice());
        }

        if (productDetails.getCategories() != null) {
            product.setCategories(productDetails.getCategories());
        }

        if (productDetails.getCoupons() != null) {
            product.setCoupons(productDetails.getCoupons());
        }

        return productDAO.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        product.setIsActive(false);
        productDAO.save(product);
    }

    @Transactional
    public Product attachCoupons(Long productId, List<Long> couponIds) {
        Product product = getProductById(productId);

        List<Coupon> coupons = couponDAO.findAllById(couponIds);
        product.setCoupons(coupons);
        return productDAO.save(product);
    }

    @Transactional
    public Product detachCoupon(Long productId, Long couponId) {
        Product product = getProductById(productId);
        if (product.getCoupons() != null) {
            product.getCoupons().removeIf(c -> c.getId().equals(couponId));
        }
        return productDAO.save(product);
    }

    @Transactional
    public Product uploadProductImage(Long productId, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new ValidationException("image", "Image file is required");
        }

        if (image.getSize() > 10 * 1024 * 1024) {
            throw new ValidationException("image", "Image file must be less than 10MB");
        }

        if (!image.getContentType().startsWith("image/")) {
            throw new ValidationException("image", "Image file must be an image");
        }

        if (!image.getContentType().equals("image/jpeg") && !image.getContentType().equals("image/png")
                && !image.getContentType().equals("image/gif") && !image.getContentType().equals("image/webp")) {
            throw new ValidationException("image", "Image file must be a JPEG, PNG, GIF, or WEBP image");
        }

        Product product = getProductById(productId);

        String existingImageKey = product.getImageKey();
        if (existingImageKey != null && !existingImageKey.isBlank()) {
            cloudflareR2Service.deleteObject(existingImageKey);
        }

        String objectKey = cloudflareR2Service.uploadProductImage(productId, image);
        product.setImageKey(objectKey);
        product.setImageUrl(cloudflareR2Service.resolvePublicUrl(objectKey));

        return productDAO.save(product);
    }

    @Transactional
    public Product deleteProductImage(Long productId) {
        Product product = getProductById(productId);

        String existingImageKey = product.getImageKey();
        if (existingImageKey == null || existingImageKey.isBlank()) {
            throw new ResourceNotFoundException("Product Image", "productId", productId);
        }

        cloudflareR2Service.deleteObject(existingImageKey);
        product.setImageKey(null);
        product.setImageUrl(null);

        return productDAO.save(product);
    }

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
        if (product.getPrice() <= 0) {
            throw new ValidationException("price", "Price must be greater than 0");
        }
    }
}
