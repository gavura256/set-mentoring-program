package com.bookshop.controller;

import com.bookshop.dto.BookingDto;
import com.bookshop.dto.ProductDto;
import com.bookshop.dto.UserDto;
import com.bookshop.model.enums.Role;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookingControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        UserDto user = new UserDto();
        user.setEmail("booking_user@example.com");
        user.setName("Booking User");
        user.setRole(Role.CUSTOMER);
        String userResp = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andReturn().getResponse().getContentAsString();
        userId = objectMapper.readValue(userResp, UserDto.class).getId();

        ProductDto product = new ProductDto();
        product.setTitle("Booking Book");
        product.setAuthor("Author");
        product.setPrice(new BigDecimal("25.00"));
        String prodResp = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andReturn().getResponse().getContentAsString();
        productId = objectMapper.readValue(prodResp, ProductDto.class).getId();
    }

    private BookingDto buildBookingDto() {
        BookingDto dto = new BookingDto();
        dto.setUserId(userId);
        dto.setProductId(productId);
        dto.setQuantity(1);
        return dto;
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get("/api/bookings"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validBooking_returnsCreatedWithPendingStatus() throws Exception {
        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBookingDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_existingBooking_returnsOk() throws Exception {
        String resp = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, BookingDto.class).getId();

        mockMvc.perform(get("/api/bookings/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/bookings/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_pendingBooking_approvesSuccessfully() throws Exception {
        String resp = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, BookingDto.class).getId();

        mockMvc.perform(patch("/api/bookings/{id}/status", id)
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void cancel_pendingBooking_returnsCancelledStatus() throws Exception {
        String resp = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, BookingDto.class).getId();

        mockMvc.perform(patch("/api/bookings/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void delete_existingBooking_returnsNoContent() throws Exception {
        String resp = mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, BookingDto.class).getId();

        mockMvc.perform(delete("/api/bookings/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByUserId_existingUser_returnsBookings() throws Exception {
        mockMvc.perform(post("/api/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(buildBookingDto())));

        mockMvc.perform(get("/api/bookings/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
