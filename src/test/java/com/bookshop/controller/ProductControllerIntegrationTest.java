package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
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

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JsonUtils jsonUtils;

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.PRODUCTS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_validProduct_returnsCreated() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Test Book")
                .author("Test Author")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .build();

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"))
                .andExpect(jsonPath("$.quantity").value(10));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_missingTitle_returnsBadRequest() throws Exception {
        ProductDto dto = ProductDto.builder()
                .author("Author")
                .price(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void getById_existingId_returnsOk() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Get Book")
                .author("Author")
                .price(new BigDecimal("15.00"))
                .quantity(10)
                .build();

        String response = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = jsonUtils.fromJson(response, ProductDto.class).getId();

        mockMvc.perform(get(ApiRoutes.PRODUCTS + "/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Get Book"));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.PRODUCTS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void update_existingProduct_returnsOk() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Original")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .quantity(10)
                .build();

        String response = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = jsonUtils.fromJson(response, ProductDto.class).getId();

        ProductDto updatedDto = ProductDto.builder()
                .title("Updated")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .quantity(20)
                .build();

        mockMvc.perform(patch(ApiRoutes.PRODUCTS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_existingProduct_returnsNoContent() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("To Delete")
                .author("Author")
                .price(new BigDecimal("5.00"))
                .quantity(10)
                .build();

        String response = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = jsonUtils.fromJson(response, ProductDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.PRODUCTS + "/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(ApiRoutes.PRODUCTS + "/{id}", id))
                .andExpect(status().isNotFound());
    }
}
