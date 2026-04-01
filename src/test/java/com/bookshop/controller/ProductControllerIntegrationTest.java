package com.bookshop.controller;

import com.bookshop.dto.ProductDto;
import com.fasterxml.jackson.databind.ObjectMapper;
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
class ProductControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getAll_returnsOkWithList() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void create_validProduct_returnsCreated() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setTitle("Test Book");
        dto.setAuthor("Test Author");
        dto.setPrice(new BigDecimal("19.99"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Book"))
                .andExpect(jsonPath("$.author").value("Test Author"));
    }

    @Test
    void create_missingTitle_returnsBadRequest() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setAuthor("Author");
        dto.setPrice(new BigDecimal("10.00"));

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getById_existingId_returnsOk() throws Exception {
        // Create first
        ProductDto dto = new ProductDto();
        dto.setTitle("Get Book");
        dto.setAuthor("Author");
        dto.setPrice(new BigDecimal("15.00"));

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(response, ProductDto.class).getId();

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Get Book"));
    }

    @Test
    void getById_nonExistingId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/api/products/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_existingProduct_returnsOk() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setTitle("Original");
        dto.setAuthor("Author");
        dto.setPrice(new BigDecimal("10.00"));

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(response, ProductDto.class).getId();

        dto.setTitle("Updated");
        mockMvc.perform(put("/api/products/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void delete_existingProduct_returnsNoContent() throws Exception {
        ProductDto dto = new ProductDto();
        dto.setTitle("To Delete");
        dto.setAuthor("Author");
        dto.setPrice(new BigDecimal("5.00"));

        String response = mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andReturn().getResponse().getContentAsString();

        Long id = objectMapper.readValue(response, ProductDto.class).getId();

        mockMvc.perform(delete("/api/products/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/products/{id}", id))
                .andExpect(status().isNotFound());
    }
}
