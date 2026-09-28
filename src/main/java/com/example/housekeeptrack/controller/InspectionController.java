package com.example.housekeeptrack.controller;

import com.example.housekeeptrack.dto.request.InspectionRequest;
import com.example.housekeeptrack.dto.response.InspectionResponse;
import com.example.housekeeptrack.service.InspectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Inspection Management", description = "APIs for room inspections by supervisors")
@CrossOrigin(origins = "*")
public class InspectionController {

    @Autowired
    private InspectionService inspectionService;

    @PostMapping("/{roomId}/inspections")
    @Operation(summary = "Submit a room inspection",
               description = "Supervisor submits inspection result. PASSED → room becomes READY. FAILED → room goes back to CLEANING with a new task.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Inspection submitted successfully"),
        @ApiResponse(responseCode = "400", description = "Room not in CLEANED status or validation error"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<InspectionResponse> createInspection(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Valid @RequestBody InspectionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(inspectionService.createInspection(roomId, request));
    }

    @GetMapping("/{roomId}/inspections")
    @Operation(summary = "Get inspection history for a room",
               description = "Retrieve all inspection records for a specific room, ordered by most recent first")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Inspection history retrieved"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<List<InspectionResponse>> getInspections(
            @Parameter(description = "Room ID") @PathVariable Long roomId) {
        return ResponseEntity.ok(inspectionService.getInspectionsByRoom(roomId));
    }
}
