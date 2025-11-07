package com.java.sportshub.dtos;

import com.java.sportshub.models.Inventory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class InventoryDTO {
  private Long id;
  private Integer quantity;
  private Double price;
  private String tipo; // 'Venta' o 'Alquiler'
  private ProductDTO product;
  private StoreDTO store;

  public InventoryDTO(Inventory inventory) {
    this.id = inventory.getId();
    this.product = inventory.getProduct() != null ? new ProductDTO(inventory.getProduct()) : null;
    this.store = inventory.getStore() != null ? new StoreDTO(inventory.getStore()) : null;
    this.quantity = inventory.getQuantity();
    this.price = inventory.getPrice();
    this.tipo = inventory.getTipo();
  }
}
