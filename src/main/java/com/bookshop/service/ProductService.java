package com.bookshop.service;

import com.bookshop.converter.ProductConverter;
import com.bookshop.dto.ProductDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductConverter productConverter;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).getContent().stream()
                .map(productConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productConverter.entityToDto(product);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        if (productRepository.existsByTitleAndAuthor(dto.getTitle(), dto.getAuthor())) {
            throw new ResourceAlreadyExistsException(
                    "Product '" + dto.getTitle() + "' by '" + dto.getAuthor() + "' already exists");
        }
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
                .quantity(dto.getQuantity())
                .build();

        return productConverter.entityToDto(productRepository.save(product));
    }

    @Transactional
    public void delete(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        if (bookingRepository.existsByProductId(id)) {
            throw new InvalidOperationException("Cannot delete product with existing bookings");
        }
        productRepository.delete(product);
    }
}
