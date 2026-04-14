package com.bookshop.service;

import com.bookshop.converter.UserConverter;
import com.bookshop.dto.UserDto;
import com.bookshop.exception.ResourceAlreadyExistsException;
import com.bookshop.exception.ResourceNotFoundException;
import com.bookshop.model.User;
import com.bookshop.model.enums.Role;
import com.bookshop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserConverter userConverter;

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
        if (dto.getRole() == null) {
            dto.setRole(Role.CUSTOMER);
        }
        User saved = userRepository.save(userConverter.dtoToEntity(dto));
        return userConverter.entityToDto(saved);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setRole(dto.getRole());
        return userConverter.entityToDto(userRepository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
