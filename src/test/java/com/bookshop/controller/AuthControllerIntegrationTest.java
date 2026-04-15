package com.bookshop.controller;

import com.bookshop.dto.auth.LoginRequest;
import com.bookshop.dto.UserDto;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonUtils jsonUtils;

    @Test
    void login_validCredentials_returnsTokenAndUserDetails() throws Exception {
        // Create a user
        UserDto user = UserDto.builder()
                .email("test_auth@example.com")
                .name("Auth User")
                .password("password123")
                .build();
        
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(user)))
                .andExpect(status().isCreated());

        // Attempt login
        LoginRequest loginRequest = LoginRequest.builder()
                .email("test_auth@example.com")
                .password("password123")
                .build();
        
        mockMvc.perform(post(ApiRoutes.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.userId").exists());
    }

    @Test
    void register_validUser_returnsCreated() throws Exception {
        UserDto user = UserDto.builder()
                .email("new_reg@example.com")
                .name("New User")
                .password("password123")
                .build();

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(user)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new_reg@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        UserDto user = UserDto.builder()
                .email("dup_reg@example.com")
                .name("Dup User")
                .password("password123")
                .build();

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUtils.toJson(user)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(user)))
                .andExpect(status().isConflict());
    }

    @Test
    void login_invalidCredentials_returnsUnauthorized() throws Exception {
        LoginRequest loginRequest = LoginRequest.builder()
                .email("nonexistent@example.com")
                .password("wrongpassword")
                .build();

        mockMvc.perform(post(ApiRoutes.AUTH + "/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(loginRequest)))
                .andExpect(status().isUnauthorized());
    }
}
