package com.example.housekeeptrack.dto.request;

import com.example.housekeeptrack.model.enums.InspectionResult;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InspectionRequest {

    @NotBlank(message = "Supervisor name is required")
    @Size(max = 100, message = "Supervisor name must not exceed 100 characters")
    private String supervisorName;

    @NotNull(message = "Inspection result is required (PASSED or FAILED)")
    private InspectionResult result;

    @Size(max = 500, message = "Remarks must not exceed 500 characters")
    private String remarks;
}
