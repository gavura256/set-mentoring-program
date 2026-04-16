package com.bookshop.dto;

import com.bookshop.model.enums.Role;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto implements Serializable {

    @Schema(description = "User ID", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @NotBlank
    @Email
    @Schema(description = "User email", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "User full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @JsonIgnore
    @NotBlank
    @Schema(description = "User password", example = "password", requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;

    @Schema(description = "User role", example = "CUSTOMER")
    private Role role;
}
