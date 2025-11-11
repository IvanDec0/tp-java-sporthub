package com.java.sportshub.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CouponDAO;
import com.java.sportshub.exceptions.AttributeExistsException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Coupon;

@Service
public class CouponService {

    @Autowired
    private CouponDAO couponDAO;

    public List<Coupon> getAllCoupons() {
        return couponDAO.findAll();
    }

    public Coupon getCouponById(Long id) {
        return couponDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "id", id));
    }

    @Transactional
    public Coupon createCoupon(Coupon coupon) {
        validateCoupon(coupon);

        if (couponDAO.existsByCode(coupon.getCode())) {
            throw new AttributeExistsException("Coupon", "code", coupon.getCode());
        }

        return couponDAO.save(coupon);
    }

    @Transactional
    public Coupon updateCoupon(Long id, Coupon couponDetails) {
        Coupon coupon = getCouponById(id);

        if (couponDetails.getCode() != null) {
            if (couponDAO.existsByCode(couponDetails.getCode()) && !coupon.getCode().equals(couponDetails.getCode())) {
                throw new AttributeExistsException("Coupon", "code", couponDetails.getCode());
            }
            coupon.setCode(couponDetails.getCode());
        }

        if (couponDetails.getDescription() != null) {
            coupon.setDescription(couponDetails.getDescription());
        }

        if (couponDetails.getDiscountPercent() != null && couponDetails.getDiscountPercent() > 0) {
            coupon.setDiscountPercent(couponDetails.getDiscountPercent());
        }

        if (couponDetails.getExpiryDate() != null) {
            coupon.setExpiryDate(couponDetails.getExpiryDate());
        }

        return couponDAO.save(coupon);
    }

    @Transactional
    public void deleteCoupon(Long id) {
        Coupon coupon = getCouponById(id);
        coupon.setIsActive(false);
        couponDAO.save(coupon);
    }

    private void validateCoupon(Coupon coupon) {
        if (coupon.getCode() == null || coupon.getCode().trim().isEmpty()) {
            throw new ValidationException("code", "Code is required");
        }

        boolean hasPercent = coupon.getDiscountPercent() != null && coupon.getDiscountPercent() > 0;

        if (!hasPercent) {
            throw new ValidationException("discount", "discountPercent must be set and greater than 0");
        }
    }
}
