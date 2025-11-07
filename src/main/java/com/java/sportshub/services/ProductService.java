package com.java.sportshub.services;

import java.util.List;

import com.java.sportshub.models.Coupon;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.ProductDAO;
import com.java.sportshub.daos.CouponDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Product;

@Service
public class ProductService {

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private CouponDAO couponDAO;

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

    private void validateProduct(Product product) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            throw new ValidationException("name", "Name is required");
        }
        if (product.getPrice() <= 0) {
            throw new ValidationException("price", "Price must be greater than 0");
        }
    }
}
