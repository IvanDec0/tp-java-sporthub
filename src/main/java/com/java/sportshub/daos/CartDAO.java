package com.java.sportshub.daos;

import com.java.sportshub.models.Cart;
import com.java.sportshub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartDAO extends JpaRepository<Cart, Long> {

  List<Cart> findByUser(User user);

  @Query("SELECT c FROM Cart c WHERE c.user.id = ?1")
  List<Cart> findByUserId(Long userId);

  @Query("SELECT c FROM Cart c WHERE c.user.id = ?1 AND c.status = 'Active' AND c.isActive = true")
  Optional<Cart> findActiveCartByUserId(Long userId);

  @Query("SELECT c FROM Cart c WHERE c.status = ?1")
  List<Cart> findByStatus(String status);
}
