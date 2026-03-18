package org.example.service;

import org.example.model.entity.Course;
import org.example.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class CourseService {

    private final CourseRepository courseRepository;

    @Autowired
    public CourseService(CourseRepository courseRepository) {
        this.courseRepository = courseRepository;
    }

    /**
     * Save a course with validation
     */
    public Course saveCourse(Course course) {
        // Validation logic
        if (course.getCourseCode() == null || course.getCourseCode().trim().isEmpty()) {
            throw new IllegalArgumentException("Course code is required");
        }
        if (course.getCourseTitle() == null || course.getCourseTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Course title is required");
        }
        if (course.getCredits() <= 0) {
            throw new IllegalArgumentException("Credits must be greater than 0");
        }

        // Check for duplicate course code
        Course existingCourse = courseRepository.findByCourseCode(course.getCourseCode());
        if (existingCourse != null && !existingCourse.getId().equals(course.getId())) {
            throw new IllegalArgumentException("Course with code " + course.getCourseCode() + " already exists");
        }

        return courseRepository.save(course);
    }

    /**
     * Get all courses
     */
    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    /**
     * Get course by ID
     */
    @Transactional(readOnly = true)
    public Optional<Course> getCourseById(Long id) {
        return courseRepository.findById(id);
    }

    /**
     * Get course by course code
     */
    @Transactional(readOnly = true)
    public Course getCourseByCode(String courseCode) {
        return courseRepository.findByCourseCode(courseCode);
    }

    /**
     * Update course information
     */
    public Course updateCourse(Long id, Course courseDetails) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + id));

        // Update fields
        course.setCourseTitle(courseDetails.getCourseTitle());
        course.setCredits(courseDetails.getCredits());
        course.setCourseDescription(courseDetails.getCourseDescription());
        course.setPrerequisites(courseDetails.getPrerequisites());

        // Only update course code if it's different
        if (!course.getCourseCode().equals(courseDetails.getCourseCode())) {
            Course existing = courseRepository.findByCourseCode(courseDetails.getCourseCode());
            if (existing != null) {
                throw new IllegalArgumentException("Course code already exists: " + courseDetails.getCourseCode());
            }
            course.setCourseCode(courseDetails.getCourseCode());
        }

        return courseRepository.save(course);
    }

    /**
     * Delete course by ID
     */
    public void deleteCourse(Long id) {
        if (!courseRepository.existsById(id)) {
            throw new IllegalArgumentException("Course not found with id: " + id);
        }
        courseRepository.deleteById(id);
    }

    /**
     * Search courses by title or code
     */
    @Transactional(readOnly = true)
    public List<Course> searchCourses(String searchTerm) {
        return courseRepository.findByCourseTitleContainingIgnoreCaseOrCourseCodeContainingIgnoreCase(
                searchTerm, searchTerm);
    }

    /**
     * Check if course code exists
     */
    @Transactional(readOnly = true)
    public boolean courseCodeExists(String courseCode) {
        return courseRepository.findByCourseCode(courseCode) != null;
    }
}