package com.example.housekeeptrack.service;

import com.example.housekeeptrack.dto.request.HousekeeperRequest;
import com.example.housekeeptrack.dto.response.HousekeeperResponse;
import com.example.housekeeptrack.dto.response.HousekeeperWorkloadResponse;
import com.example.housekeeptrack.exception.BusinessRuleException;
import com.example.housekeeptrack.exception.ResourceNotFoundException;
import com.example.housekeeptrack.model.Housekeeper;
import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import com.example.housekeeptrack.model.enums.TaskStatus;
import com.example.housekeeptrack.repository.CleaningTaskRepository;
import com.example.housekeeptrack.repository.HousekeeperRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class HousekeeperService {

    @Autowired
    private HousekeeperRepository housekeeperRepository;

    @Autowired
    private CleaningTaskRepository cleaningTaskRepository;

    @Transactional
    public HousekeeperResponse createHousekeeper(HousekeeperRequest request) {
        Housekeeper housekeeper = new Housekeeper();
        housekeeper.setName(request.getName());
        housekeeper.setPhone(request.getPhone());
        housekeeper.setActive(request.isActive());
        housekeeper.setStatus(HousekeeperStatus.AVAILABLE);
        return toResponse(housekeeperRepository.save(housekeeper));
    }

    public List<HousekeeperResponse> getAllHousekeepers() {
        return housekeeperRepository.findAll()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public HousekeeperResponse getHousekeeperById(Long id) {
        return toResponse(findById(id));
    }

    @Transactional
    public HousekeeperResponse updateHousekeeper(Long id, HousekeeperRequest request) {
        Housekeeper housekeeper = findById(id);
        housekeeper.setName(request.getName());
        housekeeper.setPhone(request.getPhone());
        housekeeper.setActive(request.isActive());
        return toResponse(housekeeperRepository.save(housekeeper));
    }

    @Transactional
    public void deleteHousekeeper(Long id) {
        Housekeeper housekeeper = findById(id);

        boolean hasActiveTask = cleaningTaskRepository.existsByHousekeeperIdAndStatusIn(
                id, List.of(TaskStatus.ASSIGNED, TaskStatus.IN_PROGRESS));

        if (hasActiveTask) {
            throw new BusinessRuleException(
                    "Cannot delete housekeeper '" + housekeeper.getName() +
                    "' because they currently have an active cleaning task.");
        }

        housekeeperRepository.delete(housekeeper);
    }

    public List<HousekeeperWorkloadResponse> getWorkload() {
        return housekeeperRepository.findAll()
                .stream().map(this::toWorkloadResponse).collect(Collectors.toList());
    }

    public Housekeeper findById(Long id) {
        return housekeeperRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Housekeeper", id));
    }

    private HousekeeperResponse toResponse(Housekeeper housekeeper) {
        HousekeeperResponse response = new HousekeeperResponse();
        response.setId(housekeeper.getId());
        response.setName(housekeeper.getName());
        response.setPhone(housekeeper.getPhone());
        response.setStatus(housekeeper.getStatus());
        response.setActive(housekeeper.isActive());
        return response;
    }

    private HousekeeperWorkloadResponse toWorkloadResponse(Housekeeper housekeeper) {
        HousekeeperWorkloadResponse response = new HousekeeperWorkloadResponse();
        response.setId(housekeeper.getId());
        response.setName(housekeeper.getName());
        response.setStatus(housekeeper.getStatus());
        response.setActive(housekeeper.isActive());
        response.setAssignedTasks(
                cleaningTaskRepository.countByHousekeeperIdAndStatus(housekeeper.getId(), TaskStatus.ASSIGNED));
        response.setInProgressTasks(
                cleaningTaskRepository.countByHousekeeperIdAndStatus(housekeeper.getId(), TaskStatus.IN_PROGRESS));
        response.setCompletedTasks(
                cleaningTaskRepository.countByHousekeeperIdAndStatus(housekeeper.getId(), TaskStatus.COMPLETED));
        return response;
    }
}
