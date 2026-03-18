package org.example.Service;

/*import org.example.model.entity.Grade;
import org.example.model.entity.Student;
import org.example.model.entity.Course;
import org.example.model.entity.Enrollment;
import org.example.Repository.GradeRepository;
import org.example.repository.EnrollmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GradeReportService {

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private GradeService gradeService;

    // Generate comprehensive grade report
    public String generateComprehensiveGradeReport() {
        List<Grade> allGrades = gradeService.getAllGrades();

        StringBuilder report = new StringBuilder();
        report.append("=== COMPREHENSIVE GRADE REPORT ===\n\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Grade Records: ").append(allGrades.size()).append("\n\n");

        // Grade Distribution
        report.append("=== GRADE DISTRIBUTION ===\n");
        generateGradeDistribution(report, allGrades);

        // Student Performance
        report.append("\n=== TOP PERFORMING STUDENTS ===\n");
        generateTopStudentsReport(report, allGrades);

        // Course Performance
        report.append("\n=== COURSE PERFORMANCE SUMMARY ===\n");
        generateCoursePerformanceReport(report, allGrades);

        return report.toString();
    }

    // Generate student transcript
    public String generateStudentTranscript(Long studentId) {
        List<Grade> studentGrades = gradeService.getGradesByStudent(studentId);

        if (studentGrades.isEmpty()) {
            return "No grade records found for this student.";
        }

        Student student = studentGrades.get(0).getEnrollment().getStudent();

        StringBuilder transcript = new StringBuilder();
        transcript.append("=== STUDENT TRANSCRIPT ===\n\n");
        transcript.append("Student: ").append(student.getFirstName()).append(" ").append(student.getLastName()).append("\n");
        transcript.append("Registration No: ").append(student.getRegistrationNumber()).append("\n");
        transcript.append("Email: ").append(student.getEmail()).append("\n");
        transcript.append("Generated on: ").append(LocalDateTime.now()).append("\n\n");

        transcript.append("COURSE GRADES:\n");
        transcript.append(String.format("%-12s %-30s %-8s %-15s\n",
                "Course Code", "Course Title", "Grade", "Grade Date"));
        transcript.append("-".repeat(70)).append("\n");

        double totalPoints = 0;
        int count = 0;

        for (Grade grade : studentGrades) {
            Course course = grade.getEnrollment().getCourse();
            double points = convertGradeToPoints(grade.getGradeValue());
            totalPoints += points;
            count++;

            transcript.append(String.format("%-12s %-30s %-8s %-15s\n",
                    course.getCourseCode(),
                    course.getCourseTitle(),
                    grade.getGradeValue(),
                    grade.getGradeDate().toLocalDate().toString()));
        }

        if (count > 0) {
            double gpa = totalPoints / count;
            transcript.append("\nGPA: ").append(String.format("%.2f", gpa)).append("\n");
            transcript.append("Academic Standing: ").append(getAcademicStanding(gpa)).append("\n");
        }

        transcript.append("\n=== END OF TRANSCRIPT ===");

        return transcript.toString();
    }

    // Generate course grade report
    public String generateCourseGradeReport(Long courseId) {
        List<Grade> courseGrades = gradeService.getGradesByCourse(courseId);

        if (courseGrades.isEmpty()) {
            return "No grade records found for this course.";
        }

        Course course = courseGrades.get(0).getEnrollment().getCourse();

        StringBuilder report = new StringBuilder();
        report.append("=== COURSE GRADE REPORT ===\n\n");
        report.append("Course: ").append(course.getCourseCode()).append(" - ").append(course.getCourseTitle()).append("\n");
        report.append("Credits: ").append(course.getCredits()).append("\n");
        report.append("Total Students: ").append(courseGrades.size()).append("\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n\n");

        // Grade distribution
        report.append("GRADE DISTRIBUTION:\n");
        generateCourseGradeDistribution(report, courseGrades);

        // Student list with grades
        report.append("\nSTUDENT GRADES:\n");
        report.append(String.format("%-20s %-15s %-8s %-12s\n",
                "Student Name", "Reg No", "Grade", "Remarks"));
        report.append("-".repeat(60)).append("\n");

        for (Grade grade : courseGrades) {
            Student student = grade.getEnrollment().getStudent();
            String remarks = grade.getRemarks() != null ?
                    (grade.getRemarks().length() > 10 ? grade.getRemarks().substring(0, 10) + "..." : grade.getRemarks())
                    : "";

            report.append(String.format("%-20s %-15s %-8s %-12s\n",
                    student.getFirstName() + " " + student.getLastName(),
                    student.getRegistrationNumber(),
                    grade.getGradeValue(),
                    remarks));
        }

        return report.toString();
    }

    // Generate failing students report
    public String generateFailingStudentsReport() {
        List<Grade> allGrades = gradeService.getAllGrades();
        List<Grade> failingGrades = allGrades.stream()
                .filter(grade -> "F".equals(grade.getGradeValue()))
                .collect(Collectors.toList());

        StringBuilder report = new StringBuilder();
        report.append("=== FAILING STUDENTS REPORT ===\n\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Failing Grades: ").append(failingGrades.size()).append("\n\n");

        if (failingGrades.isEmpty()) {
            report.append("No failing grades found.\n");
        } else {
            report.append("FAILING GRADES:\n");
            report.append(String.format("%-20s %-15s %-12s %-8s\n",
                    "Student Name", "Reg No", "Course", "Grade"));
            report.append("-".repeat(60)).append("\n");

            for (Grade grade : failingGrades) {
                Student student = grade.getEnrollment().getStudent();
                Course course = grade.getEnrollment().getCourse();

                report.append(String.format("%-20s %-15s %-12s %-8s\n",
                        student.getFirstName() + " " + student.getLastName(),
                        student.getRegistrationNumber(),
                        course.getCourseCode(),
                        grade.getGradeValue()));
            }
        }

        report.append("\n=== END OF REPORT ===");
        return report.toString();
    }

    // Generate enrollment summary with grades
    public String generateEnrollmentSummaryReport() {
        List<Enrollment> enrollments = enrollmentRepository.findAll();

        StringBuilder report = new StringBuilder();
        report.append("=== ENROLLMENT SUMMARY WITH GRADES ===\n\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Enrollments: ").append(enrollments.size()).append("\n\n");

        report.append("ENROLLMENT SUMMARY:\n");
        report.append(String.format("%-20s %-15s %-12s %-8s %-12s\n",
                "Student Name", "Reg No", "Course", "Status", "Grade"));
        report.append("-".repeat(75)).append("\n");

        int gradedCount = 0;
        int ungradedCount = 0;

        for (Enrollment enrollment : enrollments) {
            Student student = enrollment.getStudent();
            Course course = enrollment.getCourse();

            // Check if grade exists for this enrollment
            String gradeStatus = "Ungraded";
            String gradeValue = "-";

            List<Grade> enrollmentGrades = gradeService.getGradesByEnrollment(enrollment.getId());
            if (!enrollmentGrades.isEmpty()) {
                gradeStatus = "Graded";
                gradeValue = enrollmentGrades.get(0).getGradeValue();
                gradedCount++;
            } else {
                ungradedCount++;
            }

            report.append(String.format("%-20s %-15s %-12s %-8s %-12s\n",
                    student.getFirstName() + " " + student.getLastName(),
                    student.getRegistrationNumber(),
                    course.getCourseCode(),
                    gradeStatus,
                    gradeValue));
        }

        report.append("\nSUMMARY:\n");
        report.append("Graded Enrollments: ").append(gradedCount).append("\n");
        report.append("Ungraded Enrollments: ").append(ungradedCount).append("\n");
        report.append("Completion Rate: ").append(String.format("%.1f%%",
                (gradedCount * 100.0) / enrollments.size())).append("\n");

        return report.toString();
    }

    // Generate student performance summary
    public String generateStudentPerformanceSummary() {
        List<Grade> allGrades = gradeService.getAllGrades();

        Map<Student, List<Grade>> gradesByStudent = allGrades.stream()
                .filter(grade -> grade.getEnrollment() != null && grade.getEnrollment().getStudent() != null)
                .collect(Collectors.groupingBy(grade -> grade.getEnrollment().getStudent()));

        StringBuilder report = new StringBuilder();
        report.append("=== STUDENT PERFORMANCE SUMMARY ===\n\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n");
        report.append("Total Students with Grades: ").append(gradesByStudent.size()).append("\n\n");

        report.append("STUDENT PERFORMANCE:\n");
        report.append(String.format("%-15s %-25s %-8s %-12s %-15s\n",
                "Reg No", "Student Name", "Courses", "GPA", "Standing"));
        report.append("-".repeat(80)).append("\n");

        for (Map.Entry<Student, List<Grade>> entry : gradesByStudent.entrySet()) {
            Student student = entry.getKey();
            List<Grade> studentGrades = entry.getValue();

            double totalPoints = studentGrades.stream()
                    .mapToDouble(grade -> convertGradeToPoints(grade.getGradeValue()))
                    .sum();
            double gpa = studentGrades.isEmpty() ? 0 : totalPoints / studentGrades.size();

            report.append(String.format("%-15s %-25s %-8d %-12.2f %-15s\n",
                    student.getRegistrationNumber(),
                    student.getFirstName() + " " + student.getLastName(),
                    studentGrades.size(),
                    gpa,
                    getAcademicStanding(gpa)));
        }

        return report.toString();
    }

    private void generateGradeDistribution(StringBuilder report, List<Grade> grades) {
        Map<String, Long> gradeCount = grades.stream()
                .collect(Collectors.groupingBy(Grade::getGradeValue, Collectors.counting()));

        List<String> gradeOrder = List.of("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F");
        int totalGrades = grades.size();

        for (String grade : gradeOrder) {
            long count = gradeCount.getOrDefault(grade, 0L);
            double percentage = totalGrades > 0 ? (count * 100.0) / totalGrades : 0;
            report.append(String.format("%-3s: %2d records (%5.1f%%)\n", grade, count, percentage));
        }
    }

    private void generateTopStudentsReport(StringBuilder report, List<Grade> grades) {
        Map<Student, List<Grade>> gradesByStudent = grades.stream()
                .filter(grade -> grade.getEnrollment() != null && grade.getEnrollment().getStudent() != null)
                .collect(Collectors.groupingBy(grade -> grade.getEnrollment().getStudent()));

        // Get top 5 students by average grade
        List<Map.Entry<Student, Double>> topStudents = gradesByStudent.entrySet().stream()
                .map(entry -> {
                    double avgPoints = entry.getValue().stream()
                            .mapToDouble(grade -> convertGradeToPoints(grade.getGradeValue()))
                            .average()
                            .orElse(0.0);
                    return Map.entry(entry.getKey(), avgPoints);
                })
                .sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
                .limit(5)
                .collect(Collectors.toList());

        if (topStudents.isEmpty()) {
            report.append("No student data available.\n");
            return;
        }

        report.append(String.format("%-20s %-15s %-8s\n", "Student Name", "Reg No", "GPA"));
        report.append("-".repeat(45)).append("\n");

        for (Map.Entry<Student, Double> entry : topStudents) {
            Student student = entry.getKey();
            report.append(String.format("%-20s %-15s %-8.2f\n",
                    student.getFirstName() + " " + student.getLastName(),
                    student.getRegistrationNumber(),
                    entry.getValue()));
        }
    }

    private void generateCoursePerformanceReport(StringBuilder report, List<Grade> grades) {
        Map<Course, List<Grade>> gradesByCourse = grades.stream()
                .filter(grade -> grade.getEnrollment() != null && grade.getEnrollment().getCourse() != null)
                .collect(Collectors.groupingBy(grade -> grade.getEnrollment().getCourse()));

        if (gradesByCourse.isEmpty()) {
            report.append("No course data available.\n");
            return;
        }

        report.append(String.format("%-10s %-25s %-8s %-12s\n",
                "Course", "Title", "Students", "Avg Grade"));
        report.append("-".repeat(60)).append("\n");

        for (Map.Entry<Course, List<Grade>> entry : gradesByCourse.entrySet()) {
            Course course = entry.getKey();
            List<Grade> courseGrades = entry.getValue();

            // Calculate average grade points
            double avgPoints = courseGrades.stream()
                    .mapToDouble(grade -> convertGradeToPoints(grade.getGradeValue()))
                    .average()
                    .orElse(0.0);

            String avgGrade = convertPointsToGrade(avgPoints);

            report.append(String.format("%-10s %-25s %-8d %-12s\n",
                    course.getCourseCode(),
                    course.getCourseTitle().length() > 23 ?
                            course.getCourseTitle().substring(0, 23) + "..." : course.getCourseTitle(),
                    courseGrades.size(),
                    avgGrade));
        }
    }

    private void generateCourseGradeDistribution(StringBuilder report, List<Grade> grades) {
        Map<String, Long> gradeCount = grades.stream()
                .collect(Collectors.groupingBy(Grade::getGradeValue, Collectors.counting()));

        List<String> gradeOrder = List.of("A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F");
        int totalGrades = grades.size();

        for (String grade : gradeOrder) {
            long count = gradeCount.getOrDefault(grade, 0L);
            double percentage = totalGrades > 0 ? (count * 100.0) / totalGrades : 0;
            report.append(String.format("%-3s: %2d students (%5.1f%%)\n", grade, count, percentage));
        }
    }

    private double convertGradeToPoints(String grade) {
        switch (grade) {
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
            case "D-": return 0.7;
            case "F": return 0.0;
            default: return 0.0;
        }
    }

    private String convertPointsToGrade(double points) {
        if (points >= 3.7) return "A";
        else if (points >= 3.3) return "A-";
        else if (points >= 3.0) return "B+";
        else if (points >= 2.7) return "B";
        else if (points >= 2.3) return "B-";
        else if (points >= 2.0) return "C+";
        else if (points >= 1.7) return "C";
        else if (points >= 1.3) return "C-";
        else if (points >= 1.0) return "D+";
        else if (points >= 0.7) return "D";
        else if (points >= 0.0) return "D-";
        else return "F";
    }

    private String getAcademicStanding(double gpa) {
        if (gpa >= 3.7) return "Excellent";
        else if (gpa >= 3.3) return "Very Good";
        else if (gpa >= 3.0) return "Good";
        else if (gpa >= 2.7) return "Satisfactory";
        else if (gpa >= 2.0) return "Needs Improvement";
        else return "Academic Probation";
    }
}*/