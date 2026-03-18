package org.example.repository;

import org.example.model.entity.Enrollment;
import org.example.model.entity.Student;
import org.example.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    // Find enrollments by student
    List<Enrollment> findByStudentId(Long studentId);

    // Find enrollments by course
    List<Enrollment> findByCourseId(Long courseId);

    // Find specific enrollment by student and course
    Optional<Enrollment> findByStudentIdAndCourseId(Long studentId, Long courseId);

    // Check if enrollment exists for student and course
    boolean existsByStudentIdAndCourseId(Long studentId, Long courseId);

    // Find enrollments by date range
    List<Enrollment> findByEnrollmentDateBetween(LocalDate startDate, LocalDate endDate);

    // Find enrollments by grade
    List<Enrollment> findByGrade(String grade);

    // Find enrollments with grades (not null)
    List<Enrollment> findByGradeIsNotNull();

    // Find enrollments without grades (null)
    List<Enrollment> findByGradeIsNull();

    // Custom search query
    @Query("SELECT e FROM Enrollment e WHERE " +
            "LOWER(e.student.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.student.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.student.registrationNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.course.courseCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.course.courseTitle) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
            "LOWER(e.grade) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    List<Enrollment> searchEnrollments(@Param("searchTerm") String searchTerm);

    // Count enrollments by course
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.course.id = :courseId")
    Long countByCourseId(@Param("courseId") Long courseId);

    // Count enrollments by student
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.student.id = :studentId")
    Long countByStudentId(@Param("studentId") Long studentId);

    // Find recent enrollments
    List<Enrollment> findTop10ByOrderByEnrollmentDateDesc();

    // Find enrollments with student and course details (eager loading)
    @Query("SELECT e FROM Enrollment e JOIN FETCH e.student JOIN FETCH e.course ORDER BY e.enrollmentDate DESC")
    List<Enrollment> findAllWithStudentAndCourse();
}