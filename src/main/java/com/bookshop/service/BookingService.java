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
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

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

    public List<BookingDto> findAll() {
        return bookingRepository.findAll().stream()
                .map(bookingConverter::entityToDto)
                .collect(Collectors.toList());
    }

    public BookingDto findById(Long id) {
        return bookingRepository.findById(id)
                .stream()
                .findAny()
                .map(bookingConverter::entityToDto)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
    }

    public List<BookingDto> findByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        return bookingRepository.findByUserId(userId).stream()
                .map(bookingConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public BookingDto create(BookingDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + dto.getUserId()));
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + dto.getProductId()));
        Booking booking = bookingConverter.dtoToEntity(dto, user, product);
        booking.setStatus(BookingStatus.PENDING);
        return bookingConverter.entityToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto updateStatus(Long id, BookingStatus status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidOperationException("Cannot update a cancelled booking");
        }
        booking.setStatus(status);
        return bookingConverter.entityToDto(bookingRepository.save(booking));
    }

    @Transactional
    public BookingDto cancel(Long id) {
        return updateStatus(id, BookingStatus.CANCELLED);
    }

    @Transactional
    public void delete(Long id) {
        bookingRepository.delete(
                bookingRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id))
        );
    }
}
