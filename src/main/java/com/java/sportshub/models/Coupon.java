package com.java.sportshub.models;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Coupon extends Generic {
    @Column(name = "code")
    private String code;

    @Column(name = "description")
    private String description;

    @Column(name = "discount_percent")
    private Double discountPercent;

    @Column(name = "expiry_date")
    private Date expiryDate;
}
