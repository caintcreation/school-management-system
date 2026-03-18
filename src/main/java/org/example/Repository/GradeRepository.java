package org.example.repository;

import org.example.model.entity.Grade;
import org.example.model.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GradeRepository extends JpaRepository<Grade, Long> {

    List<Grade> findByEnrollmentId(Long enrollmentId);

    List<Grade> findByEnrollmentStudentId(Long studentId);

    List<Grade> findByEnrollmentCourseId(Long courseId);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.id = :studentId AND g.enrollment.course.id = :courseId")
    List<Grade> findByStudentIdAndCourseId(@Param("studentId") Long studentId, @Param("courseId") Long courseId);

    @Query("SELECT g FROM Grade g WHERE g.gradeValue = :gradeValue")
    List<Grade> findByGradeValue(@Param("gradeValue") String gradeValue);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.student.registrationNumber = :registrationNumber")
    List<Grade> findByStudentRegistrationNumber(@Param("registrationNumber") String registrationNumber);

    @Query("SELECT g FROM Grade g WHERE g.enrollment.course.courseCode = :courseCode")
    List<Grade> findByCourseCode(@Param("courseCode") String courseCode);

    void deleteByEnrollmentId(Long enrollmentId);
}