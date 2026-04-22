package com.bookshop.controller;

import com.bookshop.dto.BookingDto;
import com.bookshop.dto.UserDto;
import com.bookshop.model.enums.Role;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class SecurityIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonUtils jsonUtils;

    @Test
    void getProducts_noAuth_returns401() throws Exception {
        mockMvc.perform(get(ApiRoutes.PRODUCTS))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getBookings_customerRole_returns403() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("manager@bookshop.com")
    void getBookings_managerRole_returns200() throws Exception {
        mockMvc.perform(get(ApiRoutes.BOOKINGS))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void updateBookingStatus_customerRole_returns403() throws Exception {
        mockMvc.perform(patch(ApiRoutes.BOOKINGS + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"APPROVED\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    void getUserBookings_otherUserId_returns403() throws Exception {
        // john.doe@example.com is userId=1. Requesting userId=2 should fail.
        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/user/2"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("john.doe@example.com")
    void getUserBookings_ownUserId_returns200() throws Exception {
        // john.doe@example.com is userId=1.
        mockMvc.perform(get(ApiRoutes.BOOKINGS + "/user/1"))
                .andExpect(status().isOk());
    }
}
