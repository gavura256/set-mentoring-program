package com.bookshop.service;

import com.bookshop.converter.UserConverter;
import com.bookshop.dto.UserDto;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.model.User;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserConverter userConverter;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(userConverter::entityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userConverter.entityToDto(user);
    }

    @Transactional
    public UserDto create(UserDto dto) {
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new ResourceAlreadyExistsException("An account with this email already exists");
        }

        User user = userConverter.dtoToEntity(dto);

        // Only ADMINISTRATOR can specify a role during creation
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = auth != null && auth.isAuthenticated() &&
                auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));

        if (!isAdmin || user.getRole() == null) {
            user.setRole(Role.CUSTOMER);
        }

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        } else {
            throw new InvalidOperationException("Password is required for user registration");
        }
        User saved = userRepository.save(user);
        return userConverter.entityToDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));

        // Role update logic: ONLY ADMINISTRATOR can change a user's role
        if (dto.getRole() != null && dto.getRole() != user.getRole()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.isAuthenticated() &&
                    auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));

            if (!isAdmin) {
                throw new AccessDeniedException("Only an Administrator can change user roles");
            }
            user.setRole(dto.getRole());
        }

        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                throw new ResourceAlreadyExistsException("An account with this email already exists");
            }
        }

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        return userConverter.entityToDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        // Check if user has any bookings
        var bookings = bookingRepository.findByUserIdWithFetch(id);
        if (!bookings.isEmpty()) {
            throw new InvalidOperationException("Cannot delete user with existing bookings. Please delete all bookings for this user first.");
        }
        userRepository.deleteById(id);
    }
}
