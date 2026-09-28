package com.example.housekeeptrack.dto.response;

import com.example.housekeeptrack.model.enums.InspectionResult;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InspectionResponse {

    private Long id;
    private Long roomId;
    private String roomNumber;
    private String supervisorName;
    private InspectionResult result;
    private String remarks;
    private LocalDateTime inspectedAt;
}
