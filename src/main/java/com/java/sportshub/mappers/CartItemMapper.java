package com.java.sportshub.mappers;

import com.java.sportshub.dtos.CartItemDTO;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Store;
import com.java.sportshub.models.User;

public class CartItemMapper {

  public static CartItemDTO toDTO(CartItem cartItem) {
    return new CartItemDTO(cartItem);
  }

  public static CartItem toEntity(CartItemDTO dto) {
    CartItem cartItem = new CartItem();
    if (dto.getId() != null) {
      cartItem.setId(dto.getId());
    }
    cartItem.setQuantity(dto.getQuantity());
    cartItem.setStartDate(dto.getStartDate());
    cartItem.setEstimatedEndDate(dto.getEstimatedEndDate());
    cartItem.setEndDate(dto.getEndDate());
    cartItem.setSubtotal(dto.getSubtotal());
    if (dto.getCartId() != null) {
      Cart cart = new Cart();
      cart.setId(dto.getCartId());
      cartItem.setCart(cart);
    } else if (dto.getCart() != null) {
      Cart cart = new Cart();
      if (dto.getCart().getId() != null) {
        cart.setId(dto.getCart().getId());
      }
      if (dto.getCart().getUserId() != null) {
        User user = new User();
        user.setId(dto.getCart().getUserId());
        cart.setUser(user);
      } else if (dto.getCart().getUser() != null && dto.getCart().getUser().getId() != null) {
        User user = new User();
        user.setId(dto.getCart().getUser().getId());
        cart.setUser(user);
      }
      if (dto.getCart().getStoreId() != null) {
        Store store = new Store();
        store.setId(dto.getCart().getStoreId());
        cart.setStore(store);
      } else if (dto.getCart().getStore() != null && dto.getCart().getStore().getId() != null) {
        Store store = new Store();
        store.setId(dto.getCart().getStore().getId());
        cart.setStore(store);
      }
      cartItem.setCart(cart);
    }
    // If no cartId is provided and we need to create a cart, the controller/service
    // should handle creating the parent cart; the DTO should not carry a nested
    // cart object.

    if (dto.getInventoryId() != null) {
      Inventory inventory = new Inventory();
      inventory.setId(dto.getInventoryId());
      cartItem.setInventory(inventory);
    } else if (dto.getInventory() != null && dto.getInventory().getId() != null) {
      Inventory inventory = new Inventory();
      inventory.setId(dto.getInventory().getId());
      cartItem.setInventory(inventory);
    }
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
    if (dto.getCartId() != null) {
      Cart cart = new Cart();
      cart.setId(dto.getCartId());
      cartItem.setCart(cart);
    } else if (dto.getCart() != null) {
      Cart cart = new Cart();
      if (dto.getCart().getId() != null) {
        cart.setId(dto.getCart().getId());
      }
      if (dto.getCart().getUserId() != null) {
        User user = new User();
        user.setId(dto.getCart().getUserId());
        cart.setUser(user);
      } else if (dto.getCart().getUser() != null && dto.getCart().getUser().getId() != null) {
        User user = new User();
        user.setId(dto.getCart().getUser().getId());
        cart.setUser(user);
      }
      if (dto.getCart().getStoreId() != null) {
        Store store = new Store();
        store.setId(dto.getCart().getStoreId());
        cart.setStore(store);
      } else if (dto.getCart().getStore() != null && dto.getCart().getStore().getId() != null) {
        Store store = new Store();
        store.setId(dto.getCart().getStore().getId());
        cart.setStore(store);
      }
      cartItem.setCart(cart);
    }
    if (dto.getInventoryId() != null) {
      Inventory inventory = new Inventory();
      inventory.setId(dto.getInventoryId());
      cartItem.setInventory(inventory);
    }
  }
}
