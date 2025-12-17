package com.java.sportshub.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StockValidationDTO {
    private Long cartId;
    private Boolean isValid;
    private String message;
    private List<ItemValidationResult> itemResults;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ItemValidationResult {
        private Long cartItemId;
        private Long inventoryId;
        private String productName;
        private String tipo; // "Venta" o "Alquiler"
        private Integer requestedQuantity;
        private Integer availableQuantity;
        private Boolean isValid;
        private String message;
        
        // Campos específicos para alquiler
        private LocalDate startDate;
        private LocalDate endDate;
    }

    public static StockValidationDTO success(Long cartId) {
        return StockValidationDTO.builder()
                .cartId(cartId)
                .isValid(true)
                .message("Todos los productos tienen stock disponible")
                .itemResults(new ArrayList<>())
                .build();
    }

    public static StockValidationDTO failure(Long cartId, String message, List<ItemValidationResult> results) {
        return StockValidationDTO.builder()
                .cartId(cartId)
                .isValid(false)
                .message(message)
                .itemResults(results)
                .build();
    }

    public void addItemResult(ItemValidationResult result) {
        if (this.itemResults == null) {
            this.itemResults = new ArrayList<>();
        }
        this.itemResults.add(result);
    }
}

