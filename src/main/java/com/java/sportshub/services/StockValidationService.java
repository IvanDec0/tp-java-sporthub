package com.java.sportshub.services;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.daos.RentalReservationDAO;
import com.java.sportshub.dtos.RentalAvailabilityDTO;
import com.java.sportshub.dtos.StockValidationDTO;
import com.java.sportshub.dtos.StockValidationDTO.ItemValidationResult;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;

@Service
public class StockValidationService {

  @Autowired
  private CartDAO cartDAO;

  @Autowired
  private InventoryDAO inventoryDAO;

  @Autowired
  private RentalReservationDAO rentalReservationDAO;

  @Autowired
  private RentalService rentalService;

  /**
   * Valida el stock de todos los items del carrito.
   * Para items de VENTA: valida que haya stock físico suficiente.
   * Para items de ALQUILER: valida disponibilidad en las fechas indicadas.
   */
  @Transactional(readOnly = true)
  public StockValidationDTO validateCartStock(Long cartId) {
    System.out.println(String.format("[StockValidationService] Validating cart stock for cart ID: %d", cartId));

    Cart cart = cartDAO.findById(cartId)
        .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      System.out.println(String.format("[StockValidationService] Cart %d is empty", cartId));
      return StockValidationDTO.failure(cartId, "El carrito está vacío", new ArrayList<>());
    }

    System.out.println(String.format("[StockValidationService] Cart %d has %d items", cartId, cart.getItems().size()));

    List<ItemValidationResult> results = new ArrayList<>();
    boolean allValid = true;
    List<String> errorMessages = new ArrayList<>();

    for (CartItem item : cart.getItems()) {
      ItemValidationResult result = validateCartItem(item);
      results.add(result);

      if (!result.getIsValid()) {
        allValid = false;
        errorMessages.add(result.getMessage());
      }
    }

    if (allValid) {
      StockValidationDTO dto = StockValidationDTO.success(cartId);
      dto.setItemResults(results);
      return dto;
    } else {
      String combinedMessage = String.join("; ", errorMessages);
      return StockValidationDTO.failure(cartId, combinedMessage, results);
    }
  }

  /**
   * Valida un item individual del carrito.
   */
  private ItemValidationResult validateCartItem(CartItem item) {
    Inventory inventory = inventoryDAO.findById(item.getInventory().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", item.getInventory().getId()));

    String tipo = inventory.getTipo();
    String productName = inventory.getProduct() != null ? inventory.getProduct().getName() : "Producto desconocido";

    ItemValidationResult.ItemValidationResultBuilder resultBuilder = ItemValidationResult.builder()
        .cartItemId(item.getId())
        .inventoryId(inventory.getId())
        .productName(productName)
        .tipo(tipo)
        .requestedQuantity(item.getQuantity());

    if ("venta".equalsIgnoreCase(tipo)) {
      return validateSaleItem(item, inventory, resultBuilder);
    } else if ("alquiler".equalsIgnoreCase(tipo)) {
      return validateRentalItem(item, inventory, resultBuilder);
    } else {
      return resultBuilder
          .isValid(false)
          .message("Tipo de producto no válido: " + tipo)
          .build();
    }
  }

  /**
   * Valida stock para items de VENTA.
   */
  private ItemValidationResult validateSaleItem(CartItem item, Inventory inventory,
      ItemValidationResult.ItemValidationResultBuilder resultBuilder) {

    int availableStock = inventory.getQuantity();
    int requestedQty = item.getQuantity();
    boolean isValid = availableStock >= requestedQty;

    resultBuilder.availableQuantity(availableStock);

    if (isValid) {
      return resultBuilder
          .isValid(true)
          .message("Stock disponible")
          .build();
    } else {
      return resultBuilder
          .isValid(false)
          .message(String.format("Stock insuficiente para '%s'. Disponible: %d, Solicitado: %d",
              inventory.getProduct().getName(), availableStock, requestedQty))
          .build();
    }
  }

  /**
   * Valida disponibilidad para items de ALQUILER.
   */
  private ItemValidationResult validateRentalItem(CartItem item, Inventory inventory,
      ItemValidationResult.ItemValidationResultBuilder resultBuilder) {

    LocalDate startDate = item.getStartDate();
    LocalDate endDate = item.getEstimatedEndDate();

    resultBuilder.startDate(startDate).endDate(endDate);

    // Log para debugging
    System.out.println(
        String.format("[StockValidationService] Validating rental for inventory %d (%s), dates: %s to %s, quantity: %d",
            inventory.getId(), inventory.getProduct().getName(), startDate, endDate, item.getQuantity()));

    // Verificar que las fechas estén presentes
    if (startDate == null || endDate == null) {
      return resultBuilder
          .isValid(false)
          .availableQuantity(0)
          .message(String.format("El producto '%s' requiere fechas de inicio y fin para el alquiler",
              inventory.getProduct().getName()))
          .build();
    }

    // Verificar que la fecha de inicio no sea anterior a hoy
    if (startDate.isBefore(LocalDate.now())) {
      return resultBuilder
          .isValid(false)
          .availableQuantity(0)
          .message(String.format("La fecha de inicio del alquiler para '%s' no puede ser anterior a hoy",
              inventory.getProduct().getName()))
          .build();
    }

    // Verificar que la fecha fin sea posterior a inicio
    if (endDate.isBefore(startDate) || endDate.isEqual(startDate)) {
      return resultBuilder
          .isValid(false)
          .availableQuantity(0)
          .message(String.format("La fecha de fin debe ser posterior a la fecha de inicio para '%s'",
              inventory.getProduct().getName()))
          .build();
    }

    // Calcular disponibilidad real considerando reservas existentes
    try {
      RentalAvailabilityDTO availability = rentalService.checkRentalAvailability(
          inventory.getId(),
          startDate,
          endDate,
          item.getQuantity());

      resultBuilder.availableQuantity(availability.getAvailableQuantity().intValue());

      if (availability.getIsAvailable()) {
        return resultBuilder
            .isValid(true)
            .message("Disponible para alquiler en las fechas seleccionadas")
            .build();
      } else {
        return resultBuilder
            .isValid(false)
            .message(String.format(
                "Stock insuficiente para alquilar '%s' en las fechas seleccionadas. " +
                    "Disponible: %d, Solicitado: %d",
                inventory.getProduct().getName(),
                availability.getAvailableQuantity(),
                item.getQuantity()))
            .build();
      }
    } catch (Exception e) {
      return resultBuilder
          .isValid(false)
          .availableQuantity(0)
          .message("Error al validar disponibilidad: " + e.getMessage())
          .build();
    }
  }

  /**
   * Valida stock de un item específico (útil para validaciones individuales).
   */
  @Transactional(readOnly = true)
  public ItemValidationResult validateSingleItem(Long inventoryId, Integer quantity,
      LocalDate startDate, LocalDate endDate) {

    Inventory inventory = inventoryDAO.findById(inventoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", inventoryId));

    String productName = inventory.getProduct() != null ? inventory.getProduct().getName() : "Producto desconocido";

    ItemValidationResult.ItemValidationResultBuilder resultBuilder = ItemValidationResult.builder()
        .inventoryId(inventoryId)
        .productName(productName)
        .tipo(inventory.getTipo())
        .requestedQuantity(quantity)
        .startDate(startDate)
        .endDate(endDate);

    if ("venta".equalsIgnoreCase(inventory.getTipo())) {
      int availableStock = inventory.getQuantity();
      boolean isValid = availableStock >= quantity;

      return resultBuilder
          .availableQuantity(availableStock)
          .isValid(isValid)
          .message(isValid ? "Stock disponible" : String.format("Stock insuficiente. Disponible: %d", availableStock))
          .build();
    } else if ("alquiler".equalsIgnoreCase(inventory.getTipo())) {
      if (startDate == null || endDate == null) {
        return resultBuilder
            .isValid(false)
            .availableQuantity(0)
            .message("Se requieren fechas de inicio y fin para validar alquiler")
            .build();
      }

      try {
        RentalAvailabilityDTO availability = rentalService.checkRentalAvailability(
            inventoryId, startDate, endDate, quantity);

        return resultBuilder
            .availableQuantity(availability.getAvailableQuantity().intValue())
            .isValid(availability.getIsAvailable())
            .message(availability.getMessage())
            .build();
      } catch (Exception e) {
        return resultBuilder
            .isValid(false)
            .availableQuantity(0)
            .message("Error: " + e.getMessage())
            .build();
      }
    } else {
      return resultBuilder
          .isValid(false)
          .message("Tipo de producto no válido")
          .build();
    }
  }
}
