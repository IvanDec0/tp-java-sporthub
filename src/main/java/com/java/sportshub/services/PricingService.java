package com.java.sportshub.services;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.CouponDAO;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Coupon;
import com.java.sportshub.models.Inventory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class PricingService {

    @Autowired
    private CartDAO cartDAO;

    @Autowired
    private CartItemDAO cartItemDAO;


    @Transactional
    public double computeCartTotal(Long cartId) {
        Cart cart = cartDAO.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        List<CartItem> items = cartItemDAO.findActiveItemsByCartId(cart.getId());

        double total = 0.0;

        for (CartItem item : items) {
            Inventory inv = item.getInventory();
            double base = 0.0;
            if (inv.getTipo() != null && inv.getTipo().equalsIgnoreCase("Alquiler")) {
                // compute days
                LocalDate start = item.getStartDate();
                LocalDate end = item.getEndDate() != null ? item.getEndDate() : item.getEstimatedEndDate();
                long days = 1;
                if (start != null && end != null) {
                    days = Math.max(1, java.time.temporal.ChronoUnit.DAYS.between(start, end));
                }
                double pricePerDay = inv.getPricePerDay() != null ? inv.getPricePerDay() : inv.getPrice();
                base = pricePerDay * days * item.getQuantity();
            } else {
                base = inv.getPrice() * item.getQuantity();
            }

            // pick best coupon attached to product
            double bestDiscount = 0.0;
            if (inv.getProduct() != null && inv.getProduct().getCoupons() != null) {
                for (Coupon coupon : inv.getProduct().getCoupons()) {
                    if (coupon == null || coupon.getIsActive() == null || !coupon.getIsActive()) continue;
                    if (coupon.getExpiryDate() != null && coupon.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) continue;

                    double discount = 0.0;
                    if (coupon.getDiscountPercent() != null && coupon.getDiscountPercent() > 0) {
                        discount = base * (coupon.getDiscountPercent() / 100.0);
                    }
                    if (discount > bestDiscount) bestDiscount = discount;
                }
            }

            double subtotal = base - bestDiscount;
            if (subtotal < 0) subtotal = 0;
            item.setSubtotal(subtotal);
            total += subtotal;
        }

        // apply cart-level coupon if present
        Coupon cartCoupon = cart.getAppliedCoupon();
        if (cartCoupon != null && cartCoupon.getIsActive() != null && cartCoupon.getIsActive()) {
            if (cartCoupon.getExpiryDate() == null || !cartCoupon.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) {
                double cartDiscount = 0.0;
                if (cartCoupon.getDiscountPercent() != null && cartCoupon.getDiscountPercent() > 0) {
                    cartDiscount = total * (cartCoupon.getDiscountPercent() / 100.0);
                }
                total -= cartDiscount;
                if (total < 0) total = 0;
            }
        }

        cart.setTotalAmount(total);
        cartDAO.save(cart);

        // persist item subtotals
        for (CartItem item : items) {
            cartItemDAO.save(item);
        }

        return total;
    }

    /**
     * Return a simple summary of applied coupons and their discount amounts for auditing.
     */
    public String getAppliedCouponsSummary(Long cartId) {
        Cart cart = cartDAO.findById(cartId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart", "id", cartId));

        List<CartItem> items = cartItemDAO.findActiveItemsByCartId(cart.getId());

        StringBuilder sb = new StringBuilder();

        for (CartItem item : items) {
            Inventory inv = item.getInventory();
            double base = inv.getPrice() * item.getQuantity();
            Coupon best = null;
            double bestDiscount = 0.0;
            if (inv.getProduct() != null && inv.getProduct().getCoupons() != null) {
                for (Coupon coupon : inv.getProduct().getCoupons()) {
                    if (coupon == null || coupon.getIsActive() == null || !coupon.getIsActive()) continue;
                    if (coupon.getExpiryDate() != null && coupon.getExpiryDate().toLocalDate().isBefore(LocalDate.now())) continue;
                    double discount = 0.0;
                    if (coupon.getDiscountPercent() != null && coupon.getDiscountPercent() > 0) {
                        discount = base * (coupon.getDiscountPercent() / 100.0);
                    }
                    if (discount > bestDiscount) { bestDiscount = discount; best = coupon; }
                }
            }
            if (best != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(best.getCode()).append(":").append(String.format("%.2f", bestDiscount));
            }
        }

        Coupon cartCoupon = cart.getAppliedCoupon();
        if (cartCoupon != null && cartCoupon.getIsActive() != null && cartCoupon.getIsActive()) {
            // approximate by computing discount on current cart.totalAmount
            double total = cart.getTotalAmount() != null ? cart.getTotalAmount() : 0.0;
            double cartDiscount = 0.0;
            if (cartCoupon.getDiscountPercent() != null && cartCoupon.getDiscountPercent() > 0) {
                cartDiscount = total * (cartCoupon.getDiscountPercent() / 100.0);
            }
            if (cartDiscount > 0) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(cartCoupon.getCode()).append(":").append(String.format("%.2f", cartDiscount));
            }
        }

        return sb.toString();
    }
}
