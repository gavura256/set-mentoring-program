package com.bookshop.converter;

import com.bookshop.dto.ProductDto;
import com.bookshop.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductConverter {

    public ProductDto entityToDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setAuthor(product.getAuthor());
        dto.setPrice(product.getPrice());
        dto.setDescription(product.getDescription());
        return dto;
    }

    public Product dtoToEntity(ProductDto dto) {
        Product product = new Product();
        product.setTitle(dto.getTitle());
        product.setAuthor(dto.getAuthor());
        product.setPrice(dto.getPrice());
        product.setDescription(dto.getDescription());
        return product;
    }
}
