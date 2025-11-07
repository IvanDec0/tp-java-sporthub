package com.java.sportshub.daos;

import com.java.sportshub.models.Store;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreDAO extends JpaRepository<Store, Long> {

  boolean existsByName(String name);

  boolean existsByEmail(String email);
}
