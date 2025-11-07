package com.java.sportshub.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.daos.StoreDAO;
import com.java.sportshub.daos.UserDAO;
import com.java.sportshub.exceptions.InsufficientStockException;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Store;
import com.java.sportshub.models.User;

@Service
public class CartService {

    @Autowired
    private CartDAO cartDAO;

    @Autowired
    private CartItemDAO cartItemDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private StoreDAO storeDAO;

    @Autowired
    private InventoryDAO inventoryDAO;

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private com.java.sportshub.daos.CouponDAO couponDAO;

    @Autowired
    private PricingService pricingService;

    public List<Cart> getAllCarts() {
        return cartDAO.findAll();
    }

    public Cart getCartById(Long id) {
        return cartDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", id));
    }

    public List<Cart> getCartsByUserId(Long userId) {
        return cartDAO.findByUserId(userId);
    }

    public Cart getActiveCartByUserId(Long userId) {
        return cartDAO.findActiveCartByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Active Cart", "userId", userId));
    }

    @Transactional
    public Cart createCart(Cart cart, Long userId, Long storeId) {
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Store store = storeDAO.findById(storeId)
                .orElseThrow(() -> new ResourceNotFoundException("Store", "id", storeId));

        Optional<Cart> activeCart = cartDAO.findActiveCartByUserId(userId);
        if (activeCart.isPresent()) {
            throw new IllegalArgumentException("User already has an active cart");
        }

        cart.setUser(user);
        cart.setStore(store);
        cart.setStatus("Active");
        cart.setTotalAmount(0.0);
        cartDAO.save(cart);
        return cart;
    }

    @Transactional
    public Cart updateCart(Long id, Cart cartDetails) {
        Cart cart = getCartById(id);

        if (cartDetails.getStatus() != null) {
            cart.setStatus(cartDetails.getStatus());
        }

        if (cartDetails.getStore() != null) {
            cart.setStore(cartDetails.getStore());
        }

        calculateTotal(cart);
        cartDAO.save(cart);
        return cart;
    }

    @Transactional
    public Cart completeCart(Long id) {
        Cart cart = getCartById(id);

        List<CartItem> items = cartItemDAO.findActiveItemsByCartId(cart.getId());
        if (items.isEmpty()) {
            throw new IllegalArgumentException("Cart has no items");
        }

        // Validar que exista un pago completado asociado a este carrito
        paymentService.validateCompletedPayment(cart.getId());

        List<Inventory> inventoriesToUpdate = new ArrayList<>();
        for (CartItem item : items) {
            Inventory inventory = inventoryDAO.findById(item.getInventory().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory", "id", item.getInventory().getId()));
            if (inventory.getQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(
                        inventory.getProduct().getName(),
                        item.getQuantity().longValue(),
                        inventory.getQuantity().longValue());
            }
            inventoriesToUpdate.add(inventory);
        }

        for (int i = 0; i < items.size(); i++) {
            CartItem item = items.get(i);
            Inventory inventory = inventoriesToUpdate.get(i);
            inventory.setQuantity(inventory.getQuantity() - item.getQuantity());
            inventoryDAO.save(inventory);
        }

        cart.setStatus("Completed");
        cartDAO.save(cart);

        // TODO: Enviar email de confirmación

        return cart;
    }

    @Transactional
    public Cart applyCartCoupon(Long cartId, String code) {
        Cart cart = getCartById(cartId);
        com.java.sportshub.models.Coupon coupon = couponDAO.findByCode(code)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon", "code", code));

        if (coupon.getIsActive() == null || !coupon.getIsActive()) {
            throw new IllegalArgumentException("Coupon is not active");
        }
        if (coupon.getExpiryDate() != null && coupon.getExpiryDate().toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Coupon has expired");
        }

        cart.setAppliedCoupon(coupon);
        cartDAO.save(cart);

        // recompute totals
        pricingService.computeCartTotal(cart.getId());
        return cart;
    }

    @Transactional
    public Cart removeCartCoupon(Long cartId) {
        Cart cart = getCartById(cartId);
        cart.setAppliedCoupon(null);
        cartDAO.save(cart);
        pricingService.computeCartTotal(cart.getId());
        return cart;
    }

    @Transactional
    public Cart deleteCart(Long id) {
        Cart cart = getCartById(id);
        cart.setIsActive(false);
        cart.setStatus("Cancelled");
        cartDAO.save(cart);
        return cart;
    }

    private void calculateTotal(Cart cart) {
        // TODO: Sumar todos los subtotales de los CartItems activos
        List<CartItem> items = cartItemDAO.findActiveItemsByCartId(cart.getId());
        double total = items.stream()
                .mapToDouble(item -> item.getSubtotal() != null ? item.getSubtotal() : 0.0)
                .sum();
        cart.setTotalAmount(total);
    }
}
