package com.java.sportshub.dtos;

import java.time.LocalDate;

import com.java.sportshub.models.CartItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItemDTO {
  private Long id;
  private Integer quantity;
  private LocalDate startDate;
  private LocalDate estimatedEndDate;
  private LocalDate endDate;
  private Double subtotal;
  private Long cartId;
  private Long inventoryId;
  private InventoryDTO inventory;
  private CartDTO cart;

  public CartItemDTO(CartItem cartItem) {
    this.id = cartItem.getId();
    this.cartId = cartItem.getCart() != null ? cartItem.getCart().getId() : null;
    this.inventoryId = cartItem.getInventory() != null ? cartItem.getInventory().getId() : null;
    this.cart = cartItem.getCart() != null ? new CartDTO(cartItem.getCart()) : null;
    this.inventory = cartItem.getInventory() != null ? new InventoryDTO(cartItem.getInventory()) : null;
    this.quantity = cartItem.getQuantity();
    this.startDate = cartItem.getStartDate();
    this.estimatedEndDate = cartItem.getEstimatedEndDate();
    this.endDate = cartItem.getEndDate();
    this.subtotal = cartItem.getSubtotal();
  }
}
