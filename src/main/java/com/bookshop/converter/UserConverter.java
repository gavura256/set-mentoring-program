package com.bookshop.converter;

import com.bookshop.dto.UserDto;
import com.bookshop.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserDto entityToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .role(user.getRole())
                .build();
    }

    public User dtoToEntity(UserDto dto) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(dto.getPassword())
                .role(dto.getRole())
                .build();
    }
}
