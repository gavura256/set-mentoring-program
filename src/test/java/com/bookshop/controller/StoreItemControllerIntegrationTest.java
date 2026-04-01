package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
import com.bookshop.dto.StoreItemDto;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("dev")
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class StoreItemControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private Long productId;

    @BeforeEach
    void setUp() throws Exception {
        ProductDto product = new ProductDto();
        product.setTitle("Store Book");
        product.setAuthor("Author");
        product.setPrice(new BigDecimal("20.00"));
        String resp = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(product)))
                .andReturn().getResponse().getContentAsString();
        productId = objectMapper.readValue(resp, ProductDto.class).getId();
    }

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get("/api/store-items"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validStoreItem_returnsCreated() throws Exception {
        StoreItemDto dto = new StoreItemDto();
        dto.setProductId(productId);
        dto.setQuantity(5);

        mockMvc.perform(post("/api/store-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(5));
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/store-items/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingStoreItem_updatesQuantity() throws Exception {
        StoreItemDto dto = new StoreItemDto();
        dto.setProductId(productId);
        dto.setQuantity(5);

        String resp = mockMvc.perform(post("/api/store-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, StoreItemDto.class).getId();

        dto.setQuantity(20);
        mockMvc.perform(put("/api/store-items/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    void delete_existingStoreItem_returnsNoContent() throws Exception {
        StoreItemDto dto = new StoreItemDto();
        dto.setProductId(productId);
        dto.setQuantity(3);

        String resp = mockMvc.perform(post("/api/store-items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = objectMapper.readValue(resp, StoreItemDto.class).getId();

        mockMvc.perform(delete("/api/store-items/{id}", id))
                .andExpect(status().isNoContent());
    }
}
