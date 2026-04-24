package com.bookshop.service;

import com.bookshop.mapper.UserMapper;
import com.bookshop.dto.UserDto;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.Booking;
import org.springframework.security.access.AccessDeniedException;
import com.bookshop.model.User;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .email("john@example.com")
                .name("John Doe")
                .role(Role.CUSTOMER)
                .build();

        userDto = UserDto.builder()
                .id(1L)
                .email("john@example.com")
                .name("John Doe")
                .password("password")
                .role(Role.CUSTOMER)
                .build();
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findAll_returnsEmptyListWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_existingId_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void create_newEmail_returnsCreatedDto() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDto)).thenReturn(user);
        when(passwordEncoder.encode("password")).thenReturn("encoded");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
        verify(userRepository).save(user);
    }

    @Test
    void create_duplicateEmail_throwsAlreadyExistsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("An account with this email already exists");
    }

    // UserService.update() mutates the fetched entity in-place (setName, setEmail, setRole),
    // so save() receives the same object reference — the stub matches correctly.
    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void update_existingId_updatesUser() {
        UserDto updateDto = UserDto.builder()
                .name("Jane Doe")
                .email("jane@example.com")
                .role(Role.MANAGER)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(userDto);

        UserDto result = userService.update(1L, updateDto);

        assertThat(user.getName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getRole()).isEqualTo(Role.MANAGER);
        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void update_nonExistingId_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.update(99L, UserDto.builder().email("test@example.com").name("Test").build()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void delete_existingId_deletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByUserIdWithFetch(1L)).thenReturn(Collections.emptyList());

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_nullPassword_throwsInvalidOperationException() {
        userDto.setPassword(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userMapper.toEntity(userDto)).thenReturn(user);

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Password is required");
    }

    @Test
    void delete_userWithBookings_throwsInvalidOperationException() {
        when(userRepository.existsById(1L)).thenReturn(true);
        when(bookingRepository.findByUserIdWithFetch(1L)).thenReturn(List.of(Booking.builder().build()));

        assertThatThrownBy(() -> userService.delete(1L))
                .isInstanceOf(InvalidOperationException.class)
                .hasMessageContaining("Cannot delete user with existing bookings");
    }

    @Test
    @WithMockUser(roles = "ADMINISTRATOR")
    void update_duplicateEmail_throwsAlreadyExistsException() {
        UserDto updateDto = UserDto.builder()
                .name("John Doe")
                .email("other@example.com")
                .build();
        User other = User.builder().id(2L).email("other@example.com").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> userService.update(1L, updateDto))
                .isInstanceOf(ResourceAlreadyExistsException.class)
                .hasMessageContaining("email already exists");
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    void update_roleChangeByNonAdmin_throwsAccessDeniedException() {
        UserDto updateDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .role(Role.ADMINISTRATOR)
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.update(1L, updateDto))
                .isInstanceOf(AccessDeniedException.class);
    }
}
