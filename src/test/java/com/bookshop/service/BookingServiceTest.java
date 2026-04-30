package com.bookshop.service;

import com.bookshop.mapper.BookingMapper;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    private BookingMapper bookingMapper;

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
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        Page<BookingDto> result = bookingService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().getFirst().getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void findAll_returnsEmptyListWhenNoBookings() {
        when(bookingRepository.findAllWithFetch(any(Pageable.class))).thenReturn(Page.empty());

        Page<BookingDto> result = bookingService.findAll(Pageable.unpaged());

        assertThat(result.getContent()).isEmpty();
    }

    @Test
    void findById_existingId_returnsDto() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

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
        when(bookingRepository.findByUserIdWithFetch(eq(1L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(booking)));
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        Page<BookingDto> result = bookingService.findByUserId(1L, Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void findByUserId_nonExistingUser_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.findByUserId(99L, Pageable.unpaged()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_validDto_setsStatusPendingAndSaves() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(bookingMapper.toEntity(bookingDto, user, product)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        BookingDto result = bookingService.create(bookingDto);

        assertThat(result).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
        verify(bookingRepository).save(booking);
    }

    @Test
    void create_validDto_decrementsProductStock() {
        product.setQuantity(10);
        bookingDto.setQuantity(3);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(bookingMapper.toEntity(bookingDto, user, product)).thenReturn(booking);
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        bookingService.create(bookingDto);

        assertThat(product.getQuantity()).isEqualTo(7);
        verify(productRepository).save(product);
    }

    @Test
    void create_insufficientStock_doesNotDecrementStock() {
        product.setQuantity(1);
        bookingDto.setQuantity(5);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> bookingService.create(bookingDto))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Insufficient stock");

        verify(productRepository, never()).save(any());
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
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.create(bookingDto))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void updateStatus_pendingToApproved_doesNotChangeStock() {
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.APPROVED);

        assertThat(booking.getStatus()).isEqualTo(BookingStatus.APPROVED);
        verify(productRepository, never()).save(any());
    }

    @Test
    void updateStatus_cancelledBooking_restoresProductStock() {
        product.setQuantity(7);
        booking.setQuantity(3);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.CANCELLED);

        assertThat(product.getQuantity()).isEqualTo(10);
        verify(productRepository).save(product);
    }

    @Test
    void updateStatus_rejectedBooking_restoresProductStock() {
        product.setQuantity(7);
        booking.setQuantity(3);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.REJECTED);

        assertThat(product.getQuantity()).isEqualTo(10);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.REJECTED);
        verify(productRepository).save(product);
    }

    @Test
    void updateStatus_approvedThenCancelled_restoresStock() {
        booking.setStatus(BookingStatus.APPROVED);
        product.setQuantity(7);
        booking.setQuantity(3);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));
        when(bookingRepository.save(booking)).thenReturn(booking);
        when(bookingMapper.toDto(booking)).thenReturn(bookingDto);

        bookingService.updateStatus(1L, BookingStatus.CANCELLED);

        assertThat(product.getQuantity()).isEqualTo(10);
        verify(productRepository).save(product);
    }

    @Test
    void updateStatus_invalidTransition_cancelledToApproved_throws() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1L, BookingStatus.APPROVED))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_invalidTransition_rejectedToPending_throws() {
        booking.setStatus(BookingStatus.REJECTED);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1L, BookingStatus.PENDING))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_sameStatus_pendingToPending_throws() {
        booking.setStatus(BookingStatus.PENDING);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));

        assertThatThrownBy(() -> bookingService.updateStatus(1L, BookingStatus.PENDING))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Invalid status transition");
    }

    @Test
    void updateStatus_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.updateStatus(99L, BookingStatus.APPROVED))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_pendingBooking_restoresStock() {
        product.setQuantity(7);
        booking.setQuantity(3);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        bookingService.delete(1L);

        assertThat(product.getQuantity()).isEqualTo(10);
        verify(productRepository).save(product);
        verify(bookingRepository).delete(booking);
    }

    @Test
    void delete_approvedBooking_restoresStock() {
        booking.setStatus(BookingStatus.APPROVED);
        product.setQuantity(7);
        booking.setQuantity(3);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(product));

        bookingService.delete(1L);

        assertThat(product.getQuantity()).isEqualTo(10);
        verify(productRepository).save(product);
        verify(bookingRepository).delete(booking);
    }

    @Test
    void delete_cancelledBooking_doesNotRestoreStock() {
        booking.setStatus(BookingStatus.CANCELLED);
        when(bookingRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(booking));

        bookingService.delete(1L);

        verify(productRepository, never()).save(any());
        verify(bookingRepository).delete(booking);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(bookingRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> bookingService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void isOwner_bookingOwnedByUser_returnsTrue() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.isOwner(1L, 1L)).isTrue();
    }

    @Test
    void isOwner_bookingNotOwnedByUser_returnsFalse() {
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

        assertThat(bookingService.isOwner(1L, 99L)).isFalse();
    }

    @Test
    void isOwner_nonExistingBooking_returnsFalse() {
        when(bookingRepository.findById(99L)).thenReturn(Optional.empty());

        assertThat(bookingService.isOwner(99L, 1L)).isFalse();
    }
}
