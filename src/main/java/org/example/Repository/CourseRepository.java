package org.example.repository;

import org.example.model.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseCode(String courseCode);
    List<Course> findByCourseTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(String title, String code);
}