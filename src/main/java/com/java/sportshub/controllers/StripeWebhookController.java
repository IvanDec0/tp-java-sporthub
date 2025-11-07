package com.java.sportshub.controllers;

import com.java.sportshub.services.PaymentService;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/webhooks/stripe")
public class StripeWebhookController {

    @Autowired
    private PaymentService paymentService;

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping
    public ResponseEntity<?> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Verificar la firma del webhook para asegurar que viene de Stripe
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // Firma inválida
            Map<String, String> error = new HashMap<>();
            error.put("error", "Invalid webhook signature");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Webhook error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }

        // Manejar el evento
        try {
            switch (event.getType()) {
                case "payment_intent.succeeded":
                    handlePaymentIntentSucceeded(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentIntentFailed(event);
                    break;

                case "payment_intent.canceled":
                    handlePaymentIntentCanceled(event);
                    break;

                case "payment_intent.processing":
                    handlePaymentIntentProcessing(event);
                    break;

                case "charge.refunded":
                    handleChargeRefunded(event);
                    break;

                default:
                    // Tipo de evento no manejado
                    Map<String, String> info = new HashMap<>();
                    info.put("message", "Unhandled event type: " + event.getType());
                    return ResponseEntity.ok(info);
            }

            Map<String, String> response = new HashMap<>();
            response.put("message", "Webhook handled successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error processing webhook: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    private void handlePaymentIntentSucceeded(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found in event"));

        paymentService.handleStripeWebhook(paymentIntent.getId(), "succeeded");
    }

    private void handlePaymentIntentFailed(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found in event"));

        paymentService.handleStripeWebhook(paymentIntent.getId(), "requires_payment_method");
    }

    private void handlePaymentIntentCanceled(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found in event"));

        paymentService.handleStripeWebhook(paymentIntent.getId(), "canceled");
    }

    private void handlePaymentIntentProcessing(Event event) {
        PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                .getObject()
                .orElseThrow(() -> new IllegalStateException("PaymentIntent not found in event"));

        paymentService.handleStripeWebhook(paymentIntent.getId(), "processing");
    }

    private void handleChargeRefunded(Event event) {

    }
}
