package com.bookshop.dto;

import com.bookshop.model.enums.BookingStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BookingDto implements Serializable {

    @Schema(description = "Booking ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
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

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Booking status", example = "PENDING")
    private BookingStatus status;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @Schema(description = "Booking creation timestamp")
    private LocalDateTime createdAt;
}
