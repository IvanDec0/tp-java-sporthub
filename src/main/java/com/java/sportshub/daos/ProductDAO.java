package com.java.sportshub.daos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.java.sportshub.models.Product;

import java.util.List;

@Repository
public interface ProductDAO extends JpaRepository<Product, Long> {

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Product p SET p.isActive = false WHERE p.id = ?1")
    void deleteById(Long id);

    @Override
    @Query("SELECT p FROM Product p WHERE p.isActive = true")
    List<Product> findAll();
}
