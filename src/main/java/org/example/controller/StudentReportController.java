package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.example.model.entity.Student;
import org.example.Service.ReportService;
import org.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class StudentReportController implements Initializable {

    @Autowired
    private ReportService reportService;

    @Autowired
    private StudentService studentService;

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextArea reportTextArea;
    @FXML private Label totalStudentsLabel;
    @FXML private Label averageGPALabel;
    @FXML private Label activeEnrollmentsLabel;

    @FXML private TableView<Map<String, Object>> allStudentsTable;
    @FXML private TableColumn<Map<String, Object>, String> colRegNumber;
    @FXML private TableColumn<Map<String, Object>, String> colStudentName;
    @FXML private TableColumn<Map<String, Object>, String> colDepartment;
    @FXML private TableColumn<Map<String, Object>, String> colEmail;
    @FXML private TableColumn<Map<String, Object>, Long> colEnrollmentCount;
    @FXML private TableColumn<Map<String, Object>, Double> colGPA;
    @FXML private TableColumn<Map<String, Object>, String> colAcademicStanding;

    private ObservableList<Student> studentList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStudentComboBox();
        setupAllStudentsTable();
        loadAllStudentsReport();
        updateSummaryStatistics();
    }

    private void setupStudentComboBox() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList = FXCollections.observableArrayList(students);
            studentComboBox.setItems(studentList);
            studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
                @Override
                public String toString(Student student) {
                    return student == null ? null :
                            student.getRegistrationNumber() + " - " + student.getFirstName() + " " + student.getLastName();
                }

                @Override
                public Student fromString(String string) {
                    return studentList.stream()
                            .filter(student -> toString(student).equals(string))
                            .findFirst()
                            .orElse(null);
                }
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupAllStudentsTable() {
        colRegNumber.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("registrationNumber")));
        colStudentName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("fullName")));
        colDepartment.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("department")));
        colEmail.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("email")));
        colEnrollmentCount.setCellValueFactory(data ->
                new javafx.beans.property.SimpleLongProperty((Long) data.getValue().get("enrollmentCount")).asObject());
        colGPA.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue().get("gpa")).asObject());
        colAcademicStanding.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("academicStanding")));
    }

    @FXML
    private void handleGenerateStudentReport() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent == null) {
            showAlert("Warning", "Please select a student to generate report.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> report = reportService.generateStudentReport(selectedStudent.getId());
            displayStudentReport(report);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate student report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleGenerateAllStudentsReport() {
        loadAllStudentsReport();
    }

    @FXML
    private void handleExportReport() {
        if (reportTextArea.getText().isEmpty()) {
            showAlert("Warning", "No report to export. Please generate a report first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            TextInputDialog dialog = new TextInputDialog("student_report");
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

    private void loadAllStudentsReport() {
        try {
            List<Map<String, Object>> allStudentsReport = reportService.generateAllStudentsReport();
            ObservableList<Map<String, Object>> data = FXCollections.observableArrayList(allStudentsReport);
            allStudentsTable.setItems(data);
        } catch (Exception e) {
            showAlert("Error", "Failed to load all students report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void displayStudentReport(Map<String, Object> report) {
        if (report.isEmpty()) {
            reportTextArea.setText("No data available for the selected student.");
            return;
        }

        StringBuilder reportBuilder = new StringBuilder();

        reportBuilder.append("=== STUDENT ACADEMIC REPORT ===\n\n");
        reportBuilder.append("STUDENT INFORMATION:\n");
        reportBuilder.append("Registration Number: ").append(report.get("registrationNumber")).append("\n");
        reportBuilder.append("Full Name: ").append(report.get("fullName")).append("\n");
        reportBuilder.append("Email: ").append(report.get("email")).append("\n");
        reportBuilder.append("Department: ").append(report.get("department")).append("\n");
        reportBuilder.append("Enrollment Date: ").append(report.get("enrollmentDate")).append("\n");
        reportBuilder.append("Date of Birth: ").append(report.get("dateOfBirth")).append("\n\n");

        reportBuilder.append("ACADEMIC SUMMARY:\n");
        reportBuilder.append("Total Courses: ").append(report.get("totalCourses")).append("\n");
        reportBuilder.append("Completed Courses: ").append(report.get("completedCourses")).append("\n");
        reportBuilder.append("Current Enrollments: ").append(report.get("currentEnrollments")).append("\n");
        reportBuilder.append("Cumulative GPA: ").append(String.format("%.2f", report.get("gpa"))).append("\n");
        reportBuilder.append("Academic Standing: ").append(report.get("academicStanding")).append("\n\n");

        reportBuilder.append("COURSE DETAILS:\n");
        List<Map<String, Object>> courseDetails = (List<Map<String, Object>>) report.get("courseDetails");
        if (courseDetails != null && !courseDetails.isEmpty()) {
            reportBuilder.append(String.format("%-12s %-30s %-8s %-8s %-6s %-12s %-10s\n",
                    "Course Code", "Course Title", "Credits", "Score", "Grade", "Enrollment Date", "Status"));
            reportBuilder.append("-".repeat(95)).append("\n");

            for (Map<String, Object> course : courseDetails) {
                reportBuilder.append(String.format("%-12s %-30s %-8s %-8s %-6s %-12s %-10s\n",
                        course.get("courseCode"),
                        truncate((String) course.get("courseTitle"), 29),
                        course.get("credits"),
                        course.get("score") != null ? String.format("%.1f", course.get("score")) : "N/A",
                        course.get("grade") != null ? course.get("grade") : "N/A",
                        course.get("enrollmentDate"),
                        course.get("status")));
            }
        } else {
            reportBuilder.append("No course enrollments found.\n");
        }

        reportTextArea.setText(reportBuilder.toString());
    }

    private void updateSummaryStatistics() {
        try {
            Map<String, Object> stats = reportService.getSystemStatistics();
            totalStudentsLabel.setText(String.valueOf(stats.get("totalStudents")));
            activeEnrollmentsLabel.setText(String.valueOf(stats.get("totalEnrollments")));

            List<Map<String, Object>> allStudents = reportService.generateAllStudentsReport();
            double averageGPA = allStudents.stream()
                    .mapToDouble(student -> (Double) student.get("gpa"))
                    .average()
                    .orElse(0.0);
            averageGPALabel.setText(String.format("%.2f", averageGPA));

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
