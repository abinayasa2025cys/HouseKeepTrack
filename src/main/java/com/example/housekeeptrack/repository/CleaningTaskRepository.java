package com.example.housekeeptrack.repository;

import com.example.housekeeptrack.model.CleaningTask;
import com.example.housekeeptrack.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CleaningTaskRepository extends JpaRepository<CleaningTask, Long> {

    List<CleaningTask> findByRoomId(Long roomId);

    List<CleaningTask> findByHousekeeperId(Long housekeeperId);

    List<CleaningTask> findByStatus(TaskStatus status);

    long countByHousekeeperIdAndStatus(Long housekeeperId, TaskStatus status);

    Optional<CleaningTask> findFirstByRoomIdAndStatusIn(Long roomId, List<TaskStatus> statuses);

    boolean existsByRoomIdAndStatusIn(Long roomId, List<TaskStatus> statuses);

    boolean existsByHousekeeperIdAndStatusIn(Long housekeeperId, List<TaskStatus> statuses);
}
