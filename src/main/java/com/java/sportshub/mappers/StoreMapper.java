package com.java.sportshub.mappers;

import com.java.sportshub.dtos.StoreDTO;
import com.java.sportshub.models.Store;

public class StoreMapper {

  public static StoreDTO toDTO(Store store) {
    return new StoreDTO(store);
  }

  public static Store toEntity(StoreDTO dto) {
    Store store = new Store();
    store.setName(dto.getName());
    store.setPhoneNumber(dto.getPhoneNumber());
    store.setEmail(dto.getEmail());
    store.setAddress(dto.getAddress());
    return store;
  }

  public static void updateEntity(Store store, StoreDTO dto) {
    if (dto.getName() != null) {
      store.setName(dto.getName());
    }
    if (dto.getPhoneNumber() != null) {
      store.setPhoneNumber(dto.getPhoneNumber());
    }
    if (dto.getEmail() != null) {
      store.setEmail(dto.getEmail());
    }
    if (dto.getAddress() != null) {
      store.setAddress(dto.getAddress());
    }
  }
}
