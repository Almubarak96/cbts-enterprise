package com.almubaraksuleiman.cbts.examiner.repository;


import com.almubaraksuleiman.cbts.examiner.model.Enrollment;
import com.almubaraksuleiman.cbts.examiner.model.Examiner;
import com.almubaraksuleiman.cbts.examiner.model.Test;
import com.almubaraksuleiman.cbts.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByTestId(Long testId);
    
    Optional<Enrollment> findByTestIdAndStudentId(Long testId, Long studentId);
    
    boolean existsByTestIdAndStudentId(Long testId, Long studentId);
    
    void deleteByTestIdAndStudentId(Long testId, Long studentId);
    
    void deleteByTestId(Long testId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.test.id = :testId")
    Long countByTestId(@Param("testId") Long testId);
    
    @Query("SELECT e.student FROM Enrollment e WHERE e.test.id = :testId")
    List<Student> findEnrolledStudentsByTestId(@Param("testId") Long testId);


    //List<Enrollment> findByStudentId(Long studentId);

    List<Enrollment> findByStudentIdAndStatus(Long studentId, Enrollment.EnrollmentStatus status);

    boolean existsByTestIdAndStudentIdAndStatus(Long testId, Long studentId, Enrollment.EnrollmentStatus status);

    @Query("SELECT e FROM Enrollment e WHERE e.student.id = :studentId")
    List<Enrollment> findByStudentId(@Param("studentId") Long studentId);





    // NEW: Find enrollments by examiner (tests created by specific examiner)
    @Query("SELECT e FROM Enrollment e WHERE e.test.createdBy = :examiner")
    List<Enrollment> findByTestCreatedBy(@Param("examiner") Examiner examiner);

    // NEW: Count distinct students enrolled in examiner's tests
    @Query("SELECT COUNT(DISTINCT e.student.id) FROM Enrollment e WHERE e.test.createdBy = :examiner")
    Long countDistinctStudentsByExaminer(@Param("examiner") Examiner examiner);

    // NEW: Find enrollments by examiner and status
    @Query("SELECT e FROM Enrollment e WHERE e.test.createdBy = :examiner AND e.status = :status")
    List<Enrollment> findByTestCreatedByAndStatus(@Param("examiner") Examiner examiner,
                                                  @Param("status") Enrollment.EnrollmentStatus status);

    // NEW: Count enrollments by examiner
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.test.createdBy = :examiner")
    Long countByTestCreatedBy(@Param("examiner") Examiner examiner);
}