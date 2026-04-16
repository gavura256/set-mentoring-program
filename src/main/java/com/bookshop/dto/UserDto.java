package com.bookshop.dto;

import com.bookshop.model.enums.Role;
import com.bookshop.validation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @ValidPassword
    @Schema(description = "User password", example = "Passw0rd$23", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String password;

    @Schema(description = "User role", example = "CUSTOMER")
    private Role role;
}
