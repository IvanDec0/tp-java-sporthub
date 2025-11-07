package com.java.sportshub.models;

import jakarta.persistence.Entity;
import java.sql.Date;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Coupon extends Generic {
    private String code;
    private String description;
    private Double discountPercent;
    private Date expiryDate;
}
