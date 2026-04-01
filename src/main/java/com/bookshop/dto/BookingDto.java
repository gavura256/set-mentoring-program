package com.bookshop.dto;

import com.bookshop.model.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto implements Serializable {

    @Schema(description = "Booking ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "User ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long userId;

    @NotNull
    @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull
    @Positive
    @Schema(description = "Quantity to book", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;

    @Schema(description = "Booking status", example = "PENDING")
    private BookingStatus status;

    @Schema(description = "Booking creation timestamp")
    private LocalDateTime createdAt;
}
