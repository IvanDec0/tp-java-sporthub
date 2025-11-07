package com.java.sportshub.mappers;

import com.java.sportshub.dtos.CartDTO;
import com.java.sportshub.models.Cart;

public class CartMapper {

  public static CartDTO toDTO(Cart cart) {
    return new CartDTO(cart);
  }

  public static Cart toEntity(CartDTO dto) {
    Cart cart = new Cart();
    cart.setStatus(dto.getStatus());
    cart.setTotalAmount(dto.getTotalAmount());
    return cart;
  }

  public static void updateEntity(Cart cart, CartDTO dto) {
    if (dto.getStatus() != null) {
      cart.setStatus(dto.getStatus());
    }
    if (dto.getTotalAmount() != null) {
      cart.setTotalAmount(dto.getTotalAmount());
    }
  }
}
