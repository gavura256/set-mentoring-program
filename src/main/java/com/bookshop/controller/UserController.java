package com.bookshop.controller;

import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(ApiRoutes.USERS)
@Tag(name = "Users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public Page<UserResponse> getAll(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return userService.findAll(pageable);
    }

    @GetMapping(ApiRoutes.BY_ID)
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR') or #id == authentication.principal.id")
    public UserResponse getById(@PathVariable Long id) {
        return userService.findById(id);
    }

    @PutMapping(ApiRoutes.BY_ID)
    @Operation(summary = "Update a user")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest dto) {
        return userService.update(id, dto);
    }

    @DeleteMapping(ApiRoutes.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }
}
