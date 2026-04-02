package com.bookshop.service;

import com.bookshop.converter.ProductConverter;
import com.bookshop.dto.ProductDto;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductConverter productConverter;

    public List<ProductDto> findAll() {
        return productRepository.findAll().stream()
                .map(productConverter::entityToDto)
                .collect(Collectors.toList());
    }

    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productConverter.entityToDto(product);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        Product saved = productRepository.save(productConverter.dtoToEntity(dto));
        return productConverter.entityToDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id))
                .toBuilder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .price(dto.getPrice())
                .description(dto.getDescription())
                .build();

        return productConverter.entityToDto(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        productRepository.delete(
                productRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id)));
    }
}
