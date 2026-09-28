package com.example.housekeeptrack.controller;

import com.example.housekeeptrack.dto.request.RoomRequest;
import com.example.housekeeptrack.dto.request.RoomStatusRequest;
import com.example.housekeeptrack.dto.response.RoomResponse;
import com.example.housekeeptrack.service.RoomService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/rooms")
@Tag(name = "Room Management", description = "APIs for managing hotel rooms and their status lifecycle")
@CrossOrigin(origins = "*")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @PostMapping
    @Operation(summary = "Create a new room", description = "Register a new hotel room in the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Room created successfully"),
        @ApiResponse(responseCode = "400", description = "Validation error"),
        @ApiResponse(responseCode = "409", description = "Room number already exists")
    })
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }

    @GetMapping
    @Operation(summary = "Get all rooms", description = "Retrieve all hotel rooms with their current status")
    public ResponseEntity<List<RoomResponse>> getAllRooms() {
        return ResponseEntity.ok(roomService.getAllRooms());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get room by ID", description = "Retrieve a single room by its ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Room found"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponse> getRoomById(
            @Parameter(description = "Room ID") @PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update room status",
               description = "Transition room to a new status. When set to DIRTY, a cleaning task is automatically created and a housekeeper assigned.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Room status updated"),
        @ApiResponse(responseCode = "400", description = "Invalid status transition"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<RoomResponse> updateRoomStatus(
            @Parameter(description = "Room ID") @PathVariable Long id,
            @Valid @RequestBody RoomStatusRequest request) {
        return ResponseEntity.ok(roomService.updateRoomStatus(id, request.getStatus()));
    }

    @PutMapping("/{id}/allocate")
    @Operation(summary = "Allocate room to guest",
               description = "Attempt to allocate a room to a guest. Only READY rooms can be allocated.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Room is eligible for guest allocation"),
        @ApiResponse(responseCode = "400", description = "Room is not ready for allocation"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<Map<String, Object>> allocateRoom(
            @Parameter(description = "Room ID") @PathVariable Long id) {
        RoomResponse room = roomService.allocateRoom(id);
        return ResponseEntity.ok(Map.of(
                "message", "Room " + room.getRoomNumber() + " is READY and eligible for guest allocation.",
                "room", room
        ));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a room",
               description = "Delete a room. Rooms with active cleaning tasks cannot be deleted.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Room deleted successfully"),
        @ApiResponse(responseCode = "400", description = "Cannot delete room with active task"),
        @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<Void> deleteRoom(
            @Parameter(description = "Room ID") @PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/turnaround/average")
    @Operation(summary = "Get average room turnaround time",
               description = "Calculate the average time (in minutes) from cleaning started to cleaning completed")
    public ResponseEntity<Map<String, Object>> getAverageTurnaround() {
        return ResponseEntity.ok(roomService.getAverageTurnaround());
    }

    @GetMapping("/statistics")
    @Operation(summary = "Get room statistics",
               description = "Get count of rooms in each status")
    public ResponseEntity<Map<String, Long>> getRoomStatistics() {
        return ResponseEntity.ok(roomService.getRoomStatistics());
    }
}
