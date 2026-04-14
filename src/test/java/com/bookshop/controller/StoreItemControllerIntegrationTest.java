package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
import com.bookshop.dto.StoreItemDto;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

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
class StoreItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JsonUtils jsonUtils;

    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        ProductDto product = ProductDto.builder()
                .title("Store Book")
                .author("Author")
                .price(new BigDecimal("20.00"))
                .build();
        String resp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(product)))
                .andReturn().getResponse().getContentAsString();
        productId = jsonUtils.fromJson(resp, ProductDto.class).getId();
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get(ApiRoutes.STORE_ITEMS))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validStoreItem_returnsCreated() throws Exception {
        StoreItemDto dto = StoreItemDto.builder()
                .productId(productId)
                .quantity(5)
                .build();

        mockMvc.perform(post(ApiRoutes.STORE_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get(ApiRoutes.STORE_ITEMS + "/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingStoreItem_updatesQuantity() throws Exception {
        StoreItemDto dto = StoreItemDto.builder()
                .productId(productId)
                .quantity(5)
                .build();

        String resp = mockMvc.perform(post(ApiRoutes.STORE_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, StoreItemDto.class).getId();

        StoreItemDto updatedDto = StoreItemDto.builder()
                .productId(productId)
                .quantity(20)
                .build();

        mockMvc.perform(put(ApiRoutes.STORE_ITEMS + "/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void delete_existingStoreItem_returnsNoContent() throws Exception {
        StoreItemDto dto = StoreItemDto.builder()
                .productId(productId)
                .quantity(3)
                .build();

        String resp = mockMvc.perform(post(ApiRoutes.STORE_ITEMS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, StoreItemDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.STORE_ITEMS + "/{id}", id))
                .andExpect(status().isNoContent());
    }
}
