package com.mycom.myapp.domain.program.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import com.mycom.myapp.domain.user.entity.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "program_trainer", uniqueConstraints =
        @UniqueConstraint(name = "uk_program_trainer", columnNames = {"program_id", "trainer_id"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProgramTrainer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AssignmentRole assignmentRole;

    @CreationTimestamp
    private LocalDateTime assignedAt;

    public enum AssignmentRole {
        MAIN, ASSISTANT
    }
}
