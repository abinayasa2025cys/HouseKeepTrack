package com.example.housekeeptrack.dto.response;

import com.example.housekeeptrack.model.enums.RoomStatus;
import lombok.Data;

@Data
public class RoomResponse {

    private Long id;
    private String roomNumber;
    private String roomType;
    private RoomStatus status;
}
