package com.siemens.internship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the ItemService class.
 * Tests cover basic CRUD operations and asynchronous processing functionality.
 */

@ExtendWith(MockitoExtension.class)
public class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;
    private List<Long> testItemIds;

    @BeforeEach
    void setUp(){
        // Create test data
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setDescription("This Description");
        testItem.setStatus("New");
        testItem.setEmail("test@example.com");

        testItemIds = Arrays.asList(1L, 2L, 3L);
    }

    @Test
    void findAll_ShouldReturnAllItems() {
        // Arrange
        when(itemRepository.findAll()).thenReturn(Collections.singletonList(testItem));

        // Act
        List<Item> result = itemService.findAll();

        // Assert
        assertEquals(1, result.size());
        assertEquals(testItem.getId(), result.get(0).getId());
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void findById_WithExistingId_ShouldReturnItem() {
        // Arrange
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));

        // Act
        Optional<Item> result = itemService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testItem.getId(), result.get().getId());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void findById_WithNonExistingId_ShouldReturnEmptyOptional() {
        // Arrange
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        // Act
        Optional<Item> result = itemService.findById(99L);

        // Assert
        assertFalse(result.isPresent());
        verify(itemRepository, times(1)).findById(99L);
    }

    @Test
    void save_ShouldSaveAndReturnItem() {
        // Arrange
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        // Act
        Item result = itemService.save(testItem);

        // Assert
        assertNotNull(result);
        assertEquals(testItem.getId(), result.getId());
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void deleteById_ShouldCallRepositoryDeleteById() {
        // Arrange
        doNothing().when(itemRepository).deleteById(1L);

        // Act
        itemService.deleteById(1L);

        // Assert
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void processItemsAsync_ShouldProcessAllItems() {
        // Arrange
        when(itemRepository.findAllIds()).thenReturn(testItemIds);

        for (Long id : testItemIds) {
            Item item = new Item();
            item.setId(id);
            item.setStatus("NEW");

            when(itemRepository.findById(id)).thenReturn(Optional.of(item));

            Item processedItem = new Item();
            processedItem.setId(id);
            processedItem.setStatus("PROCESSED");

            when(itemRepository.save(any(Item.class))).thenReturn(processedItem);
        }

        // Act
        List<Item> result = itemService.processItemsAsync();

        // Assert - we need to allow time for async processing
        // In a real test we might use awaitility or other mechanisms
        // For this example we'll use a simple approach
        assertNotNull(result);
        verify(itemRepository, times(1)).findAllIds();

        // We should have called findById for each item ID
        for (Long id : testItemIds) {
            verify(itemRepository, timeout(5000).atLeastOnce()).findById(id);
        }
    }
}
