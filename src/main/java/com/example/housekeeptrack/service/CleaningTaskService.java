package com.example.housekeeptrack.service;

import com.example.housekeeptrack.dto.response.CleaningTaskResponse;
import com.example.housekeeptrack.exception.BusinessRuleException;
import com.example.housekeeptrack.exception.ResourceNotFoundException;
import com.example.housekeeptrack.model.CleaningTask;
import com.example.housekeeptrack.model.Housekeeper;
import com.example.housekeeptrack.model.Room;
import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import com.example.housekeeptrack.model.enums.RoomStatus;
import com.example.housekeeptrack.model.enums.TaskStatus;
import com.example.housekeeptrack.repository.CleaningTaskRepository;
import com.example.housekeeptrack.repository.HousekeeperRepository;
import com.example.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CleaningTaskService {

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private HousekeeperRepository housekeeperRepository;

    /**
     * Called automatically when a room becomes DIRTY.
     * Finds an available housekeeper, creates a task, marks housekeeper BUSY, sets room to CLEANING.
     */
    @Transactional
    public CleaningTask createAndAssignTask(Room room) {
        Optional<Housekeeper> availableHousekeeper =
                housekeeperRepository.findFirstByStatusAndActiveTrue(HousekeeperStatus.AVAILABLE);

        CleaningTask task = new CleaningTask();
        task.setRoom(room);
        task.setAssignedAt(LocalDateTime.now());

        if (availableHousekeeper.isPresent()) {
            Housekeeper housekeeper = availableHousekeeper.get();
            task.setHousekeeper(housekeeper);
            task.setStatus(TaskStatus.ASSIGNED);

            housekeeper.setStatus(HousekeeperStatus.BUSY);
            housekeeperRepository.save(housekeeper);

            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
        } else {
            // No housekeeper available: create unassigned task, room stays DIRTY
            task.setHousekeeper(null);
            task.setStatus(TaskStatus.ASSIGNED);
            // Room stays DIRTY — no housekeeper to take the task right now
        }

        return cleaningTaskRepository.save(task);
    }

    public List<CleaningTaskResponse> getAllTasks() {
        return cleaningTaskRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CleaningTaskResponse getTaskById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public CleaningTaskResponse startTask(Long taskId) {
        CleaningTask task = findById(taskId);

        if (task.getStatus() != TaskStatus.ASSIGNED) {
            throw new BusinessRuleException(
                    "Cannot start task. Task is currently in status: " + task.getStatus() +
                    ". Only ASSIGNED tasks can be started.");
        }

        if (task.getHousekeeper() == null) {
            throw new BusinessRuleException(
                    "Cannot start task because no housekeeper is assigned to this task.");
        }

        task.setStatus(TaskStatus.IN_PROGRESS);
        task.setStartedAt(LocalDateTime.now());

        // Ensure room is in CLEANING status
        Room room = task.getRoom();
        room.setStatus(RoomStatus.CLEANING);
        roomRepository.save(room);

        return toResponse(cleaningTaskRepository.save(task));
    }

    @Transactional
    public CleaningTaskResponse completeTask(Long taskId) {
        CleaningTask task = findById(taskId);

        if (task.getStatus() == TaskStatus.COMPLETED) {
            throw new BusinessRuleException("This cleaning task is already completed.");
        }

        if (task.getStatus() == TaskStatus.ASSIGNED) {
            throw new BusinessRuleException(
                    "Cannot complete an unstarted task. Please start the task first.");
        }

        if (task.getStatus() != TaskStatus.IN_PROGRESS) {
            throw new BusinessRuleException(
                    "Cannot complete task. Task must be IN_PROGRESS. Current status: " + task.getStatus());
        }

        task.setStatus(TaskStatus.COMPLETED);
        task.setCompletedAt(LocalDateTime.now());

        // Update room to CLEANED
        Room room = task.getRoom();
        room.setStatus(RoomStatus.CLEANED);
        roomRepository.save(room);

        // Release housekeeper
        if (task.getHousekeeper() != null) {
            Housekeeper housekeeper = task.getHousekeeper();
            housekeeper.setStatus(HousekeeperStatus.AVAILABLE);
            housekeeperRepository.save(housekeeper);
        }

        return toResponse(cleaningTaskRepository.save(task));
    }

    public CleaningTask findById(Long id) {
        return cleaningTaskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CleaningTask", id));
    }

    public CleaningTaskResponse toResponse(CleaningTask task) {
        CleaningTaskResponse response = new CleaningTaskResponse();
        response.setId(task.getId());
        response.setStatus(task.getStatus());
        response.setAssignedAt(task.getAssignedAt());
        response.setStartedAt(task.getStartedAt());
        response.setCompletedAt(task.getCompletedAt());

        if (task.getRoom() != null) {
            response.setRoomId(task.getRoom().getId());
            response.setRoomNumber(task.getRoom().getRoomNumber());
        }
        if (task.getHousekeeper() != null) {
            response.setHousekeeperId(task.getHousekeeper().getId());
            response.setHousekeeperName(task.getHousekeeper().getName());
        }
        return response;
    }
}
