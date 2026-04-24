package com.bookshop.dto;

import com.bookshop.model.enums.Role;
import com.bookshop.validation.ValidPassword;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserRequest {

    @NotBlank
    @Email
    @Schema(description = "User email", example = "john@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank
    @Schema(description = "User full name", example = "John Doe", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @ValidPassword
    @Schema(description = "User password", example = "Passw0rd$23")
    private String password;

    @Schema(description = "User role — only ADMINISTRATOR may assign non-default roles", example = "CUSTOMER")
    private Role role;
}
