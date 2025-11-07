package com.java.sportshub.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class RentalAvailabilityDTO {
    private Long inventoryId;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer requestedQuantity;
    private Long availableQuantity;
    private Boolean isAvailable;
    private String message;
}
