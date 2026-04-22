package com.bookshop.mapper;

import com.bookshop.dto.BookingDto;
import com.bookshop.model.Booking;
import com.bookshop.model.Product;
import com.bookshop.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    @Mapping(source = "user.id", target = "userId")
    @Mapping(source = "product.id", target = "productId")
    BookingDto toDto(Booking booking);

    @Mapping(source = "dto.quantity", target = "quantity")
    @Mapping(source = "user", target = "user")
    @Mapping(source = "product", target = "product")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    Booking toEntity(BookingDto dto, User user, Product product);
}
