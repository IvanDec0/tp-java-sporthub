package com.java.sportshub.dtos;

import java.time.LocalDateTime;

import com.java.sportshub.models.Payment;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PaymentDTO {
  private Long id;
  private Double amount;
  private String paymentMethod;
  private String paymentStatus;
  private String transactionId;
  private LocalDateTime paymentDate;
  private String notes;
  private Long cartId;
  private Long userId;
  private CartDTO cart;
  private UserDTO user;

  public PaymentDTO(Payment payment) {
    this.id = payment.getId();
    this.cartId = payment.getCart() != null ? payment.getCart().getId() : null;
    this.userId = payment.getUser() != null ? payment.getUser().getId() : null;
    this.cart = payment.getCart() != null ? new CartDTO(payment.getCart()) : null;
    this.user = payment.getUser() != null ? new UserDTO(payment.getUser()) : null;
    this.amount = payment.getAmount();
    this.paymentMethod = payment.getPaymentMethod();
    this.paymentStatus = payment.getPaymentStatus();
    this.transactionId = payment.getTransactionId();
    this.paymentDate = payment.getPaymentDate();
    this.notes = payment.getNotes();
  }
}
