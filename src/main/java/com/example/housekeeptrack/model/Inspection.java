package com.example.housekeeptrack.model;

import com.example.housekeeptrack.model.enums.InspectionResult;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Entity
@Table(name = "inspection")
public class Inspection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "room_id", nullable = false)
    @JsonIgnoreProperties({"cleaningTasks", "inspections", "hibernateLazyInitializer", "handler"})
    private Room room;

    @Column(nullable = false)
    private String supervisorName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InspectionResult result;

    private String remarks;

    @Column(nullable = false)
    private LocalDateTime inspectedAt;

    @PrePersist
    public void prePersist() {
        if (this.inspectedAt == null) {
            this.inspectedAt = LocalDateTime.now();
        }
    }
}
