package com.pereira.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record CreateProductRequest(

    @NotBlank(message = "Name is required")
    @Size(max = 255)
    String name,

    String description,

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2)
    BigDecimal price,

    @NotBlank(message = "Category is required")
    @Size(max = 100)
    String category,

    @Size(max = 500)
    String imageUrl,

    @NotNull(message = "Available status is required")
    Boolean available,

    @NotNull(message = "Stock quantity is required")
    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity

) {}
