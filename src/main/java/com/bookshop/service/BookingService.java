package com.bookshop.service;

import com.bookshop.mapper.BookingMapper;
import com.bookshop.dto.BookingDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Booking;
import com.bookshop.model.Product;
import com.bookshop.model.User;
import com.bookshop.model.enums.BookingStatus;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.ProductRepository;
import com.bookshop.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BookingMapper bookingMapper;

    @Transactional(readOnly = true)
    public List<BookingDto> findAll(Pageable pageable) {
        log.debug("Fetching all bookings, pageable: {}", pageable);
        return bookingRepository.findAllWithFetch(pageable).getContent().stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDto findById(Long id) {
        log.debug("Fetching booking by id: {}", id);
        return bookingRepository.findById(id)
                .map(bookingMapper::toDto)
                .orElseThrow(() -> {
                    log.info("Booking not found with id: {}", id);
                    return new ResourceNotFoundException("Booking not found with id: " + id);
                });
    }

    @Transactional(readOnly = true)
    public List<BookingDto> findByUserId(Long userId) {
        log.debug("Fetching bookings for userId: {}", userId);
        userRepository.findById(userId)
                .orElseThrow(() -> {
                    log.info("User not found with id: {}", userId);
                    return new ResourceNotFoundException("User not found with id: " + userId);
                });
        return bookingRepository.findByUserIdWithFetch(userId).stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto create(BookingDto dto) {
        log.info("Creating booking for userId: {}, productId: {}, quantity: {}",
                dto.getUserId(), dto.getProductId(), dto.getQuantity());
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> {
                    log.info("User not found with id: {}", dto.getUserId());
                    return new ResourceNotFoundException("User not found with id: " + dto.getUserId());
                });
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> {
                    log.info("Product not found with id: {}", dto.getProductId());
                    return new ResourceNotFoundException("Product not found with id: " + dto.getProductId());
                });

        if (product.getQuantity() < dto.getQuantity()) {
            log.info("Insufficient stock for productId: {}, available: {}, requested: {}",
                    product.getId(), product.getQuantity(), dto.getQuantity());
            throw new InvalidOperationException("Insufficient stock for product: " + product.getTitle());
        }

        Booking booking = bookingMapper.toEntity(dto, user, product);
        booking.setStatus(BookingStatus.PENDING);
        Booking saved = bookingRepository.save(booking);
        log.info("Booking created with id: {} for userId: {}, productId: {}",
                saved.getId(), saved.getUser().getId(), saved.getProduct().getId());
        return bookingMapper.toDto(saved);
    }

    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        log.info("Updating status of bookingId: {} to: {}", id, status);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Booking not found with id: {}", id);
                    return new ResourceNotFoundException("Booking not found with id: " + id);
                });

        if (status == BookingStatus.APPROVED) {
            Product product = booking.getProduct();
            if (product.getQuantity() < booking.getQuantity()) {
                log.info("Insufficient stock to approve bookingId: {}, available: {}, required: {}",
                        id, product.getQuantity(), booking.getQuantity());
                throw new InvalidOperationException("Insufficient stock to approve booking");
            }
            product.setQuantity(product.getQuantity() - booking.getQuantity());
            productRepository.save(product);
            log.info("Stock decremented for productId: {}, new quantity: {}", product.getId(), product.getQuantity());
        }

        booking.setStatus(status);
        Booking updated = bookingRepository.save(booking);
        log.info("Booking status updated, bookingId: {}, status: {}", updated.getId(), updated.getStatus());
        return bookingMapper.toDto(updated);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting bookingId: {}", id);
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("Booking not found with id: {}", id);
                    return new ResourceNotFoundException("Booking not found with id: " + id);
                });
        bookingRepository.delete(booking);
        log.info("Booking deleted, bookingId: {}", id);
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getUser().getId().equals(userId))
                .orElse(false);
    }
}
