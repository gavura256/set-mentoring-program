package com.bookshop.controller;

import com.bookshop.AbstractIntegrationTest;
import com.bookshop.dto.BookingDto;
import com.bookshop.dto.ProductDto;
import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.util.JsonUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductControllerIntegrationTest extends AbstractIntegrationTest {

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
    @WithMockUser(roles = "MANAGER")
    void create_duplicateTitleAndAuthor_returnsConflict() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Duplicate Book")
                .author("Duplicate Author")
                .price(new BigDecimal("19.99"))
                .quantity(10)
                .build();

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_missingPrice_returnsBadRequest() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Some Book")
                .author("Some Author")
                .build();

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_negativePrice_returnsBadRequest() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Bad Book")
                .author("Author")
                .price(new BigDecimal("-5.00"))
                .build();
        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void create_omitQuantity_defaultsToZero() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("No Qty Book")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .build();
        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.quantity").value(0));
    }

    @Test
    @WithMockUser(roles = "CUSTOMER")
    void create_customerRole_returnsForbidden() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Forbidden Book")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .build();

        mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void update_nonExistingId_returnsNotFound() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Updated")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .build();
        mockMvc.perform(patch(ApiRoutes.PRODUCTS + "/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_managerRole_returnsForbidden() throws Exception {
        ProductDto dto = ProductDto.builder()
                .title("Protected Book")
                .author("Author")
                .price(new BigDecimal("5.00"))
                .quantity(5)
                .build();

        String resp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(dto)))
                .andReturn().getResponse().getContentAsString();
        Long id = jsonUtils.fromJson(resp, ProductDto.class).getId();

        mockMvc.perform(delete(ApiRoutes.PRODUCTS + "/{id}", id)
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("manager").roles("MANAGER")))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void delete_productWithBookings_returnsConflict() throws Exception {
        ProductDto productRequest = ProductDto.builder()
                .title("Booked Product")
                .author("Author")
                .price(new BigDecimal("10.00"))
                .quantity(10)
                .build();
        String prodResp = mockMvc.perform(post(ApiRoutes.PRODUCTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(productRequest)))
                .andReturn().getResponse().getContentAsString();
        Long productId = jsonUtils.fromJson(prodResp, ProductDto.class).getId();

        UserRequest registerRequest = UserRequest.builder()
                .email("prodlink@example.com")
                .name("Link User")
                .password("Password1")
                .build();
        String userResp = mockMvc.perform(post(ApiRoutes.AUTH + "/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(registerRequest)))
                .andReturn().getResponse().getContentAsString();
        Long userId = jsonUtils.fromJson(userResp, UserResponse.class).getId();

        BookingDto bookingRequest = BookingDto.builder()
                .userId(userId)
                .productId(productId)
                .quantity(1)
                .build();
        mockMvc.perform(post(ApiRoutes.BOOKINGS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonUtils.toJson(bookingRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete(ApiRoutes.PRODUCTS + "/{id}", productId))
                .andExpect(status().isConflict());
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
