package com.bookshop.service;

import com.bookshop.mapper.ProductMapper;
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
    private ProductMapper productMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<ProductDto> findAll(Pageable pageable) {
        return productRepository.findAll(pageable).getContent().stream()
                .map(productMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        if (productRepository.existsByTitleAndAuthor(dto.getTitle(), dto.getAuthor())) {
            throw new ResourceAlreadyExistsException(
                    "Product '" + dto.getTitle() + "' by '" + dto.getAuthor() + "' already exists");
        }
        Product saved = productRepository.save(productMapper.toEntity(dto));
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));

        if (dto.getTitle() != null) product.setTitle(dto.getTitle());
        if (dto.getAuthor() != null) product.setAuthor(dto.getAuthor());
        if (dto.getPrice() != null) product.setPrice(dto.getPrice());
        if (dto.getDescription() != null) product.setDescription(dto.getDescription());
        if (dto.getQuantity() != null) product.setQuantity(dto.getQuantity());

        return productMapper.toDto(productRepository.save(product));
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
