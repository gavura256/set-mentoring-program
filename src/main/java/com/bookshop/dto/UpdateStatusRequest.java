package com.bookshop.dto;

import com.bookshop.model.enums.BookingStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateStatusRequest {

    @NotNull
    private BookingStatus status;
}
