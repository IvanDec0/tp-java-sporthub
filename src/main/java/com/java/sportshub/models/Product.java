package com.java.sportshub.models;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import lombok.Getter;
import lombok.Setter;

import com.java.sportshub.models.Coupon;

@Entity
@Getter
@Setter
public class Product extends Generic {
    private String name;
    private String description;
    private double price;

    @ManyToMany
    @JoinTable(name = "product_category", joinColumns = @JoinColumn(name = "product_Id"), inverseJoinColumns = @JoinColumn(name = "category_Id"))
    private List<Category> categories;

    @ManyToMany
    @JoinTable(name = "product_coupon", joinColumns = @JoinColumn(name = "product_id"), inverseJoinColumns = @JoinColumn(name = "coupon_id"))
    private List<Coupon> coupons;
}
