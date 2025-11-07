package com.java.sportshub.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
public class CartItem extends Generic {

  @ManyToOne
  @JoinColumn(name = "cart_id")
  private Cart cart;

  @ManyToOne
  @JoinColumn(name = "inventory_id")
  private Inventory inventory;

  @Column(nullable = false)
  private Integer quantity;

  // Fechas para el alquiler de equipos
  @Column(name = "start_date")
  private LocalDate startDate;

  @Column(name = "estimated_end_date")
  private LocalDate estimatedEndDate;

  @Column(name = "end_date")
  private LocalDate endDate;

  // Precio calculado según tipo (venta o alquiler) y duración
  @Column(name = "subtotal")
  private Double subtotal;
}
