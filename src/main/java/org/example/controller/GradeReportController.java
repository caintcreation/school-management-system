package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.beans.property.SimpleStringProperty;
import org.example.model.entity.Grade;
import org.example.Service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class GradeReportController {

    @FXML private TableView<Grade> reportTable;
    @FXML private TableColumn<Grade, String> colStudentReg;
    @FXML private TableColumn<Grade, String> colStudentName;
    @FXML private TableColumn<Grade, String> colCourse;
    @FXML private TableColumn<Grade, String> colGrade;
    @FXML private TableColumn<Grade, String> colDate;
    @FXML private TableColumn<Grade, String> colRemarks;

    @FXML private ComboBox<String> reportTypeComboBox;
    @FXML private TextField filterField;
    @FXML private Label summaryLabel;

    private final GradeService gradeService;
    private final ObservableList<Grade> reportData = FXCollections.observableArrayList();

    @Autowired
    public GradeReportController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupReportTypeComboBox();
        loadAllGradesForReport();
        updateSummary();
    }

    private void setupTableColumns() {
        // Fixed column bindings using JavaFX Properties
        colStudentReg.setCellValueFactory(cellData ->
                cellData.getValue().getEnrollment().getStudent().registrationNumberProperty());

        colStudentName.setCellValueFactory(cellData -> {
            String fullName = cellData.getValue().getEnrollment().getStudent().getFirstName() + " " +
                    cellData.getValue().getEnrollment().getStudent().getLastName();
            return new SimpleStringProperty(fullName);
        });

        colCourse.setCellValueFactory(cellData -> {
            String courseInfo = cellData.getValue().getEnrollment().getCourse().getCourseCode() + " - " +
                    cellData.getValue().getEnrollment().getCourse().getCourseTitle();
            return new SimpleStringProperty(courseInfo);
        });

        colGrade.setCellValueFactory(cellData -> cellData.getValue().gradeValueProperty());

        colDate.setCellValueFactory(cellData -> {
            String dateString = cellData.getValue().getGradeDate() != null ?
                    cellData.getValue().getGradeDate().toLocalDate().toString() : "N/A";
            return new SimpleStringProperty(dateString);
        });

        colRemarks.setCellValueFactory(cellData -> {
            String remarks = cellData.getValue().getRemarks();
            return new SimpleStringProperty(remarks != null ? remarks : "");
        });

        reportTable.setItems(reportData);
    }

    private void setupReportTypeComboBox() {
        reportTypeComboBox.setItems(FXCollections.observableArrayList(
                "All Grades", "By Student Registration", "By Course Code", "Grade Distribution", "Failed Grades"
        ));
        reportTypeComboBox.setValue("All Grades");

        reportTypeComboBox.setOnAction(e -> generateSelectedReport());
    }

    @FXML
    private void generateSelectedReport() {
        String reportType = reportTypeComboBox.getValue();
        String filter = filterField.getText().trim();

        try {
            switch (reportType) {
                case "All Grades":
                    loadAllGradesForReport();
                    break;
                case "By Student Registration":
                    if (!filter.isEmpty()) {
                        List<Grade> studentGrades = gradeService.findGradesByStudentRegistrationNumber(filter);
                        reportData.setAll(studentGrades);
                    } else {
                        loadAllGradesForReport();
                    }
                    break;
                case "By Course Code":
                    if (!filter.isEmpty()) {
                        List<Grade> courseGrades = gradeService.findGradesByCourseCode(filter);
                        reportData.setAll(courseGrades);
                    } else {
                        loadAllGradesForReport();
                    }
                    break;
                case "Grade Distribution":
                    generateGradeDistributionReport();
                    break;
                case "Failed Grades":
                    List<Grade> failedGrades = gradeService.findGradesByGradeValue("F");
                    reportData.setAll(failedGrades);
                    break;
                default:
                    loadAllGradesForReport();
                    break;
            }
            updateSummary();
        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleExportReport() {
        try {
            StringBuilder exportContent = new StringBuilder();
            exportContent.append("GRADE REPORT\n");
            exportContent.append("Generated on: ").append(java.time.LocalDateTime.now()).append("\n");
            exportContent.append("Report Type: ").append(reportTypeComboBox.getValue()).append("\n");
            if (!filterField.getText().isEmpty()) {
                exportContent.append("Filter: ").append(filterField.getText()).append("\n");
            }
            exportContent.append("=").append("=".repeat(80)).append("\n\n");

            exportContent.append(String.format("%-15s %-25s %-20s %-8s %-12s %s\n",
                    "Reg Number", "Student Name", "Course", "Grade", "Date", "Remarks"));
            exportContent.append("-".repeat(100)).append("\n");

            for (Grade grade : reportData) {
                String studentName = grade.getEnrollment().getStudent().getFirstName() + " " +
                        grade.getEnrollment().getStudent().getLastName();
                String courseCode = grade.getEnrollment().getCourse().getCourseCode();
                String date = grade.getGradeDate() != null ? grade.getGradeDate().toLocalDate().toString() : "N/A";
                String remarks = grade.getRemarks() != null ? grade.getRemarks() : "";

                exportContent.append(String.format("%-15s %-25s %-20s %-8s %-12s %s\n",
                        grade.getEnrollment().getStudent().getRegistrationNumber(),
                        studentName.length() > 24 ? studentName.substring(0, 24) : studentName,
                        courseCode.length() > 19 ? courseCode.substring(0, 19) : courseCode,
                        grade.getGradeValue(),
                        date,
                        remarks.length() > 30 ? remarks.substring(0, 30) + "..." : remarks
                ));
            }

            // Show export dialog
            TextArea textArea = new TextArea(exportContent.toString());
            textArea.setEditable(false);
            textArea.setWrapText(true);
            textArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 12px;");

            ScrollPane scrollPane = new ScrollPane(textArea);
            scrollPane.setPrefSize(800, 600);

            Stage stage = new Stage();
            stage.setTitle("Exported Grade Report");
            stage.setScene(new Scene(scrollPane));
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to export report: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePrintReport() {
        showAlert("Information", "Print functionality would be implemented here with proper JavaFX printing API.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleRefresh() {
        loadAllGradesForReport();
        filterField.clear();
        reportTypeComboBox.setValue("All Grades");
    }

    private void loadAllGradesForReport() {
        try {
            List<Grade> allGrades = gradeService.findAllGrades();
            reportData.setAll(allGrades);
        } catch (Exception e) {
            showAlert("Error", "Failed to load grades: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void generateGradeDistributionReport() {
        try {
            List<Grade> allGrades = gradeService.findAllGrades();

            // Calculate grade distribution
            Map<String, Long> gradeDistribution = allGrades.stream()
                    .collect(Collectors.groupingBy(
                            grade -> grade.getGradeValue().toUpperCase(),
                            Collectors.counting()
                    ));

            // Update summary with distribution
            StringBuilder distribution = new StringBuilder("GRADE DISTRIBUTION REPORT\n\n");
            long totalGrades = allGrades.size();

            gradeDistribution.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByKey())
                    .forEach(entry -> {
                        double percentage = totalGrades > 0 ? (entry.getValue() * 100.0) / totalGrades : 0;
                        distribution.append(String.format("%-5s: %3d grades (%5.1f%%)\n",
                                entry.getKey(), entry.getValue(), percentage));
                    });

            distribution.append("\nTotal Grades: ").append(totalGrades);

            summaryLabel.setText(distribution.toString());

            // Still show all grades in table
            reportData.setAll(allGrades);

        } catch (Exception e) {
            showAlert("Error", "Failed to generate distribution report: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void updateSummary() {
        int totalGrades = reportData.size();
        if (totalGrades == 0) {
            summaryLabel.setText("No grades found in the current report.");
            return;
        }

        long distinctStudents = reportData.stream()
                .map(grade -> grade.getEnrollment().getStudent().getId())
                .distinct()
                .count();

        long distinctCourses = reportData.stream()
                .map(grade -> grade.getEnrollment().getCourse().getId())
                .distinct()
                .count();

        // Calculate grade statistics
        Map<String, Long> gradeCounts = reportData.stream()
                .collect(Collectors.groupingBy(
                        grade -> grade.getGradeValue().toUpperCase(),
                        Collectors.counting()
                ));

        StringBuilder summary = new StringBuilder();
        summary.append("REPORT SUMMARY\n\n");
        summary.append(String.format("Total Grades: %d\n", totalGrades));
        summary.append(String.format("Unique Students: %d\n", distinctStudents));
        summary.append(String.format("Unique Courses: %d\n", distinctCourses));

        if (!gradeCounts.isEmpty()) {
            summary.append("\nGrade Breakdown:\n");
            gradeCounts.entrySet().stream()
                    .sorted(Map.Entry.<String, Long>comparingByKey())
                    .forEach(entry ->
                            summary.append(String.format("  %s: %d\n", entry.getKey(), entry.getValue())));
        }

        summaryLabel.setText(summary.toString());
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}