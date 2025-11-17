package com.java.sportshub.daos;

import com.java.sportshub.models.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewDAO extends JpaRepository<Review, Long> {

  List<Review> findByProduct(Product product);

  List<Review> findByStore(Store store);

  List<Review> findByUser(User user);

  @Query("SELECT r FROM Review r WHERE r.product.id = ?1 AND r.isActive = true")
  List<Review> findActiveReviewsByProductId(Long productId);

  @Query("SELECT r FROM Review r WHERE r.store.id = ?1 AND r.isActive = true")
  List<Review> findActiveReviewsByStoreId(Long storeId);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.product.id = ?1 AND r.isActive = true")
  Double getAverageRatingByProductId(Long productId);

  @Query("SELECT AVG(r.rating) FROM Review r WHERE r.store.id = ?1 AND r.isActive = true")
  Double getAverageRatingByStoreId(Long storeId);

  @Override
  @Query("SELECT r FROM Review r WHERE r.isActive = true")
  List<Review> findAll();
}
