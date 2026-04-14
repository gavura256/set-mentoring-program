package com.bookshop.service;

import com.bookshop.converter.StoreItemConverter;
import com.bookshop.dto.StoreItemDto;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Product;
import com.bookshop.model.StoreItem;
import com.bookshop.repository.ProductRepository;
import com.bookshop.repository.StoreItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class StoreItemService {

    @Autowired
    private StoreItemRepository storeItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private StoreItemConverter storeItemConverter;

    @Transactional(readOnly = true)
    public List<StoreItemDto> findAll() {
        return storeItemRepository.findAll().stream()
                .map(storeItemConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StoreItemDto findById(Long id) {
        StoreItem item = storeItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoreItem not found with id: " + id));
        return storeItemConverter.entityToDto(item);
    }

    @Transactional
    public StoreItemDto create(StoreItemDto dto) {
        if (storeItemRepository.findByProductId(dto.getProductId()).isPresent()) {
            throw new ResourceAlreadyExistsException("StoreItem already exists for product id: " + dto.getProductId());
        }
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));
        StoreItem saved = storeItemRepository.save(storeItemConverter.dtoToEntity(dto, product));
        return storeItemConverter.entityToDto(saved);
    }

    @Transactional
    public StoreItemDto update(Long id, StoreItemDto dto) {
        StoreItem item = storeItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("StoreItem not found with id: " + id));
        item.setQuantity(dto.getQuantity());
        return storeItemConverter.entityToDto(storeItemRepository.save(item));
    }

    @Transactional
    public void delete(Long id) {
        if (!storeItemRepository.existsById(id)) {
            throw new ResourceNotFoundException("StoreItem not found with id: " + id);
        }
        storeItemRepository.deleteById(id);
    }
}
