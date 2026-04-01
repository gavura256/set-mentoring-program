package com.bookshop.converter;

import com.bookshop.dto.BookingDto;
import com.bookshop.model.Booking;
import com.bookshop.model.Product;
import com.bookshop.model.User;
import org.springframework.stereotype.Component;

@Component
public class BookingConverter {

    public BookingDto entityToDto(Booking booking) {
        return BookingDto.builder()
                .id(booking.getId())
                .userId(booking.getUser().getId())
                .productId(booking.getProduct().getId())
                .quantity(booking.getQuantity())
                .status(booking.getStatus())
                .createdAt(booking.getCreatedAt())
                .build();
    }

    public Booking dtoToEntity(BookingDto dto, User user, Product product) {
        return Booking.builder()
                .user(user)
                .product(product)
                .quantity(dto.getQuantity())
                .build();
    }
}
