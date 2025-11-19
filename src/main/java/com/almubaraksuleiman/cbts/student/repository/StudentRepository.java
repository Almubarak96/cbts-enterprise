package com.almubaraksuleiman.cbts.student.repository;

import com.almubaraksuleiman.cbts.student.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @author Almubarak Suleiman
 * @version 1.0
 * @since 2025
 **/

@Repository
public interface StudentRepository  extends JpaRepository<Student, Long> {

    Optional<Student> findByUsername(String username);

    boolean existsByUsername(String username);




    List<Student> findByIdIn(List<Long> studentIds);

    @Query("SELECT DISTINCT s.department FROM Student s WHERE s.department IS NOT NULL")
    List<String> findDistinctDepartments();

    @Query("SELECT s FROM Student s WHERE s.id NOT IN " +
            "(SELECT e.student.id FROM Enrollment e WHERE e.test.id = :testId)")
    List<Student> findAvailableStudentsForTest(@Param("testId") Long testId);

    List<Student> findByDepartment(String department);

    Optional<Student> findByEmail(String email);

}
