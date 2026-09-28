package com.example.housekeeptrack.dto.request;

import com.example.housekeeptrack.model.enums.RoomStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RoomStatusRequest {

    @NotNull(message = "Status is required")
    private RoomStatus status;
}
