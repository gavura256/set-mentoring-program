package com.bookshop.controller;

import com.bookshop.dto.BookingDto;
import com.bookshop.model.enums.BookingStatus;
import com.bookshop.service.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping(ApiRoutes.BOOKINGS)
@Tag(name = "Bookings")
public class BookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    @Operation(summary = "Get all bookings")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public List<BookingDto> getAll(@PageableDefault(size = 20) Pageable pageable) {
        return bookingService.findAll(pageable);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get booking by ID")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR') or @bookingService.isOwner(#id, authentication.principal.id)")
    public BookingDto getById(@PathVariable Long id) {
        return bookingService.findById(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get bookings by user ID")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR') or #userId == authentication.principal.id")
    public List<BookingDto> getByUserId(@PathVariable Long userId) {
        return bookingService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new booking")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR') or #dto.userId == authentication.principal.id")
    public BookingDto create(@Valid @RequestBody BookingDto dto) {
        return bookingService.create(dto);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update booking status (APPROVED / REJECTED)")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR')")
    public BookingDto updateStatus(@PathVariable Long id, @RequestParam BookingStatus status) {
        return bookingService.updateStatus(id, status);
    }

    @PatchMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    @PreAuthorize("hasAnyRole('MANAGER','ADMINISTRATOR') or @bookingService.isOwner(#id, authentication.principal.id)")
    public BookingDto cancel(@PathVariable Long id) {
        return bookingService.cancel(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a booking")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public void delete(@PathVariable Long id) {
        bookingService.delete(id);
    }
}
