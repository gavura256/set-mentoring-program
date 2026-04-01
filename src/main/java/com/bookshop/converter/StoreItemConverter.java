package com.bookshop.converter;

import com.bookshop.dto.StoreItemDto;
import com.bookshop.model.Product;
import com.bookshop.model.StoreItem;
import org.springframework.stereotype.Component;

@Component
public class StoreItemConverter {

    public StoreItemDto entityToDto(StoreItem storeItem) {
        return StoreItemDto.builder()
                .id(storeItem.getId())
                .productId(storeItem.getProduct().getId())
                .quantity(storeItem.getQuantity())
                .build();
    }

    public StoreItem dtoToEntity(StoreItemDto dto, Product product) {
        return StoreItem.builder()
                .product(product)
                .quantity(dto.getQuantity())
                .build();
    }
}
