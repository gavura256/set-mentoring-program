package com.bookshop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StoreItemDto implements Serializable {

    @Schema(description = "Store item ID", example = "1")
    private Long id;

    @NotNull
    @Schema(description = "Product ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long productId;

    @NotNull
    @Positive
    @Schema(description = "Quantity in stock", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer quantity;
}
