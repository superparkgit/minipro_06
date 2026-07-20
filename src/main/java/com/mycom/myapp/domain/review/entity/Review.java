package com.mycom.myapp.domain.review.entity;

import com.mycom.myapp.domain.reservation.entity.Reservation;
import com.mycom.myapp.domain.user.entity.User;
import com.mycom.myapp.domain.program.entity.Program;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Getter
@NoArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false, unique = true)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_id", nullable = false)
    private User trainer;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @Builder
    private Review(Reservation reservation, User user, Program program, User trainer,
                   int rating, String content) {
        this.reservation = reservation;
        this.user = user;
        this.program = program;
        this.trainer = trainer;
        this.rating = rating;
        this.content = content;
        this.status = ReviewStatus.VISIBLE;
    }

    public void update(int rating, String content) {
        this.rating = rating;
        this.content = content;
    }

    public void hide() {
        this.status = ReviewStatus.HIDDEN;
    }

    public void restore() {
        this.status = ReviewStatus.VISIBLE;
    }

    public void markDeleted() {
        this.status = ReviewStatus.DELETED;
    }
}
