package com.bookshop.dto;

import com.bookshop.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User email", example = "john@example.com")
    private String email;

    @Schema(description = "User full name", example = "John Doe")
    private String name;

    @Schema(description = "User role", example = "CUSTOMER")
    private Role role;
}
