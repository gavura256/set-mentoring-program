package com.bookshop.controller;

import com.bookshop.dto.StoreItemDto;
import com.bookshop.service.StoreItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.STORE_ITEMS)
@Tag(name = "Store Items")
public class StoreItemController {

    @Autowired
    private StoreItemService storeItemService;

    @GetMapping
    @Operation(summary = "Get all store items")
    public List<StoreItemDto> getAll() {
        return storeItemService.findAll();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get store item by ID")
    public StoreItemDto getById(@PathVariable Long id) {
        return storeItemService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new store item")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public StoreItemDto create(@Valid @RequestBody StoreItemDto dto) {
        return storeItemService.create(dto);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a store item")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public StoreItemDto update(@PathVariable Long id, @Valid @RequestBody StoreItemDto dto) {
        return storeItemService.update(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a store item")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void delete(@PathVariable Long id) {
        storeItemService.delete(id);
    }
}
