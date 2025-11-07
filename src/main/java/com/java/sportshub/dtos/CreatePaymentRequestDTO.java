package com.java.sportshub.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequestDTO {
    private Long cartId;
    private String paymentMethod; // "card", "cash", etc.
    private String description;
}
