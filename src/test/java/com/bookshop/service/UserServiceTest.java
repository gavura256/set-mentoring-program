package com.bookshop.service;

import com.bookshop.converter.UserConverter;
import com.bookshop.dto.UserDto;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.User;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserConverter userConverter;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("john@example.com");
        user.setName("John Doe");
        user.setRole(Role.CUSTOMER);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setEmail("john@example.com");
        userDto.setName("John Doe");
        userDto.setRole(Role.CUSTOMER);
    }

    @Test
    void findAll_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));
        when(userConverter.entityToDto(user)).thenReturn(userDto);

        List<UserDto> result = userService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void findById_existingId_returnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userConverter.entityToDto(user)).thenReturn(userDto);

        UserDto result = userService.findById(1L);

        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void findById_nonExistingId_throwsNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_newEmail_returnsCreatedDto() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userConverter.dtoToEntity(userDto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userConverter.entityToDto(user)).thenReturn(userDto);

        UserDto result = userService.create(userDto);

        assertThat(result.getEmail()).isEqualTo("john@example.com");
    }

    @Test
    void create_duplicateEmail_throwsAlreadyExistsException() {
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.create(userDto))
                .isInstanceOf(ResourceAlreadyExistsException.class);
    }

    @Test
    void update_existingId_updatesUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userConverter.entityToDto(user)).thenReturn(userDto);

        UserDto result = userService.update(1L, userDto);

        assertThat(result).isNotNull();
        verify(userRepository).save(user);
    }

    @Test
    void delete_existingId_deletesUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.delete(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void delete_nonExistingId_throwsNotFoundException() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> userService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
