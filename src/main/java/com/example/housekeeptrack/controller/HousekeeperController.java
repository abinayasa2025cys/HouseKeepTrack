package com.example.housekeeptrack.controller;

import com.example.housekeeptrack.dto.request.HousekeeperRequest;
import com.example.housekeeptrack.dto.response.HousekeeperResponse;
import com.example.housekeeptrack.dto.response.HousekeeperWorkloadResponse;
import com.example.housekeeptrack.service.HousekeeperService;
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
@RequestMapping("/api/housekeepers")
@Tag(name = "Housekeeper Management", description = "APIs for managing housekeepers and viewing workload")
@CrossOrigin(origins = "*")
public class HousekeeperController {

    @Autowired
    private HousekeeperService housekeeperService;

    @PostMapping
    @Operation(summary = "Register a new housekeeper", description = "Add a new housekeeper to the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Housekeeper created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error")
    })
    public ResponseEntity<HousekeeperResponse> createHousekeeper(@Valid @RequestBody HousekeeperRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(housekeeperService.createHousekeeper(request));
    }

    @GetMapping
    @Operation(summary = "Get all housekeepers", description = "Retrieve all registered housekeepers")
    public ResponseEntity<List<HousekeeperResponse>> getAllHousekeepers() {
        return ResponseEntity.ok(housekeeperService.getAllHousekeepers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get housekeeper by ID", description = "Retrieve a housekeeper by their ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Housekeeper found"),
        @ApiResponse(responseCode = "404", description = "Housekeeper not found")
    })
    public ResponseEntity<HousekeeperResponse> getHousekeeperById(
            @Parameter(description = "Housekeeper ID") @PathVariable Long id) {
        return ResponseEntity.ok(housekeeperService.getHousekeeperById(id));
    }

    @GetMapping("/workload")
    @Operation(summary = "Get housekeeper workload",
               description = "View assigned, in-progress, and completed task counts for each housekeeper")
    public ResponseEntity<List<HousekeeperWorkloadResponse>> getWorkload() {
        return ResponseEntity.ok(housekeeperService.getWorkload());
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update housekeeper information", description = "Update a housekeeper's name, phone, or active status")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Housekeeper updated successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "404", description = "Housekeeper not found")
    })
    public ResponseEntity<HousekeeperResponse> updateHousekeeper(
            @Parameter(description = "Housekeeper ID") @PathVariable Long id,
            @Valid @RequestBody HousekeeperRequest request) {
        return ResponseEntity.ok(housekeeperService.updateHousekeeper(id, request));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a housekeeper",
               description = "Delete a housekeeper. Cannot be deleted if they have an active cleaning task.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Housekeeper deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete housekeeper with active task"),
        @ApiResponse(responseCode = "404", description = "Housekeeper not found")
    })
    public ResponseEntity<Void> deleteHousekeeper(
            @Parameter(description = "Housekeeper ID") @PathVariable Long id) {
        housekeeperService.deleteHousekeeper(id);
        return ResponseEntity.noContent().build();
    }
}
