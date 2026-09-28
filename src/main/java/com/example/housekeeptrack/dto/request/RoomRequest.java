package com.example.housekeeptrack.dto.request;

import com.example.housekeeptrack.model.enums.RoomStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoomRequest {

    @NotBlank(message = "Room number is required")
    @Size(max = 20, message = "Room number must not exceed 20 characters")
    private String roomNumber;

    @NotBlank(message = "Room type is required")
    @Size(max = 50, message = "Room type must not exceed 50 characters")
    private String roomType;

    private RoomStatus status;
}
