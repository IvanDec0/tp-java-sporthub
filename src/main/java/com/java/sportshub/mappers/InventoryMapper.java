package com.java.sportshub.mappers;

import com.java.sportshub.dtos.InventoryDTO;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Product;
import com.java.sportshub.models.Store;

public class InventoryMapper {

  public static InventoryDTO toDTO(Inventory inventory) {
    return new InventoryDTO(inventory);
  }

  public static Inventory toEntity(InventoryDTO dto) {
    Inventory inventory = new Inventory();
    inventory.setQuantity(dto.getQuantity());
    inventory.setPrice(dto.getPrice());
    inventory.setTipo(dto.getTipo());
    return inventory;
  }

  public static void updateEntity(Inventory inventory, InventoryDTO dto) {
    if (dto.getQuantity() != null && dto.getQuantity() >= 0) {
      inventory.setQuantity(dto.getQuantity());
    }
    if (dto.getPrice() != null && dto.getPrice() > 0) {
      inventory.setPrice(dto.getPrice());
    }
    if (dto.getTipo() != null) {
      inventory.setTipo(dto.getTipo());
    }
  }

  public static void setRelations(Inventory inventory, Product product, Store store) {
    inventory.setProduct(product);
    inventory.setStore(store);
  }
}
