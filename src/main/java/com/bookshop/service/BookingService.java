package com.bookshop.service;

import com.bookshop.converter.BookingConverter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private BookingConverter bookingConverter;

    @Transactional(readOnly = true)
    public List<BookingDto> findAll(Pageable pageable) {
        return bookingRepository.findAllWithFetch(pageable).getContent().stream()
                .map(bookingConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public BookingDto findById(Long id) {
        return bookingRepository.findById(id)
                .map(bookingConverter::entityToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<BookingDto> findByUserId(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return bookingRepository.findByUserIdWithFetch(userId).stream()
                .map(bookingConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto create(BookingDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));

        if (product.getQuantity() < dto.getQuantity()) {
            throw new InvalidOperationException("Insufficient stock for product: " + product.getTitle());
        }

        Booking booking = bookingConverter.dtoToEntity(dto, user, product);
        booking.setStatus(BookingStatus.PENDING);
        return bookingConverter.entityToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));

        if (status == BookingStatus.APPROVED) {
            Product product = booking.getProduct();
            if (product.getQuantity() < booking.getQuantity()) {
                throw new InvalidOperationException("Insufficient stock to approve booking");
            }
            product.setQuantity(product.getQuantity() - booking.getQuantity());
            productRepository.save(product);
        }

        booking.setStatus(status);
        return bookingConverter.entityToDto(bookingRepository.save(booking));
    }

    @Transactional
    public void delete(Long id) {
        bookingRepository.delete(
                bookingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id))
        );
    }

    @Transactional(readOnly = true)
    public boolean isOwner(Long bookingId, Long userId) {
        return bookingRepository.findById(bookingId)
                .map(booking -> booking.getUser().getId().equals(userId))
                .orElse(false);
    }
}
