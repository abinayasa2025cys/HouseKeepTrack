package com.example.housekeeptrack.controller;

import com.example.housekeeptrack.dto.response.CleaningTaskResponse;
import com.example.housekeeptrack.service.CleaningTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cleaning-tasks")
@Tag(name = "Cleaning Task Management", description = "APIs for managing and tracking cleaning tasks")
@CrossOrigin(origins = "*")
public class CleaningTaskController {

    @Autowired
    private CleaningTaskService cleaningTaskService;

    @GetMapping
    @Operation(summary = "Get all cleaning tasks", description = "Retrieve all cleaning tasks across all rooms")
    public ResponseEntity<List<CleaningTaskResponse>> getAllTasks() {
        return ResponseEntity.ok(cleaningTaskService.getAllTasks());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get cleaning task by ID", description = "Retrieve a specific cleaning task by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<CleaningTaskResponse> getTaskById(
            @Parameter(description = "Cleaning Task ID") @PathVariable Long id) {
        return ResponseEntity.ok(cleaningTaskService.getTaskById(id));
    }

    @PutMapping("/{taskId}/start")
    @Operation(summary = "Start a cleaning task",
               description = "Mark an ASSIGNED cleaning task as IN_PROGRESS. Room status is confirmed as CLEANING.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cleaning started successfully"),
        @ApiResponse(responseCode = "400", description = "Task cannot be started — invalid status"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<CleaningTaskResponse> startTask(
            @Parameter(description = "Cleaning Task ID") @PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningTaskService.startTask(taskId));
    }

    @PutMapping("/{taskId}/complete")
    @Operation(summary = "Complete a cleaning task",
               description = "Mark an IN_PROGRESS task as COMPLETED. Room becomes CLEANED and housekeeper becomes AVAILABLE.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cleaning completed successfully"),
        @ApiResponse(responseCode = "400", description = "Task cannot be completed — invalid status"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<CleaningTaskResponse> completeTask(
            @Parameter(description = "Cleaning Task ID") @PathVariable Long taskId) {
        return ResponseEntity.ok(cleaningTaskService.completeTask(taskId));
    }
}
