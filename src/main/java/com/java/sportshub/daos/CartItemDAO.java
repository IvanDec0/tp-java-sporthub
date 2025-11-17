package com.java.sportshub.daos;

import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CartItemDAO extends JpaRepository<CartItem, Long> {

  List<CartItem> findByCart(Cart cart);

  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1")
  List<CartItem> findByCartId(Long cartId);

  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = ?1 AND ci.isActive = true")
  List<CartItem> findActiveItemsByCartId(Long cartId);

  @Override
  @Query("SELECT c FROM CartItem c WHERE c.isActive = true")
  List<CartItem> findAll();
}
