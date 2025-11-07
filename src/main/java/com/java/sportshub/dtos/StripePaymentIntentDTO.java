package com.java.sportshub.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripePaymentIntentDTO {
    private String clientSecret;
    private String paymentIntentId;
    private Long amount; // Amount
    private String currency;
    private String status;
}
