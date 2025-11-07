package com.java.sportshub.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.CartItemDTO;
import com.java.sportshub.mappers.CartItemMapper;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.services.CartItemService;

@RestController
@RequestMapping("/api/cart-items")
public class CartItemController {

  @Autowired
  private CartItemService cartItemService;

  @GetMapping
  public ResponseEntity<List<CartItemDTO>> getAllCartItems() {
    List<CartItem> cartItems = cartItemService.getAllCartItems();
    List<CartItemDTO> cartItemDTOs = cartItems.stream()
        .map(CartItemMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(cartItemDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CartItemDTO> getCartItemById(@PathVariable("id") Long id) {
    CartItem cartItem = cartItemService.getCartItemById(id);
    return ResponseEntity.ok(CartItemMapper.toDTO(cartItem));
  }

  @GetMapping("/cart/{cartId}")
  public ResponseEntity<List<CartItemDTO>> getCartItemsByCartId(@PathVariable("cartId") Long cartId) {
    List<CartItem> cartItems = cartItemService.getCartItemsByCartId(cartId);
    List<CartItemDTO> cartItemDTOs = cartItems.stream()
        .map(CartItemMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(cartItemDTOs);
  }

  @PostMapping
  public ResponseEntity<CartItemDTO> createCartItem(@RequestBody CartItemDTO cartItemDTO) {
    if (cartItemDTO.getCart() == null) {
      throw new IllegalArgumentException("Cart is required");
    }
    if (cartItemDTO.getInventory() == null) {
      throw new IllegalArgumentException("Inventory is required");
    }
    CartItem cartItem = CartItemMapper.toEntity(cartItemDTO);
    CartItem createdCartItem = cartItemService.createCartItem(cartItem);
    return ResponseEntity.status(HttpStatus.CREATED).body(CartItemMapper.toDTO(createdCartItem));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CartItemDTO> updateCartItem(
      @PathVariable("id") Long id,
      @RequestBody CartItemDTO cartItemDTO) {
    CartItem cartItem = CartItemMapper.toEntity(cartItemDTO);
    CartItem updatedCartItem = cartItemService.updateCartItem(id, cartItem);
    return ResponseEntity.ok(CartItemMapper.toDTO(updatedCartItem));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCartItem(@PathVariable("id") Long id) {
    cartItemService.deleteCartItem(id);
    return ResponseEntity.noContent().build();
  }
}
