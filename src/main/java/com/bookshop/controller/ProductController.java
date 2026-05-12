package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
import com.bookshop.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.bookshop.dto.validation.OnCreate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.groups.Default;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;

@RestController
@RequestMapping(ApiRoutes.PRODUCTS)
@Tag(name = "Products")
@Validated
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    @Operation(summary = "Get all products")
    public Page<ProductDto> getAll(@ParameterObject @PageableDefault(size = 20) Pageable pageable) {
        return productService.findAll(pageable);
    }

    @GetMapping(ApiRoutes.BY_ID)
    @Operation(summary = "Get product by ID")
    public ProductDto getById(@PathVariable Long id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new product")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public ProductDto create(@Validated({Default.class, OnCreate.class}) @RequestBody ProductDto dto) {
        return productService.create(dto);
    }

    @PatchMapping(ApiRoutes.BY_ID)
    @Operation(summary = "Update a product")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public ProductDto update(@PathVariable Long id, @Valid @RequestBody ProductDto dto) {
        return productService.update(id, dto);
    }

    @DeleteMapping(ApiRoutes.BY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a product")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by title")
    public List<ProductDto> searchByTitle(@RequestParam @NotBlank String title) {
        return productService.searchByTitle(title);
    }
}
