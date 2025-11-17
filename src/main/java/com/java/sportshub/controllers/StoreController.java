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
import com.java.sportshub.middlewares.AuthenticatedUser;
import com.java.sportshub.middlewares.RequiredRoles;
import com.java.sportshub.models.Store;
import com.java.sportshub.models.User;
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
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<StoreDTO> createStore(
      @AuthenticatedUser User authenticatedUser,
      @RequestBody StoreDTO storeDTO) {
    Store createdStore = storeService.createStore(StoreMapper.toEntity(storeDTO), authenticatedUser);
    return ResponseEntity.status(HttpStatus.CREATED).body(StoreMapper.toDTO(createdStore));
  }

  @PutMapping("/{id}")
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<StoreDTO> updateStore(
      @PathVariable Long id,
      @AuthenticatedUser User authenticatedUser,
      @RequestBody StoreDTO storeDTO) {
    Store updatedStore = storeService.updateStore(id, StoreMapper.toEntity(storeDTO), authenticatedUser);
    return ResponseEntity.ok(StoreMapper.toDTO(updatedStore));
  }

  @DeleteMapping("/{id}")
  @RequiredRoles({ "USER", "ADMIN" })
  public ResponseEntity<Map<String, String>> deleteStore(
      @PathVariable Long id,
      @AuthenticatedUser User authenticatedUser) {
    storeService.deleteStore(id, authenticatedUser);
    Map<String, String> response = new HashMap<>();
    response.put("message", "Store deleted successfully");
    return ResponseEntity.ok(response);
  }
}
