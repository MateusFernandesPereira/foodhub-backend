package com.pereira.catalog.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record UpdateProductRequest(

    @Size(max = 255)
    String name,

    String description,

    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than zero")
    @Digits(integer = 8, fraction = 2)
    BigDecimal price,

    @Size(max = 100)
    String category,

    @Size(max = 500)
    String imageUrl,

    Boolean available,

    @Min(value = 0, message = "Stock quantity cannot be negative")
    Integer stockQuantity

) {}
