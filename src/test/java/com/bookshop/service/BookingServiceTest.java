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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
                .build();

        booking = Booking.builder()
                .id(1L)
                .user(user)
                .product(product)
                .quantity(2)
                .status(BookingStatus.PENDING)
                .createdAt(LocalDateTime.now())
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
        when(bookingRepository.findAll()).thenReturn(List.of(booking));
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void findById_existingId_returnsDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByUserId_existingUser_returnsBookings() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByUserId(1L)).thenReturn(List.of(booking));
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        List<BookingDto> result = bookingService.findByUserId(1L);

        assertThat(result).hasSize(1);
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
        verify(bookingRepository).save(booking);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void create_nonExistingUser_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        bookingDto.setUserId(99L);

        assertThatThrownBy(() -> bookingService.create(bookingDto))
                .isInstanceOf(ResourceNotFoundException.class);
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
    void updateStatus_cancelledBooking_throwsInvalidOperationException() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1L, BookingStatus.APPROVED))
                .isInstanceOf(InvalidOperationException.class);
    }

    @Test
    void cancel_pendingBooking_setsStatusCancelled() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingConverter.entityToDto(booking)).thenReturn(bookingDto);

        bookingService.cancel(1L);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void delete_existingId_deletesBooking() {
        when(bookingRepository.existsById(1L)).thenReturn(true);

        bookingService.delete(1L);

        verify(bookingRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> bookingService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
