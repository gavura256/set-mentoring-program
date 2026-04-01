package com.bookshop.service;

import com.bookshop.converter.StoreItemConverter;
import com.bookshop.dto.StoreItemDto;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.model.StoreItem;
import com.bookshop.repository.ProductRepository;
import com.bookshop.repository.StoreItemRepository;
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
class StoreItemServiceTest {

    @Mock
    private StoreItemRepository storeItemRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StoreItemConverter storeItemConverter;

    @InjectMocks
    private StoreItemService storeItemService;

    private Product product;
    private StoreItem storeItem;
    private StoreItemDto storeItemDto;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setTitle("Clean Code");
        product.setAuthor("Robert C. Martin");
        product.setPrice(new BigDecimal("29.99"));

        storeItem = new StoreItem();
        storeItem.setId(1L);
        storeItem.setProduct(product);
        storeItem.setQuantity(10);

        storeItemDto = new StoreItemDto();
        storeItemDto.setId(1L);
        storeItemDto.setProductId(1L);
        storeItemDto.setQuantity(10);
    }

    @Test
    void findAll_returnsAllStoreItems() {
        when(storeItemRepository.findAll()).thenReturn(List.of(storeItem));
        when(storeItemConverter.entityToDto(storeItem)).thenReturn(storeItemDto);

        List<StoreItemDto> result = storeItemService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getQuantity()).isEqualTo(10);
    }

    @Test
    void findById_existingId_returnsDto() {
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(storeItem));
        when(storeItemConverter.entityToDto(storeItem)).thenReturn(storeItemDto);

        StoreItemDto result = storeItemService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(storeItemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> storeItemService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_validDto_returnsCreatedDto() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(storeItemConverter.dtoToEntity(storeItemDto, product)).thenReturn(storeItem);
        when(storeItemRepository.save(storeItem)).thenReturn(storeItem);
        when(storeItemConverter.entityToDto(storeItem)).thenReturn(storeItemDto);

        StoreItemDto result = storeItemService.create(storeItemDto);

        assertThat(result.getQuantity()).isEqualTo(10);
        verify(storeItemRepository).save(storeItem);
    }

    @Test
    void create_nonExistingProduct_throwsNotFoundException() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        storeItemDto.setProductId(99L);

        assertThatThrownBy(() -> storeItemService.create(storeItemDto))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void update_existingId_updatesQuantity() {
        when(storeItemRepository.findById(1L)).thenReturn(Optional.of(storeItem));
        when(storeItemRepository.save(storeItem)).thenReturn(storeItem);
        when(storeItemConverter.entityToDto(storeItem)).thenReturn(storeItemDto);

        StoreItemDto result = storeItemService.update(1L, storeItemDto);

        assertThat(result).isNotNull();
        verify(storeItemRepository).save(storeItem);
    }

    @Test
    void delete_existingId_deletesStoreItem() {
        when(storeItemRepository.existsById(1L)).thenReturn(true);

        storeItemService.delete(1L);

        verify(storeItemRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(storeItemRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> storeItemService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
