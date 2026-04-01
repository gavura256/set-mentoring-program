package com.bookshop.service;

import com.bookshop.converter.ProductConverter;
import com.bookshop.dto.ProductDto;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductConverter productConverter;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setTitle("Clean Code");
        product.setAuthor("Robert C. Martin");
        product.setPrice(new BigDecimal("29.99"));

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setTitle("Clean Code");
        productDto.setAuthor("Robert C. Martin");
        productDto.setPrice(new BigDecimal("29.99"));
    }

    @Test
    void findAll_returnsAllProducts() {
        when(productRepository.findAll()).thenReturn(List.of(product));
        when(productConverter.entityToDto(product)).thenReturn(productDto);

        List<ProductDto> result = productService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("Clean Code");
    }

    @Test
    void findById_existingId_returnsDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productConverter.entityToDto(product)).thenReturn(productDto);

        ProductDto result = productService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
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
        when(productConverter.dtoToEntity(productDto)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productConverter.entityToDto(product)).thenReturn(productDto);

        ProductDto result = productService.create(productDto);

        assertThat(result.getTitle()).isEqualTo("Clean Code");
        verify(productRepository).save(product);
    }

    @Test
    void update_existingId_updatesAndReturnsDto() {
        ProductDto updateDto = new ProductDto();
        updateDto.setTitle("Updated");
        updateDto.setAuthor("Author");
        updateDto.setPrice(new BigDecimal("19.99"));

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productConverter.entityToDto(product)).thenReturn(productDto);

        ProductDto result = productService.update(1L, updateDto);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    void update_nonExistingId_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.update(99L, productDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existingId_deletesProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.delete(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(productRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> productService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
