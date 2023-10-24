package com.example.demo.controller;

import com.example.demo.TestUtils;
import com.example.demo.controllers.ItemController;
import com.example.demo.model.persistence.Item;
import com.example.demo.model.persistence.repositories.ItemRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.junit.Assert.assertEquals;

public class ItemControllerTest {
    private ItemController itemController;
    private ItemRepository itemRepository = mock(ItemRepository.class);

    @Before
    public void setUp() throws IOException {
        itemController = new ItemController();
        TestUtils.injectObjects(itemController, "itemRepository", itemRepository);
    }

    @Test
    public void testGetItemByName() {
        List<Item> itemList = createItemList();
        // failure
        when(itemRepository.findByName("name")).thenReturn(null);
        ResponseEntity<List<Item>> responseEntity = itemController.getItemsByName("name");
        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());

        // success
        when(itemRepository.findByName("PS5")).thenReturn(itemList);
        responseEntity = itemController.getItemsByName("PS5");

        List<Item> itemListRetrieved = responseEntity.getBody();
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("PS5", itemListRetrieved.get(0).getName());
    }

    private List<Item> createItemList() {
        Item item = new Item();
        item.setName("PS5");

        return Collections.singletonList(item);
    }
}