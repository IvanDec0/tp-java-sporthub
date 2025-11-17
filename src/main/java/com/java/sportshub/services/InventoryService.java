package com.java.sportshub.services;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.daos.ProductDAO;
import com.java.sportshub.daos.RentalReservationDAO;
import com.java.sportshub.daos.StoreDAO;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Product;
import com.java.sportshub.models.RentalReservation;
import com.java.sportshub.models.Store;

@Service
public class InventoryService {

    @Autowired
    private InventoryDAO inventoryDAO;

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private StoreDAO storeDAO;

    @Autowired
    private RentalReservationDAO rentalReservationDAO;

    public List<Inventory> getAllInventory() {
        return inventoryDAO.findAll();
    }

    public Inventory getInventoryById(Long id) {
        return inventoryDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", id));
    }

    public List<Inventory> getInventoryByStoreId(Long storeId) {
        return inventoryDAO.findByStoreId(storeId);
    }

    public List<Inventory> getInventoryByProductId(Long productId) {
        return inventoryDAO.findByProductId(productId);
    }

    public List<Inventory> getInventoryByType(String tipo) {
        return inventoryDAO.findByTipo(tipo);
    }

    public List<Inventory> getAvailableInventory() {
        return inventoryDAO.findAvailableInventory();
    }

    @Transactional
    public Inventory createInventory(Inventory inventory, Long productId, Long storeId) {
        Product product = productDAO.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));

        Store store = storeDAO.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));

        inventory.setProduct(product);
        inventory.setStore(store);

        // Validar
        validateInventory(inventory);

        // Guardar
        return inventoryDAO.save(inventory);
    }

    @Transactional
    public Inventory updateInventory(Long id, Inventory inventoryDetails, Long productId, Long storeId) {
        Inventory inventory = getInventoryById(id);

        if (inventoryDetails.getQuantity() >= 0) {
            inventory.setQuantity(inventoryDetails.getQuantity());
        }

        if (inventoryDetails.getPrice() > 0) {
            inventory.setPrice(inventoryDetails.getPrice());
        }

        if (inventoryDetails.getTipo() != null) {
            validateTipo(inventoryDetails.getTipo());
            inventory.setTipo(inventoryDetails.getTipo());
        }

        // Actualizar relaciones si se proporcionan IDs
        if (productId != null) {
            Product product = productDAO.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", productId));
            inventory.setProduct(product);
        }

        if (storeId != null) {
            Store store = storeDAO.findById(storeId)
                    .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));
            inventory.setStore(store);
        }

        return inventoryDAO.save(inventory);
    }

    @Transactional
    public Inventory updateStock(Long id, Integer quantity) {
        Inventory inventory = getInventoryById(id);

        ensureRentalCapacity(inventory, quantity);

        if (quantity < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        inventory.setQuantity(quantity);
        inventoryDAO.save(inventory);
        return inventory;
    }

    @Transactional
    public void deleteInventory(Long id) {
        Inventory inventory = getInventoryById(id);
        inventory.setIsActive(false);
        inventoryDAO.save(inventory);
    }

    private void validateInventory(Inventory inventory) {
        if (inventory.getQuantity() < 0) {
            throw new ValidationException("quantity", "Quantity cannot be negative");
        }

        if (inventory.getPrice() <= 0) {
            throw new ValidationException("price", "Price must be greater than 0");
        }

        if (inventory.getProduct() == null) {
            throw new ValidationException("product", "Product is required");
        }

        if (inventory.getStore() == null) {
            throw new ValidationException("store", "Store is required");
        }

        validateTipo(inventory.getTipo());
    }

    private void validateTipo(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            throw new ValidationException("tipo", "Tipo is required");
        }

        String tipoLower = tipo.toLowerCase().trim();
        if (!tipoLower.equals("venta") && !tipoLower.equals("alquiler")) {
            throw new ValidationException("tipo", "Tipo must be 'venta' or 'alquiler'");
        }
    }

    private void ensureRentalCapacity(Inventory inventory, Integer newQuantity) {
        if (newQuantity == null || !"alquiler".equalsIgnoreCase(inventory.getTipo())) {
            return;
        }

        List<RentalReservation> reservations = rentalReservationDAO.findByInventoryIdAndIsActiveTrue(inventory.getId());

        if (reservations.isEmpty()) {
            return;
        }

        Map<LocalDate, Long> reservedByDate = new HashMap<>();

        for (RentalReservation reservation : reservations) {
            LocalDate current = reservation.getStartDate();
            while (!current.isAfter(reservation.getEndDate())) {
                reservedByDate.merge(current, reservation.getQuantity().longValue(), Long::sum);
                current = current.plusDays(1);
            }
        }

        long maxReserved = reservedByDate.values().stream()
                .mapToLong(Long::longValue)
                .max()
                .orElse(0L);

        if (newQuantity.longValue() < maxReserved) {
            throw new ValidationException("quantity",
                    String.format("Quantity cannot be lower than existing reservations (%d)", maxReserved));
        }
    }
}
