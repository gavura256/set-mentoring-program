package com.bookshop.service;

import com.bookshop.converter.BookingConverter;
import com.bookshop.dto.BookingDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Booking;
import com.bookshop.model.Product;
import com.bookshop.model.User;
import com.bookshop.model.enums.BookingStatus;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.ProductRepository;
import com.bookshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private BookingConverter bookingConverter;

    @InjectMocks
    private BookingService bookingService;

    private User user;
    private Product product;
    private Booking booking;
    private BookingDto bookingDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .name("John Doe")
                .role(Role.CUSTOMER)
                .build();

        product = Product.builder()
                .id(1L)
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(new BigDecimal("29.99"))
                .quantity(10)
                .build();

        booking = Booking.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(2)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.of(2024, 1, 1, 12, 0))
                .build();

        bookingDto = BookingDto.builder()
                .id(1L)
                .userId(1L)
                .productId(1L)
                .quantity(2)
                .status(BookingStatus.PENDING)
                .build();
    }

    @Test
    void findAll_returnsAllBookings() {
        Page<Booking> page = new PageImpl<>(List.of(booking));
        when(bookingRepository.findAllWithFetch(any(Pageable.class))).thenReturn(page);
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.findAll(Pageable.unpaged());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void findAll_returnsEmptyListWhenNoBookings() {
        when(bookingRepository.findAllWithFetch(any(Pageable.class))).thenReturn(Page.empty());

        List<BookingDto> result = bookingService.findAll(Pageable.unpaged());

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findByUserId_existingUser_returnsBookings() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(bookingRepository.findByUserIdWithFetch(1L)).thenReturn(List.of(booking));
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.findByUserId(1L);

        assertThat(result).hasSize(1);
    }

    @Test
    void findByUserId_nonExistingUser_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findByUserId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_validDto_setsStatusPendingAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(bookingConverter.dtoToEntity(bookingDto, user, product)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.create(bookingDto);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository).save(booking);
    }

    @Test
    void create_nonExistingUser_throwsNotFoundException() {
        bookingDto.setUserId(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(bookingDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_nonExistingProduct_throwsNotFoundException() {
        bookingDto.setProductId(99L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(bookingDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStatus_pendingBooking_updatesStatus() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.APPROVED);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(bookingRepository).save(booking);
    }

    @Test
    void updateStatus_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateStatus(99L, BookingStatus.APPROVED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStatus_cancelPendingBooking_setsStatusCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.CANCELLED);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    // BookingService.delete() resolves the entity via findById first, then calls
    // delete(entity) — not existsById + deleteById.
    @Test
    void delete_existingId_deletesBooking() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        bookingService.delete(1L);

        verify(bookingRepository).delete(booking);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }
}
