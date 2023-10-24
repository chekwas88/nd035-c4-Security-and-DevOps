package com.example.demo.controller;

import com.example.demo.TestUtils;
import com.example.demo.controllers.OrderController;
import com.example.demo.model.persistence.Cart;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.User;
import com.example.demo.model.persistence.UserOrder;
import com.example.demo.model.persistence.repositories.CartRepository;
import com.example.demo.model.persistence.repositories.ItemRepository;
import com.example.demo.model.persistence.repositories.OrderRepository;
import com.example.demo.model.persistence.repositories.UserRepository;

import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OrderControllerTest {
    private OrderController orderController;
    private UserRepository userRepository = mock(UserRepository.class);
    private ItemRepository itemRepository = mock(ItemRepository.class);
    private CartRepository cartRepository = mock(CartRepository.class);
    private OrderRepository orderRepository = mock(OrderRepository.class);

    @Before
    public void setUp(){
        orderController = new OrderController();
        TestUtils.injectObjects(orderController, "userRepository", userRepository);
        TestUtils.injectObjects(orderController, "orderRepository", orderRepository);

        User user = new User(0L, "testPassword", "testUser", new Cart());
        Item item0 = new Item(0L, "item0", BigDecimal.valueOf(5), "desc");
        Item item1 = new Item(1L, "item1", BigDecimal.valueOf(10), "desc");
        Cart cart1 = new Cart(0L, Arrays.asList(item0, item1), user, BigDecimal.valueOf(15));
        Cart cart2 = new Cart(1L, Arrays.asList(item0, item1, item1), user, BigDecimal.valueOf(25));
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        when(itemRepository.findById(0L)).thenReturn(Optional.of(item0));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item1));
        when(cartRepository.findById(0L)).thenReturn(Optional.of(cart1));
        when(cartRepository.findById(1L)).thenReturn(Optional.of(cart2));
    }

    @Test
    public void submitAndGetOrders() throws IOException {
        User user = userRepository.findByUsername("testUser");
        user.setCart(cartRepository.findById(0L).get());
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        UserOrder order1 = orderController.submit("testUser").getBody();
        assertTrue(user.getCart().getItems().containsAll(order1.getItems()));
        assertEquals(BigDecimal.valueOf(15), order1.getTotal());

        user.setCart(cartRepository.findById(1L).get());
        when(userRepository.findByUsername("testUser")).thenReturn(user);
        UserOrder order2 = orderController.submit("testUser").getBody();
        assertTrue(user.getCart().getItems().containsAll(order2.getItems()));
        assertEquals(BigDecimal.valueOf(25), order2.getTotal());

        List<UserOrder> orders = Arrays.asList(order1, order2);
        when(orderRepository.findByUser(user)).thenReturn(orders);
        List<UserOrder> responseOrders = orderController.getOrdersForUser("testUser").getBody();
        assertEquals(2, responseOrders.size());
    }

    @Test
    public void testForFailures() throws IOException {
        ResponseEntity<UserOrder> response;
        ResponseEntity<List<UserOrder>> responses;

        response = orderController.submit("wrongUsername");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        responses = orderController.getOrdersForUser("wrongUsername");
        assertEquals(HttpStatus.NOT_FOUND, responses.getStatusCode());
    }
}