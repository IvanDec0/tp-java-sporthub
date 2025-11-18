package com.java.sportshub.services;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.java.sportshub.daos.CartDAO;
import com.java.sportshub.daos.CartItemDAO;
import com.java.sportshub.daos.InventoryDAO;
import com.java.sportshub.exceptions.ResourceNotFoundException;
import com.java.sportshub.exceptions.ValidationException;
import com.java.sportshub.models.Cart;
import com.java.sportshub.models.CartItem;
import com.java.sportshub.models.Inventory;
import com.java.sportshub.models.Store;
import com.java.sportshub.models.User;

@ExtendWith(MockitoExtension.class)
public class CartItemServiceTest {

    @Mock
    private CartItemDAO cartItemDAO;

    @Mock
    private InventoryDAO inventoryDAO;

    @Mock
    private CartDAO cartDAO;
    @Mock
    private CartService cartService;

    @Mock
    private RentalService rentalService;

    @InjectMocks
    private CartItemService cartItemService;

    private Inventory inventory;
    private Cart cart;

    @BeforeEach
    public void setUp() {
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setPrice(10.0);
        inventory.setTipo("venta");

        cart = new Cart();
        cart.setId(2L);
    }

    @Test
    public void createCartItem_withoutInventoryId_throwsValidation() {
        CartItem ci = new CartItem();
        ci.setQuantity(1);
        ci.setCart(cart);

        assertThrows(ValidationException.class, () -> cartItemService.createCartItem(ci));
    }

    @Test
    public void createCartItem_withoutCartId_throwsValidation() {
        CartItem ci = new CartItem();
        ci.setQuantity(2);
        Inventory inv = new Inventory();
        inv.setId(1L);
        Store s = new Store();
        s.setId(10L);
        inv.setStore(s);
        ci.setInventory(inv);
        Cart partialCart = new Cart();
        User user = new User();
        user.setId(3L);
        partialCart.setUser(user);
        ci.setCart(partialCart);

        when(inventoryDAO.findById(1L)).thenReturn(Optional.of(inv));
        when(cartDAO.findActiveCartByUserId(3L)).thenReturn(Optional.empty());
        Cart created = new Cart();
        created.setId(7L);
        when(cartService.createCart(any(), any(Long.class), any(Long.class))).thenReturn(created);
        when(cartItemDAO.save(any())).thenAnswer(i -> i.getArgument(0));

        CartItem saved = cartItemService.createCartItem(ci);
        assertEquals(7L, saved.getCart().getId());
    }

    @Test
    public void createCartItem_inventoryNotFound_throwsResourceNotFound() {
        CartItem ci = new CartItem();
        ci.setQuantity(1);
        Inventory inv = new Inventory();
        inv.setId(999L);
        ci.setInventory(inv);
        Cart c = new Cart();
        c.setId(2L);
        ci.setCart(c);

        when(inventoryDAO.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> cartItemService.createCartItem(ci));
    }

    @Test
    public void createCartItem_success_saleSubtotalCalculated() {
        CartItem ci = new CartItem();
        ci.setQuantity(3);
        Inventory inv = new Inventory();
        inv.setId(1L);
        inv.setPrice(15.0);
        inv.setTipo("venta");
        ci.setInventory(inv);
        Cart c = new Cart();
        c.setId(2L);
        ci.setCart(c);

        when(inventoryDAO.findById(1L)).thenReturn(Optional.of(inv));
        when(cartDAO.findById(2L)).thenReturn(Optional.of(c));
        when(cartItemDAO.save(any())).thenAnswer(i -> i.getArgument(0));

        CartItem saved = cartItemService.createCartItem(ci);
        assertEquals(45.0, saved.getSubtotal());
    }
}
