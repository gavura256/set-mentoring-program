package com.bookshop.mapper;

import com.bookshop.dto.UserRequest;
import com.bookshop.dto.UserResponse;
import com.bookshop.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toResponse(User user);

    @Mapping(target = "id", ignore = true)
    User toEntity(UserRequest dto);
}
