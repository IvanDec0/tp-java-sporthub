package com.java.sportshub.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Payment extends Generic {

  @ManyToOne
  @JoinColumn(name = "cart_id")
  private Cart cart;

  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(nullable = false)
  private Double amount;

  @Column(name = "payment_method")
  private String paymentMethod; // Credit Card, Debit Card, Cash, Transfer

  @Column(name = "payment_status")
  private String paymentStatus; // Pending, Completed, Failed, Refunded


  @Column(name = "order_status")
  private String orderStatus; // "Pendiente", "En preparación", "Listo para retirar", "Entregado"

  @Column(name = "transaction_id")
  private String transactionId;

  @Column(name = "stripe_payment_intent_id")
  private String stripePaymentIntentId;

  @Column(name = "stripe_charge_id")
  private String stripeChargeId;

  @Column(name = "stripe_refund_id")
  private String stripeRefundId;

  @Column(name = "payment_date")
  private LocalDateTime paymentDate;

  @Column(name = "notes")
  private String notes;

  @Column(name = "applied_coupons", length = 1000)
  private String appliedCoupons; // JSON or comma-separated list of applied coupon codes and amounts
}
