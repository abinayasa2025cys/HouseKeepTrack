package com.example.housekeeptrack.service;

import com.example.housekeeptrack.dto.request.InspectionRequest;
import com.example.housekeeptrack.dto.response.InspectionResponse;
import com.example.housekeeptrack.exception.BusinessRuleException;
import com.example.housekeeptrack.exception.ResourceNotFoundException;
import com.example.housekeeptrack.model.CleaningTask;
import com.example.housekeeptrack.model.Inspection;
import com.example.housekeeptrack.model.Room;
import com.example.housekeeptrack.model.enums.InspectionResult;
import com.example.housekeeptrack.model.enums.RoomStatus;
import com.example.housekeeptrack.repository.InspectionRepository;
import com.example.housekeeptrack.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InspectionService {

    @Autowired
    private InspectionRepository inspectionRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private RoomService roomService;

    @Autowired
    private CleaningTaskService cleaningTaskService;

    @Transactional
    public InspectionResponse createInspection(Long roomId, InspectionRequest request) {
        Room room = roomService.findById(roomId);

        // Room must be CLEANED to be eligible for inspection
        if (room.getStatus() != RoomStatus.CLEANED) {
            throw new BusinessRuleException(
                    "Room " + room.getRoomNumber() + " cannot be inspected. " +
                    "Room must be in CLEANED status before inspection. Current status: " + room.getStatus() + ".");
        }

        // If FAILED, remarks are mandatory
        if (request.getResult() == InspectionResult.FAILED) {
            if (request.getRemarks() == null || request.getRemarks().isBlank()) {
                throw new BusinessRuleException(
                        "Remarks are required when an inspection fails. " +
                        "Please provide details of what needs to be fixed.");
            }
        }

        // Save inspection record
        Inspection inspection = new Inspection();
        inspection.setRoom(room);
        inspection.setSupervisorName(request.getSupervisorName());
        inspection.setResult(request.getResult());
        inspection.setRemarks(request.getRemarks());
        inspection.setInspectedAt(LocalDateTime.now());
        inspection = inspectionRepository.save(inspection);

        // Apply workflow based on result
        if (request.getResult() == InspectionResult.PASSED) {
            // CLEANED → INSPECTED → READY
            room.setStatus(RoomStatus.INSPECTED);
            roomRepository.save(room);
            room.setStatus(RoomStatus.READY);
            roomRepository.save(room);
        } else {
            // CLEANED → CLEANING → new cleaning task
            room.setStatus(RoomStatus.CLEANING);
            roomRepository.save(room);
            // Create and assign new cleaning task
            CleaningTask newTask = cleaningTaskService.createAndAssignTask(room);
            // createAndAssignTask may change room status to CLEANING if housekeeper assigned
        }

        return toResponse(inspection);
    }

    public List<InspectionResponse> getInspectionsByRoom(Long roomId) {
        // Verify room exists
        roomService.findById(roomId);
        return inspectionRepository.findByRoomIdOrderByInspectedAtDesc(roomId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    private InspectionResponse toResponse(Inspection inspection) {
        InspectionResponse response = new InspectionResponse();
        response.setId(inspection.getId());
        response.setRoomId(inspection.getRoom().getId());
        response.setRoomNumber(inspection.getRoom().getRoomNumber());
        response.setSupervisorName(inspection.getSupervisorName());
        response.setResult(inspection.getResult());
        response.setRemarks(inspection.getRemarks());
        response.setInspectedAt(inspection.getInspectedAt());
        return response;
    }
}
