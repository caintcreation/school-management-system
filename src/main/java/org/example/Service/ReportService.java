package org.example.Service;

import org.example.model.entity.*;
import org.example.Repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportService {

    @Autowired
    private org.example.repository.StudentRepository studentRepository;

    @Autowired
    private org.example.repository.CourseRepository courseRepository;

    @Autowired
    private org.example.repository.EnrollmentRepository enrollmentRepository;

    @Autowired
    private org.example.repository.GradeRepository gradeRepository;

    // Course Reports
    public Map<String, Object> generateCourseReport(Long courseId) {
        Map<String, Object> report = new HashMap<>();

        Course course = courseRepository.findById(courseId).orElse(null);
        if (course == null) {
            return report;
        }

        List<Enrollment> enrollments = enrollmentRepository.findByCourseId(courseId);
        List<Grade> grades = gradeRepository.findByEnrollmentCourseId(courseId);

        // Basic course info
        report.put("courseCode", course.getCourseCode());
        report.put("courseTitle", course.getCourseTitle());
        report.put("credits", course.getCredits());
        report.put("description", course.getCourseDescription());
        report.put("prerequisites", course.getPrerequisites());

        // Enrollment statistics
        report.put("totalEnrollments", enrollments.size());
        report.put("enrolledStudents", enrollments.stream()
                .map(Enrollment::getStudent)
                .collect(Collectors.toList()));

        // Grade distribution
        Map<String, Long> gradeDistribution = grades.stream()
                .filter(grade -> grade.getGradeValue() != null)
                .collect(Collectors.groupingBy(
                        Grade::getGradeValue,
                        Collectors.counting()
                ));
        report.put("gradeDistribution", gradeDistribution);

        // Average score
        OptionalDouble avgScore = enrollments.stream()
                .filter(e -> e.getScore() != null)
                .mapToDouble(Enrollment::getScore)
                .average();
        report.put("averageScore", avgScore.orElse(0.0));

        // Student list with grades
        List<Map<String, Object>> studentGrades = enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> studentGrade = new HashMap<>();
                    studentGrade.put("studentName", enrollment.getStudent().getFirstName() + " " + enrollment.getStudent().getLastName());
                    studentGrade.put("registrationNumber", enrollment.getStudent().getRegistrationNumber());
                    studentGrade.put("score", enrollment.getScore());
                    studentGrade.put("grade", enrollment.getGrade());
                    studentGrade.put("enrollmentDate", enrollment.getEnrollmentDate());
                    return studentGrade;
                })
                .collect(Collectors.toList());
        report.put("studentGrades", studentGrades);

        return report;
    }

    public List<Map<String, Object>> generateAllCoursesReport() {
        List<Course> courses = courseRepository.findAll();
        return courses.stream()
                .map(course -> {
                    Map<String, Object> courseReport = new HashMap<>();
                    Long enrollmentCount = enrollmentRepository.countByCourseId(course.getId());

                    courseReport.put("courseCode", course.getCourseCode());
                    courseReport.put("courseTitle", course.getCourseTitle());
                    courseReport.put("credits", course.getCredits());
                    courseReport.put("enrollmentCount", enrollmentCount);
                    courseReport.put("description", course.getCourseDescription());

                    return courseReport;
                })
                .collect(Collectors.toList());
    }

    // Student Reports
    public Map<String, Object> generateStudentReport(Long studentId) {
        Map<String, Object> report = new HashMap<>();

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return report;
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);
        List<Grade> grades = gradeRepository.findByEnrollmentStudentId(studentId);

        // Student info
        report.put("studentId", student.getId());
        report.put("registrationNumber", student.getRegistrationNumber());
        report.put("fullName", student.getFirstName() + " " + student.getLastName());
        report.put("email", student.getEmail());
        report.put("department", student.getDepartment());
        report.put("enrollmentDate", student.getEnrollmentDate());
        report.put("dateOfBirth", student.getDateOfBirth());

        // Enrollment summary
        report.put("totalCourses", enrollments.size());
        report.put("completedCourses", enrollments.stream()
                .filter(e -> e.getGrade() != null && !e.getGrade().equals("F"))
                .count());
        report.put("currentEnrollments", enrollments.stream()
                .filter(e -> e.getGrade() == null)
                .count());

        // GPA calculation
        double gpa = calculateStudentGPA(studentId);
        report.put("gpa", gpa);
        report.put("academicStanding", getAcademicStanding(gpa));

        // Course details
        List<Map<String, Object>> courseDetails = enrollments.stream()
                .map(enrollment -> {
                    Map<String, Object> courseInfo = new HashMap<>();
                    courseInfo.put("courseCode", enrollment.getCourse().getCourseCode());
                    courseInfo.put("courseTitle", enrollment.getCourse().getCourseTitle());
                    courseInfo.put("credits", enrollment.getCourse().getCredits());
                    courseInfo.put("enrollmentDate", enrollment.getEnrollmentDate());
                    courseInfo.put("score", enrollment.getScore());
                    courseInfo.put("grade", enrollment.getGrade());
                    courseInfo.put("status", enrollment.getGrade() == null ? "In Progress" : "Completed");
                    return courseInfo;
                })
                .collect(Collectors.toList());
        report.put("courseDetails", courseDetails);

        return report;
    }

    public List<Map<String, Object>> generateAllStudentsReport() {
        List<Student> students = studentRepository.findAll();
        return students.stream()
                .map(student -> {
                    Map<String, Object> studentReport = new HashMap<>();
                    Long enrollmentCount = enrollmentRepository.countByStudentId(student.getId());
                    double gpa = calculateStudentGPA(student.getId());

                    studentReport.put("registrationNumber", student.getRegistrationNumber());
                    studentReport.put("fullName", student.getFirstName() + " " + student.getLastName());
                    studentReport.put("department", student.getDepartment());
                    studentReport.put("email", student.getEmail());
                    studentReport.put("enrollmentCount", enrollmentCount);
                    studentReport.put("gpa", gpa);
                    studentReport.put("academicStanding", getAcademicStanding(gpa));
                    studentReport.put("enrollmentDate", student.getEnrollmentDate());

                    return studentReport;
                })
                .collect(Collectors.toList());
    }

    // Transcript Report
    public Map<String, Object> generateTranscriptReport(Long studentId) {
        Map<String, Object> transcript = new HashMap<>();

        Student student = studentRepository.findById(studentId).orElse(null);
        if (student == null) {
            return transcript;
        }

        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        // Student and institutional info
        transcript.put("studentName", student.getFirstName() + " " + student.getLastName());
        transcript.put("registrationNumber", student.getRegistrationNumber());
        transcript.put("department", student.getDepartment());
        transcript.put("generationDate", LocalDate.now());
        transcript.put("institution", "Chuka University");
        transcript.put("faculty", "Department of Computer Science");

        // Academic summary
        double gpa = calculateStudentGPA(studentId);
        transcript.put("cumulativeGPA", gpa);
        transcript.put("academicStanding", getAcademicStanding(gpa));

        int totalCredits = enrollments.stream()
                .filter(e -> e.getGrade() != null && !e.getGrade().equals("F"))
                .mapToInt(e -> e.getCourse().getCredits())
                .sum();
        transcript.put("totalCreditsEarned", totalCredits);

        // Course grades
        List<Map<String, Object>> courseGrades = enrollments.stream()
                .filter(e -> e.getGrade() != null)
                .map(enrollment -> {
                    Map<String, Object> gradeInfo = new HashMap<>();
                    gradeInfo.put("courseCode", enrollment.getCourse().getCourseCode());
                    gradeInfo.put("courseTitle", enrollment.getCourse().getCourseTitle());
                    gradeInfo.put("credits", enrollment.getCourse().getCredits());
                    gradeInfo.put("grade", enrollment.getGrade());
                    gradeInfo.put("score", enrollment.getScore());
                    gradeInfo.put("semester", getSemesterFromDate(enrollment.getEnrollmentDate()));
                    gradeInfo.put("gradePoints", convertGradeToPoints(enrollment.getGrade()));
                    return gradeInfo;
                })
                .collect(Collectors.toList());
        transcript.put("courseGrades", courseGrades);

        // Semester-wise breakdown
        Map<String, List<Map<String, Object>>> semesterBreakdown = courseGrades.stream()
                .collect(Collectors.groupingBy(
                        grade -> (String) grade.get("semester")
                ));
        transcript.put("semesterBreakdown", semesterBreakdown);

        return transcript;
    }

    // Utility methods
    private double calculateStudentGPA(Long studentId) {
        List<Enrollment> enrollments = enrollmentRepository.findByStudentId(studentId);

        double totalPoints = 0;
        int totalCredits = 0;

        for (Enrollment enrollment : enrollments) {
            if (enrollment.getGrade() != null && !enrollment.getGrade().isEmpty()) {
                double gradePoints = convertGradeToPoints(enrollment.getGrade());
                int credits = enrollment.getCourse().getCredits();
                totalPoints += gradePoints * credits;
                totalCredits += credits;
            }
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }

    private double convertGradeToPoints(String grade) {
        if (grade == null) return 0.0;

        switch (grade.toUpperCase()) {
            case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.3;
            case "B": return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.3;
            case "C": return 2.0;
            case "C-": return 1.7;
            case "D+": return 1.3;
            case "D": return 1.0;
            case "F": return 0.0;
            default: return 0.0;
        }
    }

    private String getAcademicStanding(double gpa) {
        if (gpa >= 3.7) return "First Class Honors";
        else if (gpa >= 3.3) return "Second Class Upper";
        else if (gpa >= 3.0) return "Second Class Lower";
        else if (gpa >= 2.7) return "Third Class";
        else if (gpa >= 2.0) return "Pass";
        else return "Probation";
    }

    private String getSemesterFromDate(LocalDate date) {
        int month = date.getMonthValue();
        int year = date.getYear();

        if (month >= 1 && month <= 5) {
            return "Spring " + year;
        } else if (month >= 6 && month <= 8) {
            return "Summer " + year;
        } else {
            return "Fall " + year;
        }
    }

    // Statistics for dashboard
    public Map<String, Object> getSystemStatistics() {
        Map<String, Object> stats = new HashMap<>();

        stats.put("totalStudents", studentRepository.count());
        stats.put("totalCourses", courseRepository.count());
        stats.put("totalEnrollments", enrollmentRepository.count());
        stats.put("gradedEnrollments", enrollmentRepository.findByGradeIsNotNull().size());

        return stats;
    }
}
