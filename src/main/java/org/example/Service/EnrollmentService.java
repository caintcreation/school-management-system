package org.example.Service;

import org.example.model.entity.Enrollment;
import org.example.model.entity.Student;
import org.example.model.entity.Course;
import org.example.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private org.example.service.StudentService studentService;

    @Autowired
    private org.example.service.CourseService courseService;

    // Get all enrollments with student and course details
    public List<Enrollment> getAllEnrollments() {
        return enrollmentRepository.findAllWithStudentAndCourse();
    }

    // Get enrollment by ID
    public Optional<Enrollment> getEnrollmentById(Long id) {
        return enrollmentRepository.findById(id);
    }

    // Save new enrollment
    public Enrollment saveEnrollment(Enrollment enrollment) {
        validateEnrollment(enrollment);

        // Check for duplicate enrollment
        if (enrollmentRepository.existsByStudentIdAndCourseId(
                enrollment.getStudent().getId(),
                enrollment.getCourse().getId())) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        // Check if score results in FAIL and prevent enrollment
        if (enrollment.getScore() != null && "FAIL".equals(enrollment.getGrade())) {
            throw new IllegalArgumentException("Student cannot be enrolled with a failing score (" + enrollment.getScore() + "). Minimum required: 60");
        }

        return enrollmentRepository.save(enrollment);
    }

    // Update enrollment
    public Enrollment updateEnrollment(Long id, Enrollment enrollmentDetails) {
        Enrollment enrollment = enrollmentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found with id: " + id));

        validateEnrollment(enrollmentDetails);

        // Check for duplicate enrollment (excluding current enrollment)
        if (enrollmentRepository.existsByStudentIdAndCourseId(
                enrollmentDetails.getStudent().getId(),
                enrollmentDetails.getCourse().getId()) &&
                !enrollment.getId().equals(id)) {
            throw new IllegalArgumentException("Student is already enrolled in this course");
        }

        // Check if score results in FAIL and prevent update
        if (enrollmentDetails.getScore() != null && "FAIL".equals(enrollmentDetails.getGrade())) {
            throw new IllegalArgumentException("Student cannot be enrolled with a failing score (" + enrollmentDetails.getScore() + "). Minimum required: 60");
        }

        enrollment.setStudent(enrollmentDetails.getStudent());
        enrollment.setCourse(enrollmentDetails.getCourse());
        enrollment.setEnrollmentDate(enrollmentDetails.getEnrollmentDate());
        enrollment.setScore(enrollmentDetails.getScore());
        // Grade is automatically calculated when score is set

        return enrollmentRepository.save(enrollment);
    }

    // Update enrollment (overloaded for existing entity)
    public Enrollment updateEnrollment(Enrollment enrollment) {
        validateEnrollment(enrollment);

        // Check if score results in FAIL and prevent update
        if (enrollment.getScore() != null && "FAIL".equals(enrollment.getGrade())) {
            throw new IllegalArgumentException("Student cannot be enrolled with a failing score (" + enrollment.getScore() + "). Minimum required: 60");
        }

        return enrollmentRepository.save(enrollment);
    }

    // Delete enrollment
    public void deleteEnrollment(Long id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new IllegalArgumentException("Enrollment not found with id: " + id);
        }
        enrollmentRepository.deleteById(id);
    }

    // Search enrollments
    public List<Enrollment> searchEnrollments(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllEnrollments();
        }
        return enrollmentRepository.searchEnrollments(searchTerm.trim());
    }

    // Get enrollments by student
    public List<Enrollment> getEnrollmentsByStudent(Long studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    // Get enrollments by course
    public List<Enrollment> getEnrollmentsByCourse(Long courseId) {
        return enrollmentRepository.findByCourseId(courseId);
    }

    // Check if student is enrolled in course
    public boolean isStudentEnrolled(Long studentId, Long courseId) {
        return enrollmentRepository.existsByStudentIdAndCourseId(studentId, courseId);
    }

    // Get enrollment count by course
    public Long getEnrollmentCountByCourse(Long courseId) {
        return enrollmentRepository.countByCourseId(courseId);
    }

    // Get enrollment count by student
    public Long getEnrollmentCountByStudent(Long studentId) {
        return enrollmentRepository.countByStudentId(studentId);
    }

    // Get recent enrollments
    public List<Enrollment> getRecentEnrollments() {
        return enrollmentRepository.findTop10ByOrderByEnrollmentDateDesc();
    }

    // Update score for enrollment (auto-calculates grade)
    public Enrollment updateScore(Long enrollmentId, Double score) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found with id: " + enrollmentId));

        if (score != null && (score < 0 || score > 100)) {
            throw new IllegalArgumentException("Score must be between 0 and 100");
        }

        enrollment.setScore(score);
        return enrollmentRepository.save(enrollment);
    }

    // Validate enrollment data
    private void validateEnrollment(Enrollment enrollment) {
        if (enrollment.getStudent() == null || enrollment.getStudent().getId() == null) {
            throw new IllegalArgumentException("Student is required");
        }

        if (enrollment.getCourse() == null || enrollment.getCourse().getId() == null) {
            throw new IllegalArgumentException("Course is required");
        }

        if (enrollment.getEnrollmentDate() == null) {
            throw new IllegalArgumentException("Enrollment date is required");
        }

        if (enrollment.getEnrollmentDate().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Enrollment date cannot be in the future");
        }

        // Verify student exists
        Student student = studentService.getStudentById(enrollment.getStudent().getId())
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + enrollment.getStudent().getId()));

        // Verify course exists
        Course course = courseService.getCourseById(enrollment.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found with id: " + enrollment.getCourse().getId()));

        enrollment.setStudent(student);
        enrollment.setCourse(course);

        // Validate score if provided
        if (enrollment.getScore() != null) {
            if (enrollment.getScore() < 0 || enrollment.getScore() > 100) {
                throw new IllegalArgumentException("Score must be between 0 and 100");
            }
        }
    }

    // Calculate grade based on score
    public static String calculateGrade(Double score) {
        if (score == null) {
            return null;
        }

        if (score >= 90) return "A";
        else if (score >= 80) return "B";
        else if (score >= 70) return "C";
        else if (score >= 60) return "D";
        else return "FAIL";
    }
}