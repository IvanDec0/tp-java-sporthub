package com.java.sportshub.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

import com.java.sportshub.dtos.StoreDTO;
import com.java.sportshub.mappers.StoreMapper;
import com.java.sportshub.models.Store;
import com.java.sportshub.services.StoreService;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

  @Autowired
  private StoreService storeService;

  @GetMapping
  public ResponseEntity<List<StoreDTO>> getAllStores() {
    List<Store> stores = storeService.getAllStores();
    List<StoreDTO> storeDTOs = stores.stream()
        .map(StoreMapper::toDTO)
        .toList();
    return ResponseEntity.ok(storeDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<StoreDTO> getStoreById(@PathVariable Long id) {
    Store store = storeService.getStoreById(id);
    return ResponseEntity.ok(StoreMapper.toDTO(store));
  }

  @PostMapping
  public ResponseEntity<StoreDTO> createStore(@RequestBody Store store) {
    Store createdStore = storeService.createStore(store);
    return ResponseEntity.status(HttpStatus.CREATED).body(StoreMapper.toDTO(createdStore));
  }

  @PutMapping("/{id}")
  public ResponseEntity<StoreDTO> updateStore(
      @PathVariable Long id,
      @RequestBody Store store) {
    Store updatedStore = storeService.updateStore(id, store);
    return ResponseEntity.ok(StoreMapper.toDTO(updatedStore));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Map<String, String>> deleteStore(@PathVariable Long id) {
    storeService.deleteStore(id);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Store deleted successfully");
    return ResponseEntity.ok(response);
  }
}
