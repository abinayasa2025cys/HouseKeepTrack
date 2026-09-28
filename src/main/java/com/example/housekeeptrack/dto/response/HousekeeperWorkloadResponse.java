package com.example.housekeeptrack.dto.response;

import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import lombok.Data;

@Data
public class HousekeeperWorkloadResponse {

    private Long id;
    private String name;
    private HousekeeperStatus status;
    private boolean active;
    private long assignedTasks;
    private long inProgressTasks;
    private long completedTasks;
}
