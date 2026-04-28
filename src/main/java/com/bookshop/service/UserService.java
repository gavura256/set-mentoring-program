package com.bookshop.service;

import com.bookshop.mapper.UserMapper;
import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.exception.InvalidOperationException;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.User;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.BookingRepository;
import com.bookshop.repository.UserRepository;
import com.bookshop.util.SanitizerUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponse> findAll() {
        log.debug("Fetching all users");
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse findById(Long id) {
        log.debug("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse create(UserRequest dto) {
        log.info("Creating user, email: '{}'", dto.getEmail());
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            log.info("User already exists with email: '{}'", dto.getEmail());
            throw new ResourceAlreadyExistsException("An account with this email already exists");
        }

        User user = userMapper.toEntity(dto);
        user.setName(SanitizerUtils.sanitize(user.getName()));

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
            log.info("Password missing for new user with email: '{}'", dto.getEmail());
            throw new InvalidOperationException("Password is required for user registration");
        }
        User saved = userRepository.save(user);
        log.info("User created with id: {}, role: {}", saved.getId(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public UserResponse update(Long id, UserRequest dto) {
        log.info("Updating userId: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.info("User not found with id: {}", id);
                    return new ResourceNotFoundException("User not found with id: " + id);
                });

        // Role update logic: ONLY ADMINISTRATOR can change a user's role
        if (dto.getRole() != null && dto.getRole() != user.getRole()) {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAdmin = auth != null && auth.isAuthenticated() &&
                    auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRATOR"));

            if (!isAdmin) {
                log.warn("Unauthorized role change attempt on userId: {} by non-admin", id);
                throw new AccessDeniedException("Only an Administrator can change user roles");
            }
            user.setRole(dto.getRole());
        }

        if (!user.getEmail().equals(dto.getEmail())) {
            if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
                log.info("Email already in use: '{}' when updating userId: {}", dto.getEmail(), id);
                throw new ResourceAlreadyExistsException("An account with this email already exists");
            }
        }

        dto.setName(SanitizerUtils.sanitize(dto.getName()));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }
        User saved = userRepository.save(user);
        log.info("User updated, userId: {}, role: {}", saved.getId(), saved.getRole());
        return userMapper.toResponse(saved);
    }

    @Transactional
    public void delete(Long id) {
        log.info("Deleting userId: {}", id);
        if (!userRepository.existsById(id)) {
            log.info("User not found with id: {}", id);
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        var bookings = bookingRepository.findByUserIdWithFetch(id);
        if (!bookings.isEmpty()) {
            log.info("Cannot delete userId: {} — existing bookings present", id);
            throw new InvalidOperationException("Cannot delete user with existing bookings. Please delete all bookings for this user first.");
        }
        userRepository.deleteById(id);
        log.info("User deleted, userId: {}", id);
    }
}
