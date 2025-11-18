package com.java.sportshub.services;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.java.sportshub.dtos.StripeChargeDTO;
import com.java.sportshub.dtos.StripePaymentIntentDTO;
import com.java.sportshub.dtos.StripeRefundDTO;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.model.Refund;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.RefundCreateParams;

@Service
public class StripeService {

    @Value("${stripe.currency:usd}")
    private String currency;

    public StripePaymentIntentDTO createPaymentIntent(Long amount, String description, Map<String, String> metadata)
            throws StripeException {

        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency)
                .setDescription(description)
                .putAllMetadata(metadata != null ? metadata : new HashMap<>())
                .setAutomaticPaymentMethods(
                        PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                .setEnabled(true)
                                .build())
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);

        return new StripePaymentIntentDTO(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                paymentIntent.getStatus());
    }

    public StripePaymentIntentDTO getPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);

        return new StripePaymentIntentDTO(
                paymentIntent.getClientSecret(),
                paymentIntent.getId(),
                paymentIntent.getAmount(),
                paymentIntent.getCurrency(),
                paymentIntent.getStatus());
    }

    public StripePaymentIntentDTO confirmPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent confirmedPaymentIntent = paymentIntent.confirm();

        return new StripePaymentIntentDTO(
                confirmedPaymentIntent.getClientSecret(),
                confirmedPaymentIntent.getId(),
                confirmedPaymentIntent.getAmount(),
                confirmedPaymentIntent.getCurrency(),
                confirmedPaymentIntent.getStatus());
    }

    public StripePaymentIntentDTO cancelPaymentIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        PaymentIntent cancelledPaymentIntent = paymentIntent.cancel();

        return new StripePaymentIntentDTO(
                cancelledPaymentIntent.getClientSecret(),
                cancelledPaymentIntent.getId(),
                cancelledPaymentIntent.getAmount(),
                cancelledPaymentIntent.getCurrency(),
                cancelledPaymentIntent.getStatus());
    }

    public StripeChargeDTO getChargeFromPaymentIntent(String paymentIntentId) throws StripeException {
        // Expandir los charges para obtener la informacion completa
        Map<String, Object> params = new HashMap<>();
        params.put("expand", new String[] { "latest_charge" });

        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId, params, null);

        if (paymentIntent.getLatestChargeObject() == null) {
            throw new IllegalStateException("No charges found for this payment intent");
        }

        var charge = paymentIntent.getLatestChargeObject();

        return new StripeChargeDTO(
                charge.getId(),
                charge.getAmount(),
                charge.getCurrency(),
                charge.getStatus(),
                charge.getReceiptUrl());
    }

    public StripeRefundDTO createRefund(String chargeId, Long amount, String reason) throws StripeException {
        RefundCreateParams.Builder paramsBuilder = RefundCreateParams.builder()
                .setCharge(chargeId);

        if (amount != null) {
            paramsBuilder.setAmount(amount);
        }

        if (reason != null && !reason.isBlank()) {
            paramsBuilder.setReason(RefundCreateParams.Reason.REQUESTED_BY_CUSTOMER);
        }

        RefundCreateParams params = paramsBuilder.build();
        Refund refund = Refund.create(params);

        return new StripeRefundDTO(
                refund.getId(),
                refund.getCharge(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getReason());
    }

    public StripeRefundDTO getRefund(String refundId) throws StripeException {
        Refund refund = Refund.retrieve(refundId);

        return new StripeRefundDTO(
                refund.getId(),
                refund.getCharge(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getStatus(),
                refund.getReason());
    }

    public Long convertToCents(Double amount) {
        return Math.round(amount * 100);
    }

    public Double convertFromCents(Long amountInCents) {
        return amountInCents / 100.0;
    }
}
