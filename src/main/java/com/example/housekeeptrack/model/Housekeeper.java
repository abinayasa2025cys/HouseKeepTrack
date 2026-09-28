package com.example.housekeeptrack.model;

import com.example.housekeeptrack.model.enums.HousekeeperStatus;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@Entity
@Table(name = "housekeeper")
public class Housekeeper {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HousekeeperStatus status = HousekeeperStatus.AVAILABLE;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "housekeeper", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CleaningTask> cleaningTasks = new ArrayList<>();
}
