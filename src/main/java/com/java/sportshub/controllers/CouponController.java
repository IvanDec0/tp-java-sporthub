package com.java.sportshub.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.CouponDTO;
import com.java.sportshub.mappers.CouponMapper;
import com.java.sportshub.middlewares.RequiredRoles;
import com.java.sportshub.models.Coupon;
import com.java.sportshub.services.CouponService;

@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Autowired
    private CouponService couponService;

    @GetMapping
    public ResponseEntity<List<CouponDTO>> getAll() {
        List<CouponDTO> list = couponService.getAllCoupons()
                .stream()
                .map(CouponMapper::toDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CouponDTO> getById(@PathVariable Long id) {
        Coupon coupon = couponService.getCouponById(id);
        return ResponseEntity.ok(CouponMapper.toDTO(coupon));
    }

    @PostMapping
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<CouponDTO> create(@RequestBody Coupon coupon) {
        Coupon created = couponService.createCoupon(coupon);
        return ResponseEntity.status(HttpStatus.CREATED).body(CouponMapper.toDTO(created));
    }

    @PutMapping("/{id}")
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<CouponDTO> update(@PathVariable Long id, @RequestBody Coupon couponDetails) {
        Coupon updated = couponService.updateCoupon(id, couponDetails);
        return ResponseEntity.ok(CouponMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    @RequiredRoles({ "ADMIN" })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        couponService.deleteCoupon(id);
        return ResponseEntity.noContent().build();
    }
}
