package com.java.sportshub.daos;

import java.util.List;

import com.java.sportshub.models.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.java.sportshub.models.Inventory;

@Repository
public interface InventoryDAO extends JpaRepository<Inventory, Long> {

  @Query("SELECT i FROM Inventory i WHERE i.store.id = ?1 AND i.isActive = true")
  List<Inventory> findByStoreId(Long storeId);

  @Query("SELECT i FROM Inventory i WHERE i.product.id = ?1 AND i.isActive = true")
  List<Inventory> findByProductId(Long productId);

  @Query("SELECT i FROM Inventory i WHERE i.tipo = ?1 AND i.isActive = true")
  List<Inventory> findByTipo(String tipo);

  @Query("SELECT i FROM Inventory i WHERE i.quantity > 0 AND i.isActive = true")
  List<Inventory> findAvailableInventory();

  @Override
  @Query("SELECT i FROM Inventory i WHERE i.isActive = true")
  List<Inventory> findAll();

}
