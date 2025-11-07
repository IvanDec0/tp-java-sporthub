package com.java.sportshub.daos;

import com.java.sportshub.models.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CouponDAO extends JpaRepository<Coupon, Long> {

    boolean existsByCode(String code);

    Optional<Coupon> findByCode(String code);

    @Modifying
    @Query("UPDATE Coupon c SET c.isActive = false WHERE c.id = ?1")
    void deleteById(long id);
}
