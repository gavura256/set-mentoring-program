package com.bookshop.converter;

import com.bookshop.dto.UserDto;
import com.bookshop.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserConverter {

    public UserDto entityToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setEmail(user.getEmail());
        dto.setName(user.getName());
        dto.setRole(user.getRole());
        return dto;
    }

    public User dtoToEntity(UserDto dto) {
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setName(dto.getName());
        user.setRole(dto.getRole());
        return user;
    }
}
