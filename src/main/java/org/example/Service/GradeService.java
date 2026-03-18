package org.example.Service;

import org.example.model.entity.Grade;
import org.example.model.entity.Enrollment;
import org.example.repository.GradeRepository;
import org.example.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class GradeService {

    private final GradeRepository gradeRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Autowired
    public GradeService(GradeRepository gradeRepository, EnrollmentRepository enrollmentRepository) {
        this.gradeRepository = gradeRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    @Transactional(readOnly = true)
    public List<Grade> findAllGrades() {
        return gradeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Grade> findAllGradesWithEnrollment() {
        List<Grade> grades = gradeRepository.findAll();
        // Force initialization of lazy relationships
        grades.forEach(grade -> {
            if (grade.getEnrollment() != null) {
                grade.getEnrollment().getStudent().getId(); // Force student load
                grade.getEnrollment().getCourse().getId(); // Force course load
            }
        });
        return grades;
    }

    @Transactional(readOnly = true)
    public Optional<Grade> findGradeById(Long id) {
        return gradeRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByEnrollmentId(Long enrollmentId) {
        return gradeRepository.findByEnrollmentId(enrollmentId);
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByStudentId(Long studentId) {
        List<Grade> grades = gradeRepository.findByEnrollmentStudentId(studentId);
        // Force initialization
        grades.forEach(grade -> {
            grade.getEnrollment().getStudent().getId();
            grade.getEnrollment().getCourse().getId();
        });
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByCourseId(Long courseId) {
        List<Grade> grades = gradeRepository.findByEnrollmentCourseId(courseId);
        // Force initialization
        grades.forEach(grade -> {
            grade.getEnrollment().getStudent().getId();
            grade.getEnrollment().getCourse().getId();
        });
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByStudentRegistrationNumber(String registrationNumber) {
        List<Grade> grades = gradeRepository.findByStudentRegistrationNumber(registrationNumber);
        // Force initialization
        grades.forEach(grade -> {
            grade.getEnrollment().getStudent().getId();
            grade.getEnrollment().getCourse().getId();
        });
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByCourseCode(String courseCode) {
        List<Grade> grades = gradeRepository.findByCourseCode(courseCode);
        // Force initialization
        grades.forEach(grade -> {
            grade.getEnrollment().getStudent().getId();
            grade.getEnrollment().getCourse().getId();
        });
        return grades;
    }

    @Transactional(readOnly = true)
    public List<Grade> findGradesByGradeValue(String gradeValue) {
        List<Grade> grades = gradeRepository.findByGradeValue(gradeValue);
        // Force initialization
        grades.forEach(grade -> {
            grade.getEnrollment().getStudent().getId();
            grade.getEnrollment().getCourse().getId();
        });
        return grades;
    }

    public Grade saveGrade(Grade grade) {
        validateGrade(grade);
        return gradeRepository.save(grade);
    }

    public Grade updateGrade(Long id, Grade gradeDetails) {
        Grade existingGrade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Grade not found with id: " + id));

        validateGrade(gradeDetails);

        existingGrade.setGradeValue(gradeDetails.getGradeValue());
        existingGrade.setRemarks(gradeDetails.getRemarks());
        existingGrade.setGradeDate(gradeDetails.getGradeDate());

        return gradeRepository.save(existingGrade);
    }

    public void deleteGrade(Long id) {
        if (!gradeRepository.existsById(id)) {
            throw new IllegalArgumentException("Grade not found with id: " + id);
        }
        gradeRepository.deleteById(id);
    }

    public void deleteGradesByEnrollmentId(Long enrollmentId) {
        gradeRepository.deleteByEnrollmentId(enrollmentId);
    }

    public Grade assignGradeToEnrollment(Long enrollmentId, String gradeValue, String remarks) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found with id: " + enrollmentId));

        Grade grade = new Grade(enrollment, gradeValue);
        grade.setRemarks(remarks);

        return saveGrade(grade);
    }

    private void validateGrade(Grade grade) {
        if (grade.getEnrollment() == null) {
            throw new IllegalArgumentException("Enrollment is required for grade");
        }
        if (grade.getGradeValue() == null || grade.getGradeValue().trim().isEmpty()) {
            throw new IllegalArgumentException("Grade value is required");
        }
        if (grade.getGradeDate() == null) {
            throw new IllegalArgumentException("Grade date is required");
        }
    }

    public Double calculateStudentGPA(Long studentId) {
        List<Grade> studentGrades = findGradesByStudentId(studentId);
        if (studentGrades.isEmpty()) {
            return 0.0;
        }

        double totalPoints = 0.0;
        int totalCredits = 0;

        for (Grade grade : studentGrades) {
            Double gradePoint = convertGradeToPoint(grade.getGradeValue());
            Integer credits = grade.getEnrollment().getCourse().getCredits();

            if (gradePoint != null && credits != null) {
                totalPoints += gradePoint * credits;
                totalCredits += credits;
            }
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }

    private Double convertGradeToPoint(String gradeValue) {
        return switch (gradeValue.toUpperCase()) {
            case "A", "A+" -> 4.0;
            case "A-" -> 3.7;
            case "B+" -> 3.3;
            case "B" -> 3.0;
            case "B-" -> 2.7;
            case "C+" -> 2.3;
            case "C" -> 2.0;
            case "C-" -> 1.7;
            case "D+" -> 1.3;
            case "D" -> 1.0;
            case "F" -> 0.0;
            default -> null;
        };
    }
}