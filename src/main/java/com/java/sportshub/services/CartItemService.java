package com.java.sportshub.services;

import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;

@Service
public class CartItemService {

  @Autowired
  private CartItemDAO cartItemDAO;

  public List<CartItem> getAllCartItems() {
    return cartItemDAO.findAll();
  }

  public CartItem getCartItemById(Long id) {
    return cartItemDAO.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("CartItem", "id", id));
  }

  public List<CartItem> getCartItemsByCartId(Long cartId) {
    return cartItemDAO.findActiveItemsByCartId(cartId);
  }

  @Transactional
  public CartItem createCartItem(CartItem cartItem) {
    // TODO: Verificar disponibilidad del inventario según fechas de alquiler
    // TODO: Validar que la cantidad solicitada no exceda el stock disponible
    // TODO: Calcular el subtotal según tipo (venta o alquiler) y duración

    calculateSubtotal(cartItem);
    cartItemDAO.save(cartItem);
    return cartItem;
  }

  @Transactional
  public CartItem updateCartItem(Long id, CartItem cartItemDetails) {
    CartItem cartItem = getCartItemById(id);

    // TODO: Verificar disponibilidad del inventario según fechas de alquiler
    // TODO: Validar que la cantidad solicitada no exceda el stock disponible
    // TODO: Calcular el subtotal según tipo (venta o alquiler) y duración

    cartItem.setQuantity(cartItemDetails.getQuantity());
    cartItem.setStartDate(cartItemDetails.getStartDate());
    cartItem.setEstimatedEndDate(cartItemDetails.getEstimatedEndDate());
    cartItem.setEndDate(cartItemDetails.getEndDate());

    calculateSubtotal(cartItem);
    cartItemDAO.save(cartItem);
    return cartItem;
  }

  @Transactional
  public CartItem deleteCartItem(Long id) {
    CartItem cartItem = getCartItemById(id);
    cartItem.setIsActive(false);
    cartItemDAO.save(cartItem);
    return cartItem;
  }


  private void calculateSubtotal(CartItem cartItem) {
    // TODO: Implementar lógica compleja de cálculo
    // - Si es venta: precio x cantidad
    // - Si es alquiler: precio diario x cantidad x días
    // - Aplicar descuentos por cantidad de días (ej: >7 días = 10% descuento)

    Inventory inventory = cartItem.getInventory();
    double basePrice = inventory.getPrice();
    int quantity = cartItem.getQuantity();

    if ("Alquiler".equalsIgnoreCase(inventory.getTipo())) {
      if (cartItem.getStartDate() != null && cartItem.getEstimatedEndDate() != null) {
        long days = ChronoUnit.DAYS.between(cartItem.getStartDate(), cartItem.getEstimatedEndDate());
        days = Math.max(1, days); // Mínimo 1 día
        cartItem.setSubtotal(basePrice * quantity * days);
      }
    } else {
      cartItem.setSubtotal(basePrice * quantity);
    }
  }
}
