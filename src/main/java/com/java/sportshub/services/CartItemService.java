package com.java.sportshub.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.dtos.RentalAvailabilityDTO;
import com.java.sportshub.exceptions.RentalNotAvailableException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;

@Service
public class CartItemService {

  @Autowired
  private CartItemDAO cartItemDAO;

  @Autowired
  private InventoryDAO inventoryDAO;

  @Autowired
  private RentalService rentalService;

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
    Inventory inventory = resolveInventory(cartItem);
    cartItem.setInventory(inventory);

    validateCartItemData(cartItem, inventory);
    calculateSubtotal(cartItem);
    cartItemDAO.save(cartItem);
    return cartItem;
  }

  @Transactional
  public CartItem updateCartItem(Long id, CartItem cartItemDetails) {
    CartItem cartItem = getCartItemById(id);

    if (cartItemDetails.getQuantity() != null) {
      cartItem.setQuantity(cartItemDetails.getQuantity());
    }
    if (cartItemDetails.getStartDate() != null) {
      cartItem.setStartDate(cartItemDetails.getStartDate());
    }
    if (cartItemDetails.getEstimatedEndDate() != null) {
      cartItem.setEstimatedEndDate(cartItemDetails.getEstimatedEndDate());
    }
    if (cartItemDetails.getEndDate() != null) {
      cartItem.setEndDate(cartItemDetails.getEndDate());
    }
    if (cartItemDetails.getInventory() != null && cartItemDetails.getInventory().getId() != null) {
      cartItem.setInventory(cartItemDetails.getInventory());
    }

    Inventory inventory = resolveInventory(cartItem);
    validateCartItemData(cartItem, inventory);

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
    Inventory inventory = cartItem.getInventory();
    double basePrice = inventory.getPrice();
    int quantity = cartItem.getQuantity();

    if (isRental(inventory)) {
      LocalDate startDate = cartItem.getStartDate();
      LocalDate endDate = cartItem.getEstimatedEndDate();
      long days = Math.max(1, ChronoUnit.DAYS.between(startDate, endDate));
      double pricePerDay = inventory.getPricePerDay() != null ? inventory.getPricePerDay() : basePrice;
      double subtotal = pricePerDay * quantity * days;

      cartItem.setSubtotal(roundCurrency(subtotal));
      return;
    }

    cartItem.setSubtotal(roundCurrency(basePrice * quantity));
  }

  private void validateCartItemData(CartItem cartItem, Inventory inventory) {
    if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
      throw new ValidationException("quantity", "Quantity must be greater than zero");
    }

    if (isRental(inventory)) {
      validateRentalData(cartItem, inventory);
    } else {
      rentalService.validateStockForSale(inventory.getId(), cartItem.getQuantity());
    }
  }

  private void validateRentalData(CartItem cartItem, Inventory inventory) {
    LocalDate startDate = cartItem.getStartDate();
    LocalDate endDate = cartItem.getEstimatedEndDate();

    if (startDate == null || endDate == null) {
      throw new ValidationException("rentalDates", "Start date and estimated end date are required for rentals");
    }

    RentalAvailabilityDTO availability = rentalService.checkRentalAvailability(
        inventory.getId(),
        startDate,
        endDate,
        cartItem.getQuantity());

    if (!availability.getIsAvailable()) {
      throw new RentalNotAvailableException(availability.getMessage());
    }
  }

  private Inventory resolveInventory(CartItem cartItem) {
    if (cartItem.getInventory() == null || cartItem.getInventory().getId() == null) {
      throw new ValidationException("inventory", "Inventory with a valid id is required");
    }

    Inventory inventory = inventoryDAO.findById(cartItem.getInventory().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", cartItem.getInventory().getId()));

    cartItem.setInventory(inventory);
    return inventory;
  }

  private boolean isRental(Inventory inventory) {
    return inventory.getTipo() != null && "alquiler".equalsIgnoreCase(inventory.getTipo());
  }

  private double roundCurrency(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
