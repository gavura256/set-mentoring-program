package com.bookshop.converter;

import com.bookshop.dto.StoreItemDto;
import com.bookshop.model.Product;
import com.bookshop.model.StoreItem;
import org.springframework.stereotype.Component;

@Component
public class StoreItemConverter {

    public StoreItemDto entityToDto(StoreItem storeItem) {
        StoreItemDto dto = new StoreItemDto();
        dto.setId(storeItem.getId());
        dto.setProductId(storeItem.getProduct().getId());
        dto.setQuantity(storeItem.getQuantity());
        return dto;
    }

    public StoreItem dtoToEntity(StoreItemDto dto, Product product) {
        StoreItem storeItem = new StoreItem();
        storeItem.setProduct(product);
        storeItem.setQuantity(dto.getQuantity());
        return storeItem;
    }
}
