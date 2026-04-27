package com.bookshop.controller;

import com.bookshop.AbstractIntegrationTest;
import com.bookshop.dto.BookingDto;
import com.bookshop.dto.ProductDto;
import com.bookshop.dto.UpdateStatusRequest;
import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.model.enums.BookingStatus;
import com.bookshop.model.enums.Role;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import com.bookshop.model.User;
import com.bookshop.security.CustomUserDetails;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BookingControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonUtils jsonUtils;

    private Long userId;
    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("booking_user@example.com")
                .name("Booking User")
                .password("Password1")
                .build();
        String userResp = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        userId = jsonUtils.fromJson(userResp, UserResponse.class).getId();

        ProductDto product = ProductDto.builder()
                .title("Booking Book")
                .author("Author")
                .price(new BigDecimal("25.00"))
                .quantity(50)
                .build();
        String prodResp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .with(user("admin").roles("ADMINISTRATOR"))
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

    private RequestPostProcessor asCustomer() {
        return user(
                new CustomUserDetails(
                        User.builder()
                                .id(userId)
                                .email("booking_user@example.com")
                                .password("password")
                                .role(Role.CUSTOMER)
                                .build()
                ));
    }

    private RequestPostProcessor asManager() {
        return user(
                new CustomUserDetails(
                        User.builder()
                                .id(userId)
                                .email("booking_user@example.com")
                                .password("password")
                                .role(Role.MANAGER)
                                .build()
                ));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_validBooking_returnsCreatedWithPendingStatus() throws Exception {
        BookingDto dto = buildBookingDto();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getById_existingBooking_returnsOk() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateStatus_pendingBooking_approvesSuccessfully() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        UpdateStatusRequest statusRequest = UpdateStatusRequest.builder().status(BookingStatus.APPROVED).build();
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APPROVED"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_managerRole_returnsCreated() throws Exception {
        BookingDto dto = buildBookingDto();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asManager())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void cancel_managerRole_returnsCancelledStatus() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        UpdateStatusRequest statusRequest = UpdateStatusRequest.builder().status(BookingStatus.CANCELLED).build();
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(statusRequest))
                        .with(user(new CustomUserDetails(
                                User.builder().id(99L).email("manager@example.com").password("password").role(Role.MANAGER).build()
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void cancel_pendingBooking_returnsCancelledStatus() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        UpdateStatusRequest statusRequest = UpdateStatusRequest.builder().status(BookingStatus.CANCELLED).build();
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(statusRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_existingBooking_returnsNoContent() throws Exception {
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildBookingDto())))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.BOOKINGS + "/{id}", id))
                .andExpect(status().isNoContent());
    }

    @Test
    void create_insufficientStock_returnsConflict() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(100)
                .build();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void create_nonExistingProduct_returnsNotFound() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .productId(99999L)
                .quantity(1)
                .build();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_nonExistingUser_returnsNotFound() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(99999L)
                .productId(productId)
                .quantity(1)
                .build();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(user(new CustomUserDetails(
                                User.builder().id(99999L).email("ghost@example.com").password("password").role(Role.MANAGER).build()
                        )))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_customerForOtherUser_returnsForbidden() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(99999L)
                .productId(productId)
                .quantity(1)
                .build();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_missingUserId_returnsBadRequest() throws Exception {
        BookingDto dto = BookingDto.builder()
                .productId(productId)
                .quantity(1)
                .build();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_missingProductId_returnsBadRequest() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .quantity(1)
                .build();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_zeroQuantity_returnsBadRequest() throws Exception {
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(0)
                .build();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void updateStatus_approveWithInsufficientStock_returnsConflict() throws Exception {
        ProductDto limitedProduct = ProductDto.builder()
                .title("Limited Book")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .quantity(5)
                .build();
        String prodResp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .with(user("admin").roles("ADMINISTRATOR"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(limitedProduct)))
                .andReturn().getResponse().getContentAsString();
        Long limitedProductId = jsonUtils.fromJson(prodResp, ProductDto.class).getId();

        BookingDto dtoA = BookingDto.builder().userId(userId).productId(limitedProductId).quantity(3).build();
        String respA = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dtoA)))
                .andReturn().getResponse().getContentAsString();
        Long bookingAId = jsonUtils.fromJson(respA, BookingDto.class).getId();

        BookingDto dtoB = BookingDto.builder().userId(userId).productId(limitedProductId).quantity(3).build();
        String respB = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dtoB)))
                .andReturn().getResponse().getContentAsString();
        Long bookingBId = jsonUtils.fromJson(respB, BookingDto.class).getId();

        UpdateStatusRequest approveRequest = UpdateStatusRequest.builder().status(BookingStatus.APPROVED).build();
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", bookingAId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(approveRequest)))
                .andExpect(status().isOk());

        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", bookingBId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(approveRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getByUserId_nonExistingUser_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/user/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_nonExistingBooking_returnsNotFound() throws Exception {
        mockMvc.perform(delete(ApiRoutes.BOOKINGS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getByUserId_existingUser_returnsBookings() throws Exception {
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                .with(asCustomer())
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUtils.toJson(buildBookingDto())));

        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/user/{userId}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_statusInRequest_ignoredAndForcedToPending() throws Exception {
        // Clients must not be able to set the initial status — it must always be PENDING.
        // BookingDto.status is READ_ONLY so Jackson serializes it; server ignores it on input.
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .status(BookingStatus.APPROVED)
                .build();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_exactStockQuantity_succeeds() throws Exception {
        // Booking exactly the available stock (boundary) must succeed
        BookingDto dto = BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(50) // product was created with 50 in stock
                .build();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void approve_decrementsStockCorrectly() throws Exception {
        BookingDto dto = BookingDto.builder().userId(userId).productId(productId).quantity(3).build();
        String resp = mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .with(asCustomer())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, BookingDto.class).getId();

        UpdateStatusRequest approveRequest = UpdateStatusRequest.builder().status(BookingStatus.APPROVED).build();
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(approveRequest)))
                .andExpect(status().isOk());

        // product stock should now be 47 (50 - 3)
        mockMvc.perform(get(ApiRoutes.PRODUCTS + "/{id}", productId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(47));
    }
}
