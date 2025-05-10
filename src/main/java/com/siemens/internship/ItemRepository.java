package com.siemens.internship;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Item entity providing data access operations.
 * Extends JpaRepository for standard CRUD operations.
 */
@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {
    /**
     * Custom query to retrieve all item IDs from the database.
     * This is used for batch processing operations.
     *
     * @return List of all item IDs
     */

    @Query("SELECT i.id FROM Item i")
    List<Long> findAllIds();
}
