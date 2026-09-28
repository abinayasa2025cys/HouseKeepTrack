package com.example.housekeeptrack.service;

import com.example.housekeeptrack.dto.request.RoomRequest;
import com.example.housekeeptrack.dto.response.RoomResponse;
import com.example.housekeeptrack.exception.BusinessRuleException;
import com.example.housekeeptrack.exception.InvalidStateException;
import com.example.housekeeptrack.exception.ResourceNotFoundException;
import com.example.housekeeptrack.model.CleaningTask;
import com.example.housekeeptrack.model.Room;
import com.example.housekeeptrack.model.enums.RoomStatus;
import com.example.housekeeptrack.model.enums.TaskStatus;
import com.example.housekeeptrack.repository.CleaningTaskRepository;
import com.example.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private CleaningTaskService cleaningTaskService;

    // Valid status transitions
    private static final Map<RoomStatus, Set<RoomStatus>> VALID_TRANSITIONS = Map.of(
            RoomStatus.DIRTY,    EnumSet.of(RoomStatus.CLEANING),
            RoomStatus.CLEANING, EnumSet.of(RoomStatus.CLEANED),
            RoomStatus.CLEANED,  EnumSet.of(RoomStatus.INSPECTED, RoomStatus.CLEANING), // CLEANING only via failed inspection
            RoomStatus.INSPECTED,EnumSet.of(RoomStatus.READY),
            RoomStatus.READY,    EnumSet.of(RoomStatus.DIRTY)   // Guest checks out → room becomes DIRTY
    );

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new InvalidStateException(
                    "Room with number '" + request.getRoomNumber() + "' already exists.");
        }

        Room room = new Room();
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(request.getRoomType());
        RoomStatus initialStatus = request.getStatus() != null ? request.getStatus() : RoomStatus.READY;
        room.setStatus(initialStatus);
        room = roomRepository.save(room);

        // If created as DIRTY, auto-create and assign a cleaning task
        if (initialStatus == RoomStatus.DIRTY) {
            cleaningTaskService.createAndAssignTask(room);
            room = findById(room.getId()); // re-fetch to get updated status
        }

        return toResponse(room);
    }

    public List<RoomResponse> getAllRooms() {
        return roomRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public RoomResponse getRoomById(Long id) {
        return toResponse(findById(id));
    }

    /**
     * Manual status transition endpoint. Handles DIRTY trigger for auto task creation.
     * CLEANED → CLEANING and CLEANED → INSPECTED transitions are protected by business rules.
     */
    @Transactional
    public RoomResponse updateRoomStatus(Long id, RoomStatus newStatus) {
        Room room = findById(id);
        RoomStatus currentStatus = room.getStatus();

        // Block CLEANED → READY: must go through inspection
        if (currentStatus == RoomStatus.CLEANED && newStatus == RoomStatus.READY) {
            throw new BusinessRuleException(
                    "Room cannot be marked as ready because it has not passed inspection. " +
                    "A room must be INSPECTED (passed inspection) before it can be marked READY.");
        }

        // Block CLEANED → CLEANING via manual request (only allowed via failed inspection)
        if (currentStatus == RoomStatus.CLEANED && newStatus == RoomStatus.CLEANING) {
            throw new BusinessRuleException(
                    "Room cannot be manually set to CLEANING from CLEANED. " +
                    "This transition only occurs automatically when an inspection fails.");
        }

        // Block CLEANED → INSPECTED via manual request (must go through inspection endpoint)
        if (currentStatus == RoomStatus.CLEANED && newStatus == RoomStatus.INSPECTED) {
            throw new BusinessRuleException(
                    "Room cannot be manually marked as INSPECTED. " +
                    "Please submit an inspection through POST /api/rooms/" + id + "/inspections");
        }

        // Validate transition
        Set<RoomStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, EnumSet.noneOf(RoomStatus.class));
        if (!allowed.contains(newStatus)) {
            throw new BusinessRuleException(
                    "Invalid room status transition from " + currentStatus + " to " + newStatus + ".");
        }

        // If becoming DIRTY, auto-create cleaning task and set to CLEANING if housekeeper available
        if (newStatus == RoomStatus.DIRTY) {
            room.setStatus(RoomStatus.DIRTY);
            roomRepository.save(room);
            // createAndAssignTask may set room to CLEANING internally if housekeeper found
            cleaningTaskService.createAndAssignTask(room);
            // Re-fetch room to get updated status
            room = findById(id);
            return toResponse(room);
        }

        room.setStatus(newStatus);
        return toResponse(roomRepository.save(room));
    }

    /**
     * Guest allocation — only allowed when room is READY.
     */
    @Transactional
    public RoomResponse allocateRoom(Long id) {
        Room room = findById(id);

        if (room.getStatus() != RoomStatus.READY) {
            throw new BusinessRuleException(
                    "Room " + room.getRoomNumber() + " cannot be allocated to a guest. " +
                    "Room must be READY. Current status: " + room.getStatus() + ".");
        }

        // Mark as DIRTY (guest check-in → will need cleaning on checkout)
        // For demo: just confirm allocation eligibility without changing status
        // (A real system would move it to OCCUPIED, but we have no OCCUPIED status per spec)
        return toResponse(room);
    }

    @Transactional
    public void deleteRoom(Long id) {
        Room room = findById(id);

        boolean hasActiveTask = cleaningTaskRepository.existsByRoomIdAndStatusIn(
                id, List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS));

        if (hasActiveTask) {
            throw new BusinessRuleException(
                    "Cannot delete room " + room.getRoomNumber() +
                    " because it has an active cleaning task in progress.");
        }

        roomRepository.delete(room);
    }

    /**
     * Calculate average turnaround time: from startedAt to completedAt for COMPLETED tasks.
     */
    public Map<String, Object> getAverageTurnaround() {
        List<CleaningTask> completedTasks = cleaningTaskRepository.findByStatus(TaskStatus.COMPLETED);

        List<CleaningTask> validTasks = completedTasks.stream()
                .filter(t -> t.getStartedAt() != null && t.getCompletedAt() != null)
                .collect(Collectors.toList());

        if (validTasks.isEmpty()) {
            return Map.of(
                    "averageTurnaroundMinutes", 0.0,
                    "totalCompletedTasks", 0,
                    "message", "No completed tasks with timing data found."
            );
        }

        double avgMinutes = validTasks.stream()
                .mapToLong(t -> Duration.between(t.getStartedAt(), t.getCompletedAt()).toSeconds())
                .average()
                .orElse(0.0) / 60.0;

        return Map.of(
                "averageTurnaroundMinutes", Math.round(avgMinutes * 10.0) / 10.0,
                "totalCompletedTasks", validTasks.size()
        );
    }

    public Map<String, Long> getRoomStatistics() {
        return Map.of(
                "total",    roomRepository.count(),
                "dirty",    roomRepository.countByStatus(RoomStatus.DIRTY),
                "cleaning", roomRepository.countByStatus(RoomStatus.CLEANING),
                "cleaned",  roomRepository.countByStatus(RoomStatus.CLEANED),
                "inspected",roomRepository.countByStatus(RoomStatus.INSPECTED),
                "ready",    roomRepository.countByStatus(RoomStatus.READY)
        );
    }

    public Room findById(Long id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    public RoomResponse toResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setRoomType(room.getRoomType());
        response.setStatus(room.getStatus());
        return response;
    }
}
