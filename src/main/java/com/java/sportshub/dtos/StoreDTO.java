package com.java.sportshub.dtos;

import com.java.sportshub.models.Address;
import com.java.sportshub.models.Store;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class StoreDTO {
  private Long id;
  private String name;
  private String phoneNumber;
  private String email;
  private Address address;
  private Long ownerId;

  public StoreDTO(Store store) {
    this.id = store.getId();
    this.name = store.getName();
    this.phoneNumber = store.getPhoneNumber();
    this.email = store.getEmail();
    this.address = store.getAddress();
    this.ownerId = store.getOwner() != null ? store.getOwner().getId() : null;
  }
}
