package com.mycom.myapp.domain.program.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mycom.myapp.domain.program.entity.ProgramTrainer;
import com.mycom.myapp.domain.program.entity.ProgramTrainer.AssignmentRole;

public interface ProgramTrainerRepository extends JpaRepository<ProgramTrainer, Long> {

    boolean existsByProgramIdAndTrainerId(Long programId, Long trainerId);

    boolean existsByProgramIdAndTrainerIdAndAssignmentRole(
            Long programId, Long trainerId, AssignmentRole assignmentRole);

    Optional<ProgramTrainer> findByProgramIdAndTrainerId(Long programId, Long trainerId);

    List<ProgramTrainer> findByProgramId(Long programId);

    // 대표(MAIN) 트레이너 조회 - 리뷰 대상 트레이너 지정에 사용
    Optional<ProgramTrainer> findByProgramIdAndAssignmentRole(
            Long programId, AssignmentRole assignmentRole);
}
