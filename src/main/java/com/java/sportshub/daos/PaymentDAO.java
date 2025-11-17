package com.java.sportshub.daos;

import com.java.sportshub.models.Category;
import com.java.sportshub.models.Payment;
import com.java.sportshub.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentDAO extends JpaRepository<Payment, Long> {

  List<Payment> findByUser(User user);

  @Query("SELECT p FROM Payment p WHERE p.user.id = ?1")
  List<Payment> findByUserId(Long userId);

  @Query("SELECT p FROM Payment p WHERE p.paymentStatus = ?1")
  List<Payment> findByPaymentStatus(String status);

  @Query("SELECT p FROM Payment p WHERE p.cart.id = ?1")
  Payment findByCartId(Long cartId);

  @Override
  @Query("SELECT p FROM Payment p WHERE p.isActive = true")
  List<Payment> findAll();

}
