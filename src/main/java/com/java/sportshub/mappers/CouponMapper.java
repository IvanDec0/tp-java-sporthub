package com.java.sportshub.mappers;

import com.java.sportshub.dtos.CouponDTO;
import com.java.sportshub.models.Coupon;

public class CouponMapper {

    public static CouponDTO toDTO(Coupon coupon) {
        return new CouponDTO(coupon);
    }

    public static Coupon toEntity(CouponDTO dto) {
        Coupon coupon = new Coupon();
        coupon.setCode(dto.getCode());
        coupon.setDescription(dto.getDescription());
        coupon.setDiscountPercent(dto.getDiscountPercent());
        coupon.setExpiryDate(dto.getExpiryDate());
        return coupon;
    }

    public static void updateEntity(Coupon coupon, CouponDTO dto) {
        if (dto.getCode() != null) coupon.setCode(dto.getCode());
        if (dto.getDescription() != null) coupon.setDescription(dto.getDescription());
        if (dto.getDiscountPercent() != null) coupon.setDiscountPercent(dto.getDiscountPercent());
        if (dto.getExpiryDate() != null) coupon.setExpiryDate(dto.getExpiryDate());
    }
}
