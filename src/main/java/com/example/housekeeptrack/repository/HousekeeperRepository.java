package com.example.housekeeptrack.repository;

import com.example.housekeeptrack.model.Housekeeper;
import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HousekeeperRepository extends JpaRepository<Housekeeper, Long> {

    List<Housekeeper> findByStatus(HousekeeperStatus status);

    Optional<Housekeeper> findFirstByStatusAndActiveTrue(HousekeeperStatus status);

    List<Housekeeper> findByActiveTrue();
}
