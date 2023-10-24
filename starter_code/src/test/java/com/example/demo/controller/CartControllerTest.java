package com.example.demo.controller;


import com.example.demo.TestUtils;
import com.example.demo.controllers.CartController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.UserRepository;
import com.example.demo.model.requests.ModifyCartRequest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CartControllerTest {
    private CartController cartController;
    private UserRepository userRepository = mock(UserRepository.class);
    private ItemRepository itemRepository = mock(ItemRepository.class);
    private CartRepository cartRepository = mock(CartRepository.class);

    @Before
    public void setUp(){
        cartController = new CartController();
        TestUtils.injectObjects(cartController, "userRepository", userRepository);
        TestUtils.injectObjects(cartController, "cartRepository", cartRepository);
        TestUtils.injectObjects(cartController, "itemRepository", itemRepository);

        User user = new User(1L, "testPassword", "testUser", new Cart());
        when(userRepository.findByUsername("testUser")).thenReturn(user);

        Item item1 = new Item(1L, "item1", BigDecimal.valueOf(5), "desc");
        Item item2 = new Item(2L, "item2", BigDecimal.valueOf(10), "desc");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item2));
    }

    @Test
    public void addAndRemoveFromCart() throws IOException {
        Cart cart;
        ModifyCartRequest request = new ModifyCartRequest("testUser", 1L, 1);
        cart = cartController.addTocart(request).getBody();
        assertEquals(BigDecimal.valueOf(5), cart.getTotal());

        request.setQuantity(2);
        cart = cartController.addTocart(request).getBody();
        assertEquals(BigDecimal.valueOf(15), cart.getTotal());

        request.setItemId(2L);
        cart = cartController.addTocart(request).getBody();
        assertEquals(BigDecimal.valueOf(35), cart.getTotal());

        cart = cartController.removeFromcart(request).getBody();
        assertEquals(BigDecimal.valueOf(15), cart.getTotal());

        request.setQuantity(1);
        request.setItemId(1L);
        cart = cartController.removeFromcart(request).getBody();
        assertEquals(BigDecimal.valueOf(10), cart.getTotal());
    }

    @Test
    public void testForFailures() throws IOException {
        ResponseEntity<Cart> response;
        ModifyCartRequest request;

        request = new ModifyCartRequest();
        response = cartController.addTocart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        request.setUsername("testUser");
        response = cartController.addTocart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        request = new ModifyCartRequest();
        response = cartController.removeFromcart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        request.setUsername("testUser");
        response = cartController.removeFromcart(request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}