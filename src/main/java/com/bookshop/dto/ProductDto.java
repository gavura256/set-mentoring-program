package com.bookshop.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import com.bookshop.dto.validation.OnCreate;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Positive;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDto implements Serializable {

    @Schema(description = "Product ID", example = "1")
    private Long id;

    @NotBlank
    @Schema(description = "Book title", example = "Clean Code", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @NotBlank
    @Schema(description = "Book author", example = "Robert C. Martin", requiredMode = Schema.RequiredMode.REQUIRED)
    private String author;

    @NotNull(groups = OnCreate.class)
    @Positive
    @Schema(description = "Price", example = "29.99", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal price;

    @Schema(description = "Book description", example = "A handbook of agile software craftsmanship")
    private String description;

    @PositiveOrZero
    @Schema(description = "Stock quantity", example = "50")
    private Integer quantity = 0;
}
