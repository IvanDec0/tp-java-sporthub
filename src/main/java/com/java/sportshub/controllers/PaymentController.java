package com.java.sportshub.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.java.sportshub.dtos.CreatePaymentRequestDTO;
import com.java.sportshub.dtos.PaymentDTO;
import com.java.sportshub.dtos.StripePaymentIntentDTO;
import com.java.sportshub.mappers.PaymentMapper;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.Payment;
import com.java.sportshub.services.CartService;
import com.java.sportshub.services.PaymentService;
import com.java.sportshub.services.StripeService;
import com.stripe.exception.StripeException;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

  @Autowired
  private PaymentService paymentService;

  @Autowired
  private CartService cartService;

  @Autowired
  private StripeService stripeService;

  @GetMapping("")
  public ResponseEntity<List<PaymentDTO>> getAllPayments() {
    List<Payment> payments = paymentService.getAllPayments();
    List<PaymentDTO> paymentDTOs = payments.stream()
        .map(PaymentMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(paymentDTOs);
  }

  @GetMapping("/{id}")
  public ResponseEntity<PaymentDTO> getPaymentById(@PathVariable("id") Long id) {
    Payment payment = paymentService.getPaymentById(id);
    return ResponseEntity.ok(PaymentMapper.toDTO(payment));
  }

  @GetMapping("/{id}/client-secret")
  public ResponseEntity<?> getPaymentClientSecret(@PathVariable("id") Long id) {
    try {
      Payment payment = paymentService.getPaymentById(id);
      Map<String, String> response = new HashMap<>();
      if (payment.getStripePaymentIntentId() != null) {
        StripePaymentIntentDTO paymentIntent = stripeService.getPaymentIntent(payment.getStripePaymentIntentId());
        response.put("clientSecret", paymentIntent.getClientSecret());
      } else {
        response.put("clientSecret", null);
      }
      return ResponseEntity.ok(response);
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @GetMapping("/user/{userId}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByUserId(@PathVariable("userId") Long userId) {
    List<Payment> payments = paymentService.getPaymentsByUserId(userId);
    List<PaymentDTO> paymentDTOs = payments.stream()
        .map(PaymentMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(paymentDTOs);
  }

  @GetMapping("/status/{status}")
  public ResponseEntity<List<PaymentDTO>> getPaymentsByStatus(@PathVariable("status") String status) {
    List<Payment> payments = paymentService.getPaymentsByStatus(status);
    List<PaymentDTO> paymentDTOs = payments.stream()
        .map(PaymentMapper::toDTO)
        .collect(Collectors.toList());
    return ResponseEntity.ok(paymentDTOs);
  }

  @GetMapping("/cart/{cartId}")
  public ResponseEntity<PaymentDTO> getPaymentByCartId(@PathVariable("cartId") Long cartId) {
    Payment payment = paymentService.getPaymentByCartId(cartId);
    return ResponseEntity.ok(PaymentMapper.toDTO(payment));
  }

  @PostMapping("/create")
  public ResponseEntity<?> createPaymentFromRequest(@RequestBody CreatePaymentRequestDTO request) {
    try {
      Cart cart = cartService.getCartById(request.getCartId());

      Payment payment = new Payment();
      payment.setCart(cart);
      payment.setUser(cart.getUser());
      payment.setAmount(cart.getTotalAmount());
      payment.setPaymentMethod(request.getPaymentMethod());
      payment.setNotes(request.getDescription());

      Payment createdPayment = paymentService.createPayment(payment);
      PaymentDTO paymentDTO = PaymentMapper.toDTO(createdPayment);

      // Si es un pago de Stripe, también devolver el client secret
      Map<String, Object> response = new HashMap<>();
      response.put("payment", paymentDTO);

      if (createdPayment.getStripePaymentIntentId() != null) {
        StripePaymentIntentDTO paymentIntent = stripeService.getPaymentIntent(
            createdPayment.getStripePaymentIntentId());
        response.put("clientSecret", paymentIntent.getClientSecret());
      }

      return ResponseEntity.status(HttpStatus.CREATED).body(response);
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @PostMapping
  public ResponseEntity<?> createPayment(@RequestBody PaymentDTO paymentDTO) {
    try {
      Payment payment = PaymentMapper.toEntity(paymentDTO);
      Payment createdPayment = paymentService.createPayment(payment);
      return ResponseEntity.status(HttpStatus.CREATED).body(PaymentMapper.toDTO(createdPayment));
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @PostMapping("/{id}/process")
  public ResponseEntity<?> processPayment(@PathVariable("id") Long id) {
    try {
      Payment payment = paymentService.processPayment(id);
      return ResponseEntity.ok(PaymentMapper.toDTO(payment));
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @GetMapping("/{id}/verify")
  public ResponseEntity<?> verifyPayment(@PathVariable("id") Long id) {
    try {
      Payment payment = paymentService.getPaymentById(id);

      Map<String, Object> response = new HashMap<>();
      response.put("payment", PaymentMapper.toDTO(payment));

      if (payment.getStripePaymentIntentId() != null) {
        StripePaymentIntentDTO paymentIntent = stripeService.getPaymentIntent(
            payment.getStripePaymentIntentId());
        response.put("stripeStatus", paymentIntent.getStatus());
      }

      return ResponseEntity.ok(response);
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }

  @PutMapping("/{id}")
  public ResponseEntity<PaymentDTO> updatePayment(
      @PathVariable("id") Long id,
      @RequestBody PaymentDTO paymentDTO) {
    Payment payment = PaymentMapper.toEntity(paymentDTO);
    Payment updatedPayment = paymentService.updatePayment(id, payment);
    return ResponseEntity.ok(PaymentMapper.toDTO(updatedPayment));
  }

  @PostMapping("/{id}/refund")
  public ResponseEntity<?> refundPayment(@PathVariable("id") Long id) {
    try {
      Payment payment = paymentService.refundPayment(id);
      return ResponseEntity.ok(PaymentMapper.toDTO(payment));
    } catch (StripeException e) {
      Map<String, String> error = new HashMap<>();
      error.put("error", "Stripe error: " + e.getMessage());
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
  }
}
