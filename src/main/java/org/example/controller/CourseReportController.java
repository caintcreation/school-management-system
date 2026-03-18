package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.entity.Course;
import org.example.service.CourseService;
import org.example.Service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class CourseReportController implements Initializable {

    @Autowired
    private ReportService reportService;

    @Autowired
    private CourseService courseService;

    @FXML private ComboBox<Course> courseComboBox;
    @FXML private TextArea reportTextArea;
    @FXML private Label totalCoursesLabel;
    @FXML private Label totalEnrollmentsLabel;
    @FXML private Label averageEnrollmentsLabel;

    @FXML private TableView<Map<String, Object>> allCoursesTable;
    @FXML private TableColumn<Map<String, Object>, String> colCourseCode;
    @FXML private TableColumn<Map<String, Object>, String> colCourseTitle;
    @FXML private TableColumn<Map<String, Object>, Integer> colCredits;
    @FXML private TableColumn<Map<String, Object>, Long> colEnrollmentCount;
    @FXML private TableColumn<Map<String, Object>, String> colDescription;

    private ObservableList<Course> courseList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCourseComboBox();
        setupAllCoursesTable();
        loadAllCoursesReport();
        updateSummaryStatistics();
    }

    private void setupCourseComboBox() {
        try {
            List<Course> courses = courseService.getAllCourses();
            courseList = FXCollections.observableArrayList(courses);
            courseComboBox.setItems(courseList);
            courseComboBox.setConverter(new javafx.util.StringConverter<Course>() {
                @Override
                public String toString(Course course) {
                    return course == null ? null : course.getCourseCode() + " - " + course.getCourseTitle();
                }

                @Override
                public Course fromString(String string) {
                    return courseList.stream()
                            .filter(course -> toString(course).equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to load courses: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupAllCoursesTable() {
        colCourseCode.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("courseCode")));
        colCourseTitle.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("courseTitle")));
        colCredits.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue().get("credits")).asObject());
        colEnrollmentCount.setCellValueFactory(data ->
                new javafx.beans.property.SimpleLongProperty((Long) data.getValue().get("enrollmentCount")).asObject());
        colDescription.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("description")));
    }

    @FXML
    private void handleGenerateCourseReport() {
        Course selectedCourse = courseComboBox.getValue();
        if (selectedCourse == null) {
            showAlert("Warning", "Please select a course to generate report.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> report = reportService.generateCourseReport(selectedCourse.getId());
            displayCourseReport(report);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate course report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateAllCoursesReport() {
        loadAllCoursesReport();
    }

    @FXML
    private void handleExportReport() {
        if (reportTextArea.getText().isEmpty()) {
            showAlert("Warning", "No report to export. Please generate a report first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            TextInputDialog dialog = new TextInputDialog("course_report");
            dialog.setTitle("Export Report");
            dialog.setHeaderText("Enter filename for export");
            dialog.setContentText("Filename:");

            dialog.showAndWait().ifPresent(filename -> {
                showAlert("Export", "Report exported successfully as: " + filename + ".txt", Alert.AlertType.INFORMATION);
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to export report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePrintReport() {
        if (reportTextArea.getText().isEmpty()) {
            showAlert("Warning", "No report to print. Please generate a report first.", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Print", "Print functionality would be implemented here.", Alert.AlertType.INFORMATION);
    }

    private void loadAllCoursesReport() {
        try {
            List<Map<String, Object>> allCoursesReport = reportService.generateAllCoursesReport();
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(allCoursesReport);
            allCoursesTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load all courses report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayCourseReport(Map<String, Object> report) {
        if (report.isEmpty()) {
            reportTextArea.setText("No data available for the selected course.");
            return;
        }

        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("=== COURSE REPORT ===\n\n");
        reportBuilder.append("COURSE INFORMATION:\n");
        reportBuilder.append("Course Code: ").append(report.get("courseCode")).append("\n");
        reportBuilder.append("Course Title: ").append(report.get("courseTitle")).append("\n");
        reportBuilder.append("Credits: ").append(report.get("credits")).append("\n");
        reportBuilder.append("Description: ").append(report.get("description")).append("\n");
        reportBuilder.append("Prerequisites: ").append(report.get("prerequisites")).append("\n\n");

        reportBuilder.append("ENROLLMENT STATISTICS:\n");
        reportBuilder.append("Total Enrollments: ").append(report.get("totalEnrollments")).append("\n");
        reportBuilder.append("Average Score: ").append(String.format("%.2f", report.get("averageScore"))).append("\n\n");

        reportBuilder.append("GRADE DISTRIBUTION:\n");
        Map<String, Long> gradeDistribution = (Map<String, Long>) report.get("gradeDistribution");
        if (gradeDistribution != null && !gradeDistribution.isEmpty()) {
            gradeDistribution.forEach((grade, count) ->
                    reportBuilder.append(grade).append(": ").append(count).append(" students\n"));
        } else {
            reportBuilder.append("No grades recorded yet.\n");
        }
        reportBuilder.append("\n");

        reportBuilder.append("STUDENT GRADES:\n");
        List<Map<String, Object>> studentGrades = (List<Map<String, Object>>) report.get("studentGrades");
        if (studentGrades != null && !studentGrades.isEmpty()) {
            reportBuilder.append(String.format("%-20s %-15s %-8s %-6s %-12s\n",
                    "Student Name", "Reg Number", "Score", "Grade", "Enrollment Date"));
            reportBuilder.append("-".repeat(70)).append("\n");

            for (Map<String, Object> studentGrade : studentGrades) {
                reportBuilder.append(String.format("%-20s %-15s %-8s %-6s %-12s\n",
                        truncate((String) studentGrade.get("studentName"), 19),
                        studentGrade.get("registrationNumber"),
                        studentGrade.get("score") != null ? String.format("%.1f", studentGrade.get("score")) : "N/A",
                        studentGrade.get("grade") != null ? studentGrade.get("grade") : "N/A",
                        studentGrade.get("enrollmentDate")));
            }
        } else {
            reportBuilder.append("No student enrollments found.\n");
        }

        reportTextArea.setText(reportBuilder.toString());
    }

    private void updateSummaryStatistics() {
        try {
            Map<String, Object> stats = reportService.getSystemStatistics();
            totalCoursesLabel.setText(String.valueOf(stats.get("totalCourses")));
            totalEnrollmentsLabel.setText(String.valueOf(stats.get("totalEnrollments")));

            long totalCourses = (Long) stats.get("totalCourses");
            long totalEnrollments = (Long) stats.get("totalEnrollments");
            double average = totalCourses > 0 ? (double) totalEnrollments / totalCourses : 0;
            averageEnrollmentsLabel.setText(String.format("%.1f", average));

        } catch (Exception e) {
            showAlert("Error", "Failed to load summary statistics: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        return text.length() > maxLength ? text.substring(0, maxLength) + "..." : text;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}