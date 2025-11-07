package com.java.sportshub.mappers;

import com.java.sportshub.dtos.CartItemDTO;
import com.java.sportshub.models.CartItem;

public class CartItemMapper {

  public static CartItemDTO toDTO(CartItem cartItem) {
    return new CartItemDTO(cartItem);
  }

  public static CartItem toEntity(CartItemDTO dto) {
    CartItem cartItem = new CartItem();
    cartItem.setQuantity(dto.getQuantity());
    cartItem.setStartDate(dto.getStartDate());
    cartItem.setEstimatedEndDate(dto.getEstimatedEndDate());
    cartItem.setEndDate(dto.getEndDate());
    cartItem.setSubtotal(dto.getSubtotal());
    return cartItem;
  }

  public static void updateEntity(CartItem cartItem, CartItemDTO dto) {
    if (dto.getQuantity() != null) {
      cartItem.setQuantity(dto.getQuantity());
    }
    if (dto.getStartDate() != null) {
      cartItem.setStartDate(dto.getStartDate());
    }
    if (dto.getEstimatedEndDate() != null) {
      cartItem.setEstimatedEndDate(dto.getEstimatedEndDate());
    }
    if (dto.getEndDate() != null) {
      cartItem.setEndDate(dto.getEndDate());
    }
    if (dto.getSubtotal() != null) {
      cartItem.setSubtotal(dto.getSubtotal());
    }
  }
}
