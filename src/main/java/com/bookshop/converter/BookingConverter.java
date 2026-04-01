package com.bookshop.converter;

import com.bookshop.dto.BookingDto;
import com.bookshop.model.Booking;
import com.bookshop.model.Product;
import com.bookshop.model.User;
import org.springframework.stereotype.Component;

@Component
public class BookingConverter {

    public BookingDto entityToDto(Booking booking) {
        BookingDto dto = new BookingDto();
        dto.setId(booking.getId());
        dto.setUserId(booking.getUser().getId());
        dto.setProductId(booking.getProduct().getId());
        dto.setQuantity(booking.getQuantity());
        dto.setStatus(booking.getStatus());
        dto.setCreatedAt(booking.getCreatedAt());
        return dto;
    }

    public Booking dtoToEntity(BookingDto dto, User user, Product product) {
        Booking booking = new Booking();
        booking.setUser(user);
        booking.setProduct(product);
        booking.setQuantity(dto.getQuantity());
        return booking;
    }
}
