package com.siemens.internship;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Service class for Item operations including CRUD and asynchronous processing.
 */

@Service
public class ItemService {
    @Autowired
    private ItemRepository itemRepository;

    // Using a properly managed ExecutorService with a shutdown hook
    private static final ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    /**
     * Retrieves all items from the repository.
     *
     * @return List of all items
     */
    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    /**
     * Finds an item by its ID.
     *
     * @param id The ID of the item to find
     * @return Optional containing the item if found, empty otherwise
     */
    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves an item to the repository.
     *
     * @param item The item to save
     * @return The saved item with updated ID
     */
    public Item save(Item item) {
        return itemRepository.save(item);
    }

    /**
     * Deletes an item by its ID.
     *
     * @param id The ID of the item to delete
     */
    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Processes all items asynchronously by updating their status.
     * The implementation ensures:
     * 1. All items are processed before returning results
     * 2. Thread safety for shared state (using CompletableFuture for coordination)
     * 3. Proper error handling and propagation
     * 4. Efficient use of system resources
     *
     * @return List of all successfully processed items
     */

    public List<Item> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();

        // Create a list of CompletableFutures to track all processing tasks
        List<CompletableFuture<Item>> futures = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> processItems(id), executor))
                .toList();

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try{
            // Wait for all processing to complete with a reasonable timeout
            allFutures.get(30, TimeUnit.SECONDS);

            // Collect all successfully processed items (filtering out nulls from failed operations)
            return futures.stream()
                    .map(future -> {
                        try {
                            return future.get();
                        } catch (InterruptedException | ExecutionException e) {
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (ExecutionException e) {
            throw new RuntimeException("Error during item processing", e.getCause());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing was interrupted", e);
        } catch (TimeoutException e) {
            throw new RuntimeException("Processing timed out", e);
        }
    }

    /**
     * Processes a single item - retrieves it, updates status, and saves.
     * This is a helper method for the asynchronous processing.
     *
     * @param id The ID of the item to process
     * @return The processed item, or null if processing failed
     */

    private Item processItems(Long id) {
        try {
            // Introduce a small processing delay to simulate work
            Thread.sleep(100);

            // Find and process the item
            return itemRepository.findById(id)
                    .map(item -> {
                        item.setStatus(ItemStatus.PROCESSED.name());
                        return itemRepository.save(item);
                    })
                    .orElse(null);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Item processing interrupted", e);
        } catch (Exception e) {
            throw new RuntimeException("Error processing item with ID: " + id, e);
        }
    }

}

