package com.bookshop.service;

import com.bookshop.mapper.ProductMapper;
import com.bookshop.dto.ProductDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(new BigDecimal("29.99"))
                .quantity(50)
                .build();

        productDto = ProductDto.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(new BigDecimal("29.99"))
                .quantity(50)
                .build();
    }

    @Test
    void findAll_returnsAllProducts() {
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(productMapper.toDto(product)).thenReturn(productDto);

        Page<ProductDto> result = productService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void findAll_returnsEmptyListWhenNoProducts() {
        when(productRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        Page<ProductDto> result = productService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findById_existingId_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_validDto_returnsCreatedDto() {
        when(productMapper.toEntity(productDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = productService.create(productDto);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(productRepository).save(product);
    }

    @Test
    void update_existingId_updatesFieldsAndReturnsDto() {
        ProductDto updateDto = ProductDto.builder()
                .title("Updated Title")
                .author("Updated Author")
                .price(new BigDecimal("19.99"))
                .quantity(25)
                .build();

        ArgumentCaptor<Product> captor = ArgumentCaptor.forClass(Product.class);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        when(productMapper.toDto(product)).thenReturn(productDto);

        ProductDto result = productService.update(1L, updateDto);

        verify(productRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isEqualTo(1L);
        assertThat(captor.getValue().getTitle()).isEqualTo("Updated Title");
        assertThat(captor.getValue().getAuthor()).isEqualTo("Updated Author");
        assertThat(captor.getValue().getPrice()).isEqualByComparingTo("19.99");
        assertThat(captor.getValue().getQuantity()).isEqualTo(25);
        assertThat(result).isNotNull();
    }

    @Test
    void update_nonExistingId_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, productDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    // ProductService.delete() resolves the entity via findById first, then calls
    // delete(entity) — not existsById + deleteById.
    @Test
    void delete_existingId_deletesProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(bookingRepository.existsByProductId(1L)).thenReturn(false);

        productService.delete(1L);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_duplicateTitleAndAuthor_throwsAlreadyExistsException() {
        when(productRepository.existsByTitleAndAuthor("Clean Code", "Robert C. Martin")).thenReturn(true);

        assertThatThrownBy(() -> productService.create(productDto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("already exists");
    }

    @Test
    void delete_productWithBookings_throwsInvalidOperationException() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(bookingRepository.existsByProductId(1L)).thenReturn(true);

        assertThatThrownBy(() -> productService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot delete product with existing bookings");
    }
}
