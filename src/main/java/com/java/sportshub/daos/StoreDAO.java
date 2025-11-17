package com.java.sportshub.daos;

import com.java.sportshub.models.Role;
import com.java.sportshub.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StoreDAO extends JpaRepository<Store, Long> {

  boolean existsByName(String name);

  boolean existsByEmail(String email);

  @Override
  @Query("SELECT s FROM Store s WHERE s.isActive = true")
  List<Store> findAll();
}
