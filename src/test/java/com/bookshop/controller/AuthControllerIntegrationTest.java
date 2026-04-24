package com.bookshop.controller;

import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.dto.auth.LoginRequest;
import com.bookshop.model.enums.Role;
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
        UserRequest registerRequest = UserRequest.builder()
                .email("test_auth@example.com")
                .name("Auth User")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test_auth@example.com")
                .password("Password1")
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
        UserRequest registerRequest = UserRequest.builder()
                .email("new_reg@example.com")
                .name("New User")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new_reg@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_duplicateEmail_returnsConflict() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("dup_reg@example.com")
                .name("Dup User")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
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

    @Test
    void register_invalidEmail_returnsBadRequest() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("not-an-email")
                .name("Test")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_weakPassword_returnsBadRequest() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("weak@example.com")
                .name("Test")
                .password("weakpass")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingName_returnsBadRequest() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("noname@example.com")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMINISTRATOR")
    void register_asAdministrator_canCreateAdminUser() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("newadmin@example.com")
                .name("New Admin")
                .password("Password1")
                .role(Role.ADMINISTRATOR)
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ADMINISTRATOR"));
    }

    @Test
    void register_withAdminRole_forcedToCustomer() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("tryadmin@example.com")
                .name("Hacker")
                .password("Password1")
                .role(Role.ADMINISTRATOR)
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void register_passwordNotReturnedInResponse() throws Exception {
        UserRequest registerRequest = UserRequest.builder()
                .email("nopwd@example.com")
                .name("Test")
                .password("Password1")
                .build();
        mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
