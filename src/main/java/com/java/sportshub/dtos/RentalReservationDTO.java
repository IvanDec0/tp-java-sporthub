package com.java.sportshub.dtos;

import com.java.sportshub.models.RentalReservation;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RentalReservationDTO {
    private Long id;
    private Long inventoryId;
    private Long cartItemId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer quantity;
    private String status;
    private Double totalPrice;
    private Long userId;

    public RentalReservationDTO() {
    }

    public RentalReservationDTO(RentalReservation reservation) {
        this.id = reservation.getId();
        this.inventoryId = reservation.getInventory() != null ? reservation.getInventory().getId() : null;
        this.cartItemId = reservation.getCartItem() != null ? reservation.getCartItem().getId() : null;
        this.startDate = reservation.getStartDate();
        this.endDate = reservation.getEndDate();
        this.quantity = reservation.getQuantity();
        this.status = reservation.getStatus();
        this.totalPrice = reservation.getTotalPrice();
        this.userId = reservation.getUser() != null ? reservation.getUser().getId() : null;
    }
}
