package com.java.sportshub.services;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.daos.PaymentDAO;
import com.java.sportshub.daos.RentalReservationDAO;
import com.java.sportshub.dtos.StripeChargeDTO;
import com.java.sportshub.dtos.StripePaymentIntentDTO;
import com.java.sportshub.dtos.StripeRefundDTO;
import com.java.sportshub.exceptions.BusinessRuleException;
import com.java.sportshub.exceptions.InvalidOperationException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Payment;
import com.java.sportshub.models.RentalReservation;
import com.stripe.exception.StripeException;

@Service
public class PaymentService {

  @Autowired
  private PaymentDAO paymentDAO;

  @Autowired
  private CartDAO cartDAO;

  @Autowired
  private CartItemDAO cartItemDAO;

  @Autowired
  private InventoryDAO inventoryDAO;

  @Autowired
  private RentalReservationDAO rentalReservationDAO;

  @Autowired
  private PricingService pricingService;

  @Autowired
  private StripeService stripeService;

  @Autowired
  private EmailService emailService;

  public List<Payment> getAllPayments() {
    return paymentDAO.findAll();
  }

  public Payment getPaymentById(Long id) {
    return paymentDAO.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", id));
  }

  public List<Payment> getPaymentsByUserId(Long userId) {
    return paymentDAO.findByUserId(userId);
  }

  public List<Payment> getPaymentsByStatus(String status) {
    return paymentDAO.findByPaymentStatus(status);
  }

  public Payment getPaymentByCartId(Long cartId) {
    return paymentDAO.findByCartId(cartId);
  }

  @Transactional
  public Payment createPayment(Payment payment) throws StripeException {
    // Validar que el carrito exista y esté activo
    Cart cart = cartDAO.findById(payment.getCart().getId())
        .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", payment.getCart().getId()));

    if (!cart.getStatus().equals("Active")) {
      throw new BusinessRuleException("Cart must be active to create a payment");
    }

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new BusinessRuleException("Cart must have items to create a payment");
    }

    // Recompute cart total considering coupons
    double computedTotal = pricingService.computeCartTotal(cart.getId());
    if (Math.abs(payment.getAmount() - computedTotal) > 0.001) {
      throw new BusinessRuleException(
          String.format("Payment amount (%.2f) must match computed cart total (%.2f)",
              payment.getAmount(), computedTotal));
    }

    // Persist applied coupons summary for auditing
    try {
      payment.setAppliedCoupons(pricingService.getAppliedCouponsSummary(cart.getId()));
    } catch (Exception ex) {
    }
    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());

    payment.setTransactionId(generateTransactionId());
    payment.setPaymentDate(LocalDateTime.now());
    payment.setPaymentStatus("Pending");

    if (requiresStripeProcessing) {
      Map<String, String> metadata = new HashMap<>();
      metadata.put("cart_id", cart.getId().toString());
      metadata.put("user_id", cart.getUser().getId().toString());
      metadata.put("transaction_id", payment.getTransactionId());

      Long amountInCents = stripeService.convertToCents(payment.getAmount());
      String description = "Payment for Cart #" + cart.getId();

      StripePaymentIntentDTO paymentIntent = stripeService.createPaymentIntent(
          amountInCents,
          description,
          metadata);

      payment.setStripePaymentIntentId(paymentIntent.getPaymentIntentId());
      payment.setNotes("Stripe Payment Intent created: " + paymentIntent.getClientSecret());
    }

    paymentDAO.save(payment);
    return payment;
  }

  @Transactional
  public Payment processPayment(Long paymentId) throws StripeException {
    Payment payment = getPaymentById(paymentId);

    if (payment.getPaymentStatus().equals("Completed")) {
      throw new InvalidOperationException("Payment has already been completed");
    }

    if (payment.getPaymentStatus().equals("Refunded")) {
      throw new InvalidOperationException("Cannot process a refunded payment");
    }

    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());

    if (requiresStripeProcessing) {
      if (payment.getStripePaymentIntentId() == null) {
        throw new BusinessRuleException("Payment Intent ID is missing for card payment");
      }

      // Obtener el estado del payment intent desde Stripe
      StripePaymentIntentDTO paymentIntent = stripeService.getPaymentIntent(payment.getStripePaymentIntentId());

      if (!"succeeded".equals(paymentIntent.getStatus())) {
        throw new BusinessRuleException(
            "Payment has not been completed in Stripe. Current status: " + paymentIntent.getStatus());
      }

      StripeChargeDTO charge = stripeService.getChargeFromPaymentIntent(payment.getStripePaymentIntentId());
      payment.setStripeChargeId(charge.getChargeId());
    }

    payment.setPaymentStatus("Completed");
    payment.setPaymentDate(LocalDateTime.now());

    paymentDAO.save(payment);
    return payment;
  }

  public boolean isPaymentCompleted(Long cartId) {
    Payment payment = paymentDAO.findByCartId(cartId);
    return payment != null && "Completed".equals(payment.getPaymentStatus());
  }

  public void validateCompletedPayment(Long cartId) {
    Payment payment = paymentDAO.findByCartId(cartId);

    if (payment == null) {
      throw new BusinessRuleException("No payment found for cart #" + cartId);
    }

    if (!"Completed".equals(payment.getPaymentStatus())) {
      throw new BusinessRuleException(
          "Payment must be completed before proceeding. Current status: " + payment.getPaymentStatus());
    }

    if (isStripePaymentMethod(payment.getPaymentMethod()) && payment.getStripeChargeId() == null) {
      throw new BusinessRuleException("Payment is marked as completed but has no Stripe charge ID");
    }
  }

  @Transactional
  public Payment updatePayment(Long id, Payment paymentDetails) {
    Payment payment = getPaymentById(id);

    if (payment.getPaymentStatus().equals("Completed")) {
      throw new InvalidOperationException("Cannot update a completed payment");
    }

    payment.setAmount(paymentDetails.getAmount());
    payment.setPaymentMethod(paymentDetails.getPaymentMethod());
    payment.setPaymentStatus(paymentDetails.getPaymentStatus());
    payment.setNotes(paymentDetails.getNotes());

    paymentDAO.save(payment);
    return payment;
  }

  @Transactional
  public Payment refundPayment(Long id) throws StripeException {
    Payment payment = getPaymentById(id);

    if (!payment.getPaymentStatus().equals("Completed")) {
      throw new InvalidOperationException("Can only refund completed payments");
    }

    if (payment.getPaymentStatus().equals("Refunded")) {
      throw new InvalidOperationException("Payment has already been refunded");
    }

    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());

    if (requiresStripeProcessing) {
      if (payment.getStripeChargeId() == null) {
        throw new BusinessRuleException("Cannot refund: Stripe charge ID is missing");
      }

      // Crear reembolso en Stripe
      StripeRefundDTO refund = stripeService.createRefund(
          payment.getStripeChargeId(),
          null, // null significa reembolso completo
          "Customer requested refund");

      payment.setStripeRefundId(refund.getRefundId());
      payment.setNotes("Refunded via Stripe: " + refund.getRefundId());
    }

    if (payment.getUser() == null && payment.getCart() != null && payment.getCart().getUser() != null) {
      payment.setUser(payment.getCart().getUser());
    }

    payment.setPaymentStatus("Refunded");
    paymentDAO.save(payment);

    restoreCartAfterRefund(payment);
    emailService.sendRefundConfirmationEmail(payment);

    return payment;
  }

  @Transactional
  public void handleStripeWebhook(String paymentIntentId, String status) {
    List<Payment> payments = paymentDAO.findAll().stream()
        .filter(p -> paymentIntentId.equals(p.getStripePaymentIntentId()))
        .toList();

    if (payments.isEmpty()) {
      throw new ResourceNotFoundException("Payment", "stripePaymentIntentId", paymentIntentId);
    }

    Payment payment = payments.get(0);

    switch (status) {
      case "succeeded":
        payment.setPaymentStatus("Completed");
        payment.setPaymentDate(LocalDateTime.now());
        break;
      case "processing":
        payment.setPaymentStatus("Processing");
        break;
      case "canceled":
        payment.setPaymentStatus("Cancelled");
        break;
      case "requires_payment_method":
        payment.setPaymentStatus("Failed");
        break;
      default:
        payment.setPaymentStatus("Pending");
    }

    paymentDAO.save(payment);
  }

  private String generateTransactionId() {
    return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  private boolean isStripePaymentMethod(String paymentMethod) {
    return paymentMethod != null &&
        (paymentMethod.equalsIgnoreCase("card") ||
            paymentMethod.equalsIgnoreCase("credit card") ||
            paymentMethod.equalsIgnoreCase("debit card"));
  }

  private void restoreCartAfterRefund(Payment payment) {
    Cart cart = payment.getCart();
    if (cart == null) {
      return;
    }

    List<CartItem> items = cartItemDAO.findActiveItemsByCartId(cart.getId());

    if (items.isEmpty() && cart.getItems() != null) {
      items = cart.getItems();
    }

    for (CartItem item : items) {
      if (item.getInventory() == null || item.getInventory().getId() == null) {
        continue;
      }

      Inventory inventory = inventoryDAO.findById(item.getInventory().getId())
          .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", item.getInventory().getId()));

      if ("venta".equalsIgnoreCase(inventory.getTipo())) {
        inventory.setQuantity(inventory.getQuantity() + item.getQuantity());
        inventoryDAO.save(inventory);
      } else if ("alquiler".equalsIgnoreCase(inventory.getTipo())) {
        cancelRentalReservations(item);
      }
    }

    cart.setStatus("Refunded");
    cartDAO.save(cart);
  }

  private void cancelRentalReservations(CartItem cartItem) {
    List<RentalReservation> reservations = rentalReservationDAO.findByCartItemIdAndIsActiveTrue(cartItem.getId());

    for (RentalReservation reservation : reservations) {
      reservation.setStatus("CANCELLED");
      reservation.setIsActive(false);
      rentalReservationDAO.save(reservation);
    }
  }
}
