package com.siemens.internship;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entity class representing an Item in the system.
 * Stores basic information about items including ID, name, description, status, and email.
 */

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Status cannot be null")
    @Pattern(regexp = "^(NEW|IN_PROGRESS|PROCESSED|COMPLETED|FAILED)$",
            message = "Status must be one of: NEW, IN_PROGRESS, PROCESSED, COMPLETED, FAILED")
    private String status;

    /**
     * Email validation using both standard @Email annotation and a regex pattern for additional validation
     * The regex ensures the email follows a standard format with alphanumeric characters, a valid domain, and TLD
     */

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "Email must be properly formatted")
    private String email;
}