package com.siemens.internship;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the ItemController using MockMvc.
 * Tests cover all REST endpoints and verify proper status codes and responses.
 */
@WebMvcTest(ItemController.class)
public class ItemControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ItemService itemService;

    @Autowired
    private ObjectMapper objectMapper;

    private Item testItem;

    @BeforeEach
    void setUp() {
        // Create test data
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setStatus(ItemStatus.NEW.name());
        testItem.setEmail("test@example.com");
    }

    @Test
    void getAllItems_ShouldReturnItems() throws Exception {
        // Arrange
        when(itemService.findAll()).thenReturn(Collections.singletonList(testItem));

        // Act & Assert
        mockMvc.perform(get("/api/items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testItem.getId()))
                .andExpect(jsonPath("$[0].name").value(testItem.getName()));

        verify(itemService, times(1)).findAll();
    }

    @Test
    void getItemById_WithExistingId_ShouldReturnItem() throws Exception {
        // Arrange
        when(itemService.findById(1L)).thenReturn(Optional.of(testItem));

        // Act & Assert
        mockMvc.perform(get("/api/items/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItem.getId()))
                .andExpect(jsonPath("$.name").value(testItem.getName()));

        verify(itemService, times(1)).findById(1L);
    }

    @Test
    void getItemById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(get("/api/items/99"))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).findById(99L);
    }

    @Test
    void createItem_WithValidData_ShouldReturnCreated() throws Exception {
        // Arrange
        when(itemService.save(any(Item.class))).thenReturn(testItem);

        // Act & Assert
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(testItem.getId()));

        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void createItem_WithInvalidEmail_ShouldReturnBadRequest() throws Exception {
        // Arrange
        testItem.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isBadRequest());

        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void updateItem_WithExistingIdAndValidData_ShouldReturnOk() throws Exception {
        // Arrange
        when(itemService.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemService.save(any(Item.class))).thenReturn(testItem);

        // Act & Assert
        mockMvc.perform(put("/api/items/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testItem.getId()));

        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).save(any(Item.class));
    }

    @Test
    void updateItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(put("/api/items/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testItem)))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).findById(99L);
        verify(itemService, never()).save(any(Item.class));
    }

    @Test
    void deleteItem_WithExistingId_ShouldReturnNoContent() throws Exception {
        // Arrange
        when(itemService.findById(1L)).thenReturn(Optional.of(testItem));
        doNothing().when(itemService).deleteById(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/items/1"))
                .andExpect(status().isNoContent());

        verify(itemService, times(1)).findById(1L);
        verify(itemService, times(1)).deleteById(1L);
    }

    @Test
    void deleteItem_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        // Arrange
        when(itemService.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        mockMvc.perform(delete("/api/items/99"))
                .andExpect(status().isNotFound());

        verify(itemService, times(1)).findById(99L);
        verify(itemService, never()).deleteById(99L);
    }

    @Test
    void processItems_ShouldReturnProcessedItems() throws Exception {
        // Arrange
        when(itemService.processItemsAsync()).thenReturn(Collections.singletonList(testItem));

        // Act & Assert
        mockMvc.perform(get("/api/items/process"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(testItem.getId()));

        verify(itemService, times(1)).processItemsAsync();
    }
}
