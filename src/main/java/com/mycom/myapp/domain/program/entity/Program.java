package com.mycom.myapp.domain.program.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Program {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProgramType type;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ProgramStatus status = ProgramStatus.OPEN;

    @Builder.Default
    @OneToMany(mappedBy = "program")
    private List<ProgramTrainer> trainerAssignments = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public void update(String title, ProgramType type, int capacity,
                       LocalDateTime startAt, LocalDateTime endAt, String description) {
        this.title = title;
        this.type = type;
        this.capacity = capacity;
        this.startAt = startAt;
        this.endAt = endAt;
        this.description = description;
    }

    public void cancel() {
        this.status = ProgramStatus.CANCELED;
    }

    public void complete() {
        this.status = ProgramStatus.COMPLETED;
    }

    public void addTrainerAssignment(ProgramTrainer assignment) {
        this.trainerAssignments.add(assignment);
    }

    public enum ProgramType {
        PT, GROUP
    }

    public enum ProgramStatus {
        OPEN, CLOSED, CANCELED, COMPLETED
    }
}
