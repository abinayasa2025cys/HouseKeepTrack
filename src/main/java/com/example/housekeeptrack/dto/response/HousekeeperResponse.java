package com.example.housekeeptrack.dto.response;

import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import lombok.Data;

@Data
public class HousekeeperResponse {

    private Long id;
    private String name;
    private String phone;
    private HousekeeperStatus status;
    private boolean active;
}
