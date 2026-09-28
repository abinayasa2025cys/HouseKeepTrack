package com.example.housekeeptrack.model;

import com.example.housekeeptrack.model.enums.TaskStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "cleaning_task")
public class CleaningTask {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"cleaningTasks", "inspections", "hibernateLazyInitializer", "handler"})
    private Room room;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "housekeeper_id", nullable = true)
    @JsonIgnoreProperties({"cleaningTasks", "hibernateLazyInitializer", "handler"})
    private Housekeeper housekeeper;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status = TaskStatus.ASSIGNED;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime startedAt;

    private LocalDateTime completedAt;

    @PrePersist
    public void prePersist() {
        if (this.assignedAt == null) {
            this.assignedAt = LocalDateTime.now();
        }
    }
}
