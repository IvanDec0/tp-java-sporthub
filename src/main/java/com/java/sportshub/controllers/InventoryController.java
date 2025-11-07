package com.java.sportshub.controllers;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.InventoryDTO;
import com.java.sportshub.mappers.InventoryMapper;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.services.InventoryService;

@RestController
@RequestMapping("/api/inventories")
public class InventoryController {

  @Autowired
  private InventoryService inventoryService;

  @GetMapping
  public ResponseEntity<List<InventoryDTO>> getAllInventory() {
    List<Inventory> inventories = inventoryService.getAllInventory();
    List<InventoryDTO> inventoryDTOs = inventories.stream()
        .map(InventoryMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(inventoryDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<InventoryDTO> getInventoryById(@PathVariable("id") Long id) {
    Inventory inventory = inventoryService.getInventoryById(id);
    return ResponseEntity.ok(InventoryMapper.toDTO(inventory));
  }

  @GetMapping("/store/{storeId}")
  public ResponseEntity<List<InventoryDTO>> getInventoryByStoreId(@PathVariable("storeId") Long storeId) {
    List<Inventory> inventories = inventoryService.getInventoryByStoreId(storeId);
    List<InventoryDTO> inventoryDTOs = inventories.stream()
        .map(InventoryMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(inventoryDTOs);
  }

  @GetMapping("/product/{productId}")
  public ResponseEntity<List<InventoryDTO>> getInventoryByProductId(@PathVariable("productId") Long productId) {
    List<Inventory> inventories = inventoryService.getInventoryByProductId(productId);
    List<InventoryDTO> inventoryDTOs = inventories.stream()
        .map(InventoryMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(inventoryDTOs);
  }

  @GetMapping("/type/{tipo}")
  public ResponseEntity<List<InventoryDTO>> getInventoryByType(@PathVariable("tipo") String tipo) {
    List<Inventory> inventories = inventoryService.getInventoryByType(tipo);
    List<InventoryDTO> inventoryDTOs = inventories.stream()
        .map(InventoryMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(inventoryDTOs);
  }

  @GetMapping("/available")
  public ResponseEntity<List<InventoryDTO>> getAvailableInventory() {
    List<Inventory> inventories = inventoryService.getAvailableInventory();
    List<InventoryDTO> inventoryDTOs = inventories.stream()
        .map(InventoryMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(inventoryDTOs);
  }

  @PostMapping
  public ResponseEntity<InventoryDTO> createInventory(@RequestBody InventoryDTO inventoryDTO) {
    if (inventoryDTO.getProduct() == null || inventoryDTO.getProduct().getId() == null) {
      throw new IllegalArgumentException("Product ID is required");
    }
    if (inventoryDTO.getStore() == null || inventoryDTO.getStore().getId() == null) {
      throw new IllegalArgumentException("Store ID is required");
    }

    Inventory inventory = InventoryMapper.toEntity(inventoryDTO);
    Inventory createdInventory = inventoryService.createInventory(
        inventory,
        inventoryDTO.getProduct().getId(),
        inventoryDTO.getStore().getId());
    return ResponseEntity.status(HttpStatus.CREATED).body(InventoryMapper.toDTO(createdInventory));
  }

  @PutMapping("/{id}")
  public ResponseEntity<InventoryDTO> updateInventory(
      @PathVariable("id") Long id,
      @RequestBody InventoryDTO inventoryDTO) {
    Inventory inventory = InventoryMapper.toEntity(inventoryDTO);

    Long productId = inventoryDTO.getProduct() != null ? inventoryDTO.getProduct().getId() : null;
    Long storeId = inventoryDTO.getStore() != null ? inventoryDTO.getStore().getId() : null;

    Inventory updatedInventory = inventoryService.updateInventory(id, inventory, productId, storeId);
    return ResponseEntity.ok(InventoryMapper.toDTO(updatedInventory));
  }

  @PatchMapping("/{id}/update-stock")
  public ResponseEntity<InventoryDTO> updateStock(
      @PathVariable("id") Long id,
      @RequestParam("quantity") Integer quantity) {
    Inventory updatedInventory = inventoryService.updateStock(id, quantity);
    return ResponseEntity.ok(InventoryMapper.toDTO(updatedInventory));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteInventory(@PathVariable("id") Long id) {
    inventoryService.deleteInventory(id);
    return ResponseEntity.noContent().build();
  }
}
