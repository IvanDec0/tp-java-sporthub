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
import com.java.sportshub.dtos.StockValidationDTO;
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

  @Autowired
  private StockValidationService stockValidationService;

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
      throw new BusinessRuleException("El carrito debe estar activo para crear un pago");
    }

    if (cart.getItems() == null || cart.getItems().isEmpty()) {
      throw new BusinessRuleException("El carrito debe tener productos para crear un pago");
    }

    // VALIDAR STOCK ANTES DE CREAR EL PAYMENT INTENT
    StockValidationDTO stockValidation = stockValidationService.validateCartStock(cart.getId());
    if (!stockValidation.getIsValid()) {
      throw new BusinessRuleException("Validación de stock fallida: " + stockValidation.getMessage());
    }

    // Recompute cart total considering coupons
    double computedTotal = pricingService.computeCartTotal(cart.getId());
    if (Math.abs(payment.getAmount() - computedTotal) > 0.001) {
      throw new BusinessRuleException(
          String.format("El monto del pago (%.2f) debe coincidir con el total calculado del carrito (%.2f)",
              payment.getAmount(), computedTotal));
    }

    // Persist applied coupons summary for auditing
    try {
      payment.setAppliedCoupons(pricingService.getAppliedCouponsSummary(cart.getId()));
    } catch (Exception ex) {
    }
    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());
    payment.setOrderStatus("Pendiente");
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
      throw new InvalidOperationException("El pago ya ha sido completado");
    }

    if (payment.getPaymentStatus().equals("Refunded")) {
      throw new InvalidOperationException("No se puede procesar un pago que ya fue reembolsado");
    }

    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());

    if (requiresStripeProcessing) {
      if (payment.getStripePaymentIntentId() == null) {
        throw new BusinessRuleException("Falta el ID de Payment Intent para el pago con tarjeta");
      }

      StripePaymentIntentDTO paymentIntent = stripeService.getPaymentIntent(payment.getStripePaymentIntentId());

      if (!"succeeded".equals(paymentIntent.getStatus())) {
        throw new BusinessRuleException(
            "El pago no ha sido completado en Stripe. Estado actual: " + paymentIntent.getStatus());
      }

      StripeChargeDTO charge = stripeService.getChargeFromPaymentIntent(payment.getStripePaymentIntentId());
      payment.setStripeChargeId(charge.getChargeId());
    }

    Cart cart = payment.getCart();
    if (cart != null && cart.getItems() != null) {
      // Re-validar stock antes de procesar (por si cambió entre creación y
      // confirmación)
      StockValidationDTO stockValidation = stockValidationService.validateCartStock(cart.getId());
      if (!stockValidation.getIsValid()) {
        throw new BusinessRuleException("Validación de stock fallida: " + stockValidation.getMessage());
      }

      for (CartItem item : cart.getItems()) {
        Inventory inventory = inventoryDAO.findById(item.getInventory().getId())
            .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", item.getInventory().getId()));

        if ("venta".equalsIgnoreCase(inventory.getTipo())) {
          // Para VENTA: descontar stock físico
          int requestedQty = item.getQuantity();
          int availableStock = inventory.getQuantity();

          if (availableStock < requestedQty) {
            throw new BusinessRuleException(
                "Stock insuficiente para el producto: " + inventory.getProduct().getName() +
                    ". Disponible: " + availableStock + ", Solicitado: " + requestedQty);
          }

          inventory.setQuantity(availableStock - requestedQty);
          inventoryDAO.save(inventory);
        } else if ("alquiler".equalsIgnoreCase(inventory.getTipo())) {
          // Para ALQUILER: crear RentalReservation confirmada
          createRentalReservationForCartItem(item, inventory, payment.getUser());
        }
      }
    }

    payment.setPaymentStatus("Completed");
    payment.setOrderStatus("Recibido");
    payment.setPaymentDate(LocalDateTime.now());

    paymentDAO.save(payment);

    // Completar el carrito después del pago exitoso
    if (cart != null) {
      cart.setStatus("Completed");
      cartDAO.save(cart);
    }

    return payment;
  }

  /**
   * Crea una reserva de alquiler para un item del carrito después del pago
   * exitoso.
   */
  private void createRentalReservationForCartItem(CartItem item, Inventory inventory,
      com.java.sportshub.models.User user) {
    RentalReservation reservation = new RentalReservation();
    reservation.setInventory(inventory);
    reservation.setCartItem(item);
    reservation.setStartDate(item.getStartDate());
    reservation.setEndDate(item.getEstimatedEndDate());
    reservation.setQuantity(item.getQuantity());
    reservation.setStatus("CONFIRMED"); // Confirmada porque el pago ya se completó
    reservation.setTotalPrice(item.getSubtotal());
    reservation.setUser(user);
    reservation.setIsActive(true);

    rentalReservationDAO.save(reservation);
  }

  public boolean isPaymentCompleted(Long cartId) {
    Payment payment = paymentDAO.findByCartId(cartId);
    return payment != null && "Completed".equals(payment.getPaymentStatus());
  }

  public void validateCompletedPayment(Long cartId) {
    Payment payment = paymentDAO.findByCartId(cartId);

    if (payment == null) {
      throw new BusinessRuleException("No se encontró pago para el carrito #" + cartId);
    }

    if (!"Completed".equals(payment.getPaymentStatus())) {
      throw new BusinessRuleException(
          "El pago debe ser completado antes de proceder. Estado actual: " + payment.getPaymentStatus());
    }

    if (isStripePaymentMethod(payment.getPaymentMethod()) && payment.getStripeChargeId() == null) {
      throw new BusinessRuleException("El pago está marcado como completado pero no tiene ID de cargo en Stripe");
    }
  }

  @Transactional
  public Payment updatePayment(Long id, Payment paymentDetails) {
    Payment payment = getPaymentById(id);

    if (payment.getPaymentStatus().equals("Completed")) {
      throw new InvalidOperationException("No se puede actualizar un pago completado");
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
      throw new InvalidOperationException("Solo se pueden reembolsar pagos completados");
    }

    if (payment.getPaymentStatus().equals("Refunded")) {
      throw new InvalidOperationException("El pago ya ha sido reembolsado");
    }

    boolean requiresStripeProcessing = isStripePaymentMethod(payment.getPaymentMethod());

    if (requiresStripeProcessing) {
      if (payment.getStripeChargeId() == null) {
        throw new BusinessRuleException("No se puede reembolsar: falta el ID de cargo de Stripe");
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

  @Transactional
  public Payment updateOrderStatus(Long id, String newStatus) {
    Payment payment = getPaymentById(id);
    payment.setOrderStatus(newStatus);
    return paymentDAO.save(payment);
  }

  private String generateTransactionId() {
    return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
  }

  public boolean isStripePaymentMethod(String paymentMethod) {
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
