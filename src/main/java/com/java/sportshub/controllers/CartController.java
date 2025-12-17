package com.java.sportshub.controllers;

import java.util.List;
import java.util.Map;
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

import com.java.sportshub.dtos.CartDTO;
import com.java.sportshub.dtos.StockValidationDTO;
import com.java.sportshub.mappers.CartMapper;
import com.java.sportshub.models.Cart;
import com.java.sportshub.services.CartService;
import com.java.sportshub.services.StockValidationService;

@RestController
@RequestMapping("/api/carts")
public class CartController {

  @Autowired
  private CartService cartService;

  @Autowired
  private StockValidationService stockValidationService;

  @GetMapping
  public ResponseEntity<List<CartDTO>> getAllCarts() {
    List<Cart> carts = cartService.getAllCarts();
    List<CartDTO> cartDTOs = carts.stream()
        .map(CartMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(cartDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<CartDTO> getCartById(@PathVariable("id") Long id) {
    Cart cart = cartService.getCartById(id);
    return ResponseEntity.ok(CartMapper.toDTO(cart));
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<CartDTO>> getCartsByUserId(@PathVariable("userId") Long userId) {
    List<Cart> carts = cartService.getCartsByUserId(userId);
    List<CartDTO> cartDTOs = carts.stream()
        .map(CartMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(cartDTOs);
  }

  @GetMapping("/user/{userId}/active")
  public ResponseEntity<CartDTO> getActiveCartByUserId(@PathVariable("userId") Long userId) {
    Cart cart = cartService.getActiveCartByUserId(userId);
    return ResponseEntity.ok(CartMapper.toDTO(cart));
  }

  @PostMapping
  public ResponseEntity<CartDTO> createCart(@RequestBody CartDTO cartDTO) {
    if (cartDTO.getUserId() == null) {
      throw new IllegalArgumentException("User ID is required");
    }
    if (cartDTO.getStoreId() == null) {
      throw new IllegalArgumentException("Store ID is required");
    }

    Cart cart = CartMapper.toEntity(cartDTO);
    Cart createdCart = cartService.createCart(cart, cartDTO.getUserId(), cartDTO.getStoreId());
    return ResponseEntity.status(HttpStatus.CREATED).body(CartMapper.toDTO(createdCart));
  }

  @PutMapping("/{id}")
  public ResponseEntity<CartDTO> updateCart(
      @PathVariable("id") Long id,
      @RequestBody CartDTO cartDTO) {
    Cart cart = CartMapper.toEntity(cartDTO);
    Cart updatedCart = cartService.updateCart(id, cart);
    return ResponseEntity.ok(CartMapper.toDTO(updatedCart));
  }

  @PostMapping("/{id}/complete")
  public ResponseEntity<CartDTO> completeCart(@PathVariable("id") Long id) {
    Cart completedCart = cartService.completeCart(id);
    return ResponseEntity.ok(CartMapper.toDTO(completedCart));
  }

  @PostMapping("/{id}/apply-coupon")
  public ResponseEntity<CartDTO> applyCoupon(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
    String code = body.get("couponCode");
    Cart cart = cartService.applyCartCoupon(id, code);
    return ResponseEntity.ok(CartMapper.toDTO(cart));
  }

  @PostMapping("/{id}/remove-coupon")
  public ResponseEntity<CartDTO> removeCoupon(@PathVariable("id") Long id) {
    Cart cart = cartService.removeCartCoupon(id);
    return ResponseEntity.ok(CartMapper.toDTO(cart));
  }

  /**
   * Valida el stock de todos los items del carrito antes de proceder al pago.
   * Para items de VENTA: valida stock físico disponible.
   * Para items de ALQUILER: valida disponibilidad en las fechas indicadas.
   */
  @PostMapping("/{id}/validate-stock")
  public ResponseEntity<StockValidationDTO> validateCartStock(@PathVariable("id") Long id) {
    StockValidationDTO validation = stockValidationService.validateCartStock(id);
    
    if (validation.getIsValid()) {
      return ResponseEntity.ok(validation);
    } else {
      // Retornamos 200 pero con isValid=false para que el frontend maneje el error
      return ResponseEntity.ok(validation);
    }
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteCart(@PathVariable("id") Long id) {
    cartService.deleteCart(id);
    return ResponseEntity.noContent().build();
  }

}
