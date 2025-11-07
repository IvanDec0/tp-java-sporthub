package com.java.sportshub.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StripeRefundDTO {
    private String refundId;
    private String chargeId;
    private Long amount;
    private String currency;
    private String status;
    private String reason;
}
