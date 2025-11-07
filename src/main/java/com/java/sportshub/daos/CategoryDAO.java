package com.java.sportshub.daos;

import com.java.sportshub.models.Category;
import com.java.sportshub.models.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryDAO extends JpaRepository<Category, Long> {

    boolean existsByName(String name);

    @Modifying
    @Query("UPDATE Category c SET c.isActive = false WHERE c.id = ?1")
    void deleteById(long id);

}
