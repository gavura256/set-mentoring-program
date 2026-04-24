package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
import com.bookshop.dto.UserDto;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonUtils jsonUtils;

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void getById_passwordNotExposedInResponse() throws Exception {
        String response = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"noleak@example.com\",\"name\":\"Test\",\"password\":\"Password1\"}"))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(response, UserDto.class).getId();

        mockMvc.perform(get(ApiRoutes.USERS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAll_customerRole_returnsForbidden() throws Exception {
        mockMvc.perform(get(ApiRoutes.USERS))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void getById_existingUser_returnsOk() throws Exception {
        String response = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"getbyid@example.com\",\"name\":\"Test User\",\"password\":\"Password1\"}"))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(response, UserDto.class).getId();

        mockMvc.perform(get(ApiRoutes.USERS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("getbyid@example.com"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void update_existingUser_returnsOk() throws Exception {
        String response = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"update_usr@example.com\",\"name\":\"Original\",\"password\":\"Password1\"}"))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(response, UserDto.class).getId();

        mockMvc.perform(put(ApiRoutes.USERS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Updated Name\",\"email\":\"update_usr@example.com\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void update_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(put(ApiRoutes.USERS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"test@example.com\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_userWithBookings_returnsConflict() throws Exception {
        String userResp = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"busy_user@example.com\",\"name\":\"Busy\",\"password\":\"Password1\"}"))
                .andReturn().getResponse().getContentAsString();
        Long userId = jsonUtils.fromJson(userResp, UserDto.class).getId();

        String prodResp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Busy Book\",\"author\":\"Author\",\"price\":10.00,\"quantity\":10}"))
                .andReturn().getResponse().getContentAsString();
        Long prodId = jsonUtils.fromJson(prodResp, ProductDto.class).getId();

        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":" + userId + ",\"productId\":" + prodId + ",\"quantity\":1}"))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(ApiRoutes.USERS + "/{id}", userId))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_existingUser_returnsNoContent() throws Exception {
        String response = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"del@example.com\",\"name\":\"Test User\",\"password\":\"Password1\",\"role\":\"CUSTOMER\"}"))
                .andReturn().getResponse().getContentAsString();

        Long id = jsonUtils.fromJson(response, UserDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.USERS + "/{id}", id))
                .andExpect(status().isNoContent());
    }
}
