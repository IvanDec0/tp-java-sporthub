package com.java.sportshub.services;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.dtos.RentalAvailabilityDTO;
import com.java.sportshub.exceptions.RentalNotAvailableException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;

@Service
public class CartItemService {

  @Autowired
  private CartItemDAO cartItemDAO;

  @Autowired
  private InventoryDAO inventoryDAO;

  @Autowired
  private CartDAO cartDAO;

  @Autowired
  private CartService cartService;

  @Autowired
  private RentalService rentalService;

  @Autowired
  private PricingService pricingService;

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
    Cart cart = getOrCreateCartForItem(cartItem, inventory);
    cartItem.setCart(cart);

    validateCartItemData(cartItem, inventory);
    calculateSubtotal(cartItem);
    cartItemDAO.save(cartItem);
    if (cartItem.getCart() != null && cartItem.getCart().getId() != null) {
      pricingService.computeCartTotal(cartItem.getCart().getId());
    }
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
    if (cartItemDetails.getCart() != null && cartItemDetails.getCart().getId() != null) {
      cartItem.setCart(cartItemDetails.getCart());
    }

    Inventory inventory = resolveInventory(cartItem);
    validateCartItemData(cartItem, inventory);

    // Also validate cart if changed
    if (cartItem.getCart() == null || cartItem.getCart().getId() == null) {
      throw new ValidationException("cart", "Se debe especificar un carrito");
    }
    Cart cart = resolveCart(cartItem);
    cartItem.setCart(cart);

    calculateSubtotal(cartItem);
    cartItemDAO.save(cartItem);
    if (cartItem.getCart() != null && cartItem.getCart().getId() != null) {
      pricingService.computeCartTotal(cartItem.getCart().getId());
    }
    return cartItem;
  }

  @Transactional
  public CartItem deleteCartItem(Long id) {

    CartItem cartItem = getCartItemById(id);
    cartItem.setIsActive(false);
    cartItemDAO.save(cartItem);
    if (cartItem.getCart() != null && cartItem.getCart().getId() != null) {
      pricingService.computeCartTotal(cartItem.getCart().getId());
    }
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
      throw new ValidationException("quantity", "La cantidad debe ser mayor a cero");
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
      throw new ValidationException("rentalDates", "Se requieren fecha de inicio y fecha de fin estimada para alquileres");
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
      throw new ValidationException("inventory", "Se requiere un inventario con un ID válido");
    }

    Inventory inventory = inventoryDAO.findById(cartItem.getInventory().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", cartItem.getInventory().getId()));

    cartItem.setInventory(inventory);
    return inventory;
  }

  private Cart resolveCart(CartItem cartItem) {
    if (cartItem.getCart() == null || cartItem.getCart().getId() == null) {
      throw new ValidationException("cart", "Se requiere un carrito con un ID válido");
    }

    Cart cart = cartDAO.findById(cartItem.getCart().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartItem.getCart().getId()));
    if (cart.getIsActive() == null || !cart.getIsActive()) {
      throw new ValidationException("cart", "El carrito debe estar activo");
    }
    cartItem.setCart(cart);
    return cart;
  }

  private Cart getOrCreateCartForItem(CartItem cartItem, Inventory inventory) {
    // If cart id provided, resolve normally
    if (cartItem.getCart() != null && cartItem.getCart().getId() != null) {
      return resolveCart(cartItem);
    }

    // Otherwise, try to use userId in cart entity
    Long userId = null;
    if (cartItem.getCart() != null && cartItem.getCart().getUser() != null) {
      userId = cartItem.getCart().getUser().getId();
    }

    Long storeId = null;
    if (cartItem.getCart() != null && cartItem.getCart().getStore() != null) {
      storeId = cartItem.getCart().getStore().getId();
    }
    if (storeId == null && inventory != null && inventory.getStore() != null) {
      storeId = inventory.getStore().getId();
    }

    if (userId == null) {
      throw new ValidationException("cart.userId", "Se requiere el ID de usuario para crear un carrito cuando no se proporciona el ID del carrito");
    }

    // Try to find active cart for user
    try {
      Cart existing = cartDAO.findActiveCartByUserId(userId).orElse(null);
      if (existing != null) {
        // If store mismatch, don't create a second active cart - return error
        if (storeId != null && existing.getStore() != null && !storeId.equals(existing.getStore().getId())) {
          throw new ValidationException("cart", "El usuario ya tiene un carrito activo para una tienda diferente");
        }
        return existing;
      }
    } catch (Exception e) {
      // ignore, will create new
    }

    // If not found or not active, create a new cart
    if (storeId == null) {
      throw new ValidationException("storeId", "Se requiere el ID de la tienda para crear un nuevo carrito");
    }
    Cart newCart = cartService.createCart(new Cart(), userId, storeId);
    return newCart;
  }

  private boolean isRental(Inventory inventory) {
    return inventory.getTipo() != null && "alquiler".equalsIgnoreCase(inventory.getTipo());
  }

  private double roundCurrency(double value) {
    return Math.round(value * 100.0) / 100.0;
  }
}
