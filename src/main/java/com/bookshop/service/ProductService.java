package com.bookshop.service;

import com.bookshop.mapper.ProductMapper;
import com.bookshop.dto.ProductDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public Page<ProductDto> findAll(Pageable pageable) {
        log.debug("Fetching all products, pageable: {}", pageable);
        return productRepository.findAll(pageable).map(productMapper::toDto);
    }

    @Transactional(readOnly = true)
    public ProductDto findById(Long id) {
        log.debug("Fetching product by id: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
        return productMapper.toDto(product);
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        log.info("Creating product, title: '{}', author: '{}'", dto.getTitle(), dto.getAuthor());
        if (productRepository.existsByTitleAndAuthor(dto.getTitle(), dto.getAuthor())) {
            log.info("Product already exists, title: '{}', author: '{}'", dto.getTitle(), dto.getAuthor());
            throw new ResourceAlreadyExistsException(
                    "Product '" + dto.getTitle() + "' by '" + dto.getAuthor() + "' already exists");
        }
        Product saved = productRepository.save(productMapper.toEntity(dto));
        log.info("Product created with id: {}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Transactional
    public ProductDto update(Long id, ProductDto dto) {
        log.info("Updating productId: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });

        if (dto.getTitle() != null) { product.setTitle(dto.getTitle()); }
        if (dto.getAuthor() != null) { product.setAuthor(dto.getAuthor()); }
        if (dto.getPrice() != null) { product.setPrice(dto.getPrice()); }
        if (dto.getDescription() != null) { product.setDescription(dto.getDescription()); }
        if (dto.getQuantity() != null) { product.setQuantity(dto.getQuantity()); }

        Product saved = productRepository.save(product);
        log.info("Product updated, productId: {}", saved.getId());
        return productMapper.toDto(saved);
    }

    @Transactional(readOnly = true)
    public List<ProductDto> searchByTitle(String title) {
        log.debug("Searching products by title containing: '{}'", title);
        return productRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(productMapper::toDto)
                .toList();
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting productId: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Product not found with id: {}", id);
                    return new ResourceNotFoundException("Product not found with id: " + id);
                });
        if (bookingRepository.existsByProductId(id)) {
            log.info("Cannot delete productId: {} — existing bookings present", id);
            throw new InvalidOperationException("Cannot delete product with existing bookings");
        }
        productRepository.delete(product);
        log.info("Product deleted, productId: {}", id);
    }
}
