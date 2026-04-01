package com.bookshop.controller;

import com.bookshop.dto.UserDto;
import com.bookshop.model.enums.Role;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonUtils jsonUtils;

    private UserDto buildUserDto(String email) {
        return UserDto.builder()
                .email(email)
                .name("Test User")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validUser_returnsCreated() throws Exception {
        mockMvc.perform(post(ApiRoutes.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildUserDto("new@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void create_duplicateEmail_returnsConflict() throws Exception {
        UserDto dto = buildUserDto("dup@example.com");

        mockMvc.perform(post(ApiRoutes.USERS)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonUtils.toJson(dto)));

        mockMvc.perform(post(ApiRoutes.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_existingUser_returnsNoContent() throws Exception {
        String response = mockMvc.perform(post(ApiRoutes.USERS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(buildUserDto("del@example.com"))))
                .andReturn().getResponse().getContentAsString();

        Long id = jsonUtils.fromJson(response, UserDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.USERS + "/{id}", id))
                .andExpect(status().isNoContent());
    }
}
