package com.example.housekeeptrack.dto.response;

import com.example.housekeeptrack.model.enums.TaskStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CleaningTaskResponse {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private Long housekeeperId;
    private String housekeeperName;
    private TaskStatus status;
    private LocalDateTime assignedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
}
