package com.siemens.internship;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for handling Item operations.
 * Provides endpoints for CRUD operations and asynchronous processing.
 */


@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemService itemService;

    /**
     * Retrieves all items.
     *
     * @return ResponseEntity with a list of all items and HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return new ResponseEntity<>(itemService.findAll(), HttpStatus.OK);
    }

    /**
     * Creates a new item.
     * Includes validation for required fields and formats (e.g., email).
     *
     * @param item The item to create
     * @param result Validation result
     * @return ResponseEntity with created item (201) or validation errors (400)
     */

    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        // Handle validation errors
        ResponseEntity<?> errors = getResponseEntity(result);
        if (errors != null) return errors;
        // Save the valid item
        Item savedItem = itemService.save(item);
        return new ResponseEntity<>(savedItem, HttpStatus.CREATED);
    }

    /**
     * Retrieves an item by its ID.
     *
     * @param id The ID of the item to retrieve
     * @return ResponseEntity with item (200) or not found (404)
     */

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        return itemService.findById(id)
                .map(item -> new ResponseEntity<>(item, HttpStatus.OK))
                .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    /**
     * Updates an existing item.
     *
     * @param id The ID of the item to update
     * @param item The updated item data
     * @return ResponseEntity with updated item (200) or not found (404)
     */

    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        // Handle validation errors
        ResponseEntity<?> errors = getResponseEntity(result);
        if (errors != null) return errors;

        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isPresent()){
            item.setId(id);
            return new ResponseEntity<>(itemService.save(item), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Deletes an item by its ID.
     *
     * @param id The ID of the item to delete
     * @return ResponseEntity with no content (204)
     */

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        // Check if an item exists before attempting to delete
        itemService.deleteById(id);
        if (itemService.findById(id).isPresent()) {
            itemService.deleteById(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Processes all items asynchronously.
     *
     * @return ResponseEntity with a list of processed items (200) or error (500)
     */

    @GetMapping("/process")
    public ResponseEntity<?> processItems() {
        try {
            List<Item> processedItems = itemService.processItemsAsync();
            return new ResponseEntity<>(processedItems, HttpStatus.OK);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("message", "Error processing items: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<?> getResponseEntity(BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error: result.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            return new ResponseEntity<>(errors, HttpStatus.BAD_REQUEST);
        }
        return null;
    }
}
