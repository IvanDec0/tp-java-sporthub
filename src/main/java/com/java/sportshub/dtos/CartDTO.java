package com.java.sportshub.dtos;

import java.util.List;
import java.util.stream.Collectors;

import com.java.sportshub.models.Cart;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartDTO {
  private Long id;
  private String status;
  private Double totalAmount;
  private List<CartItemDTO> items;
  private Long userId;
  private Long storeId;
  private UserDTO user;
  private StoreDTO store;

  public CartDTO(Cart cart) {
    this.id = cart.getId();
    this.userId = cart.getUser() != null ? cart.getUser().getId() : null;
    this.storeId = cart.getStore() != null ? cart.getStore().getId() : null;
    this.user = cart.getUser() != null ? new UserDTO(cart.getUser()) : null;
    this.store = cart.getStore() != null ? new StoreDTO(cart.getStore()) : null;
    this.status = cart.getStatus();
    this.totalAmount = cart.getTotalAmount();

    if (cart.getItems() != null) {
      this.items = cart.getItems().stream()
          .map(CartItemDTO::new)
          .collect(Collectors.toList());
    }
  }
}
