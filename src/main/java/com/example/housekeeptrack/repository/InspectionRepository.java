package com.example.housekeeptrack.repository;

import com.example.housekeeptrack.model.Inspection;
import com.example.housekeeptrack.model.enums.InspectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InspectionRepository extends JpaRepository<Inspection, Long> {

    List<Inspection> findByRoomId(Long roomId);

    List<Inspection> findByRoomIdOrderByInspectedAtDesc(Long roomId);

    boolean existsByRoomIdAndResult(Long roomId, InspectionResult result);
}
