package com.java.sportshub.dtos;

import com.java.sportshub.models.Coupon;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;

@Getter
@Setter
@NoArgsConstructor
public class CouponDTO {
    private Long id;
    private String code;
    private String description;
    private Double discountPercent;
    private Date expiryDate;

    public CouponDTO(Coupon coupon) {
        if (coupon == null) return;
        this.id = coupon.getId();
        this.code = coupon.getCode();
        this.description = coupon.getDescription();
        this.discountPercent = coupon.getDiscountPercent();
        this.expiryDate = coupon.getExpiryDate();
    }
}
