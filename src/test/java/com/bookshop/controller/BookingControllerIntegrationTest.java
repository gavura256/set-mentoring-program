package com.bookshop.controller;

import com.bookshop.dto.BookingDto;
import com.bookshop.dto.ProductDto;
import com.bookshop.dto.UserDto;
import com.bookshop.model.enums.Role;
import com.bookshop.util.JsonUtils;
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
    MockMvc mockMvc;

    @Autowired
    JsonUtils jsonUtils;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        UserDto user = UserDto.builder()
                .email("booking_user@example.com")
                .name("Booking User")
                .role(Role.CUSTOMER)
                .build();
        String userResp = mockMvc.perform(post(ApiRoutes.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(user)))
                .andReturn().getResponse().getContentAsString();
        userId = jsonUtils.fromJson(userResp, UserDto.class).getId();

        ProductDto product = ProductDto.builder()
                .title("Booking Book")
                .author("Author")
                .price(new BigDecimal("25.00"))
                .build();
        String prodResp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(product)))
                .andReturn().getResponse().getContentAsString();
        productId = jsonUtils.fromJson(prodResp, ProductDto.class).getId();
    }

    private BookingDto buildBookingDto() {
        return BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .build();
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validBooking_returnsCreatedWithPendingStatus() throws Exception {
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void getById_existingBooking_returnsOk() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateStatus_pendingBooking_approvesSuccessfully() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}/status", id)
                        .param("status", "APPROVED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    void cancel_pendingBooking_returnsCancelledStatus() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    void delete_existingBooking_returnsNoContent() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.BOOKINGS + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void getByUserId_existingUser_returnsBookings() throws Exception {
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUtils.toJson(buildBookingDto())));

        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
