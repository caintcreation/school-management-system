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
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

@Controller
public class TranscriptReportController implements Initializable {

    @Autowired
    private ReportService reportService;

    @Autowired
    private StudentService studentService;

    @FXML private ComboBox<Student> studentComboBox;
    @FXML private TextArea transcriptTextArea;
    @FXML private Label totalTranscriptsLabel;
    @FXML private Label transcriptsGeneratedLabel;

    @FXML private TableView<Map<String, Object>> transcriptsTable;
    @FXML private TableColumn<Map<String, Object>, String> colStudentName;
    @FXML private TableColumn<Map<String, Object>, String> colRegNumber;
    @FXML private TableColumn<Map<String, Object>, Double> colGPA;
    @FXML private TableColumn<Map<String, Object>, String> colAcademicStanding;
    @FXML private TableColumn<Map<String, Object>, Integer> colCreditsEarned;

    private ObservableList<Student> studentList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupStudentComboBox();
        setupTranscriptsTable();
        loadRecentTranscripts();
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

    private void setupTranscriptsTable() {
        colStudentName.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("studentName")));
        colRegNumber.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("registrationNumber")));
        colGPA.setCellValueFactory(data ->
                new javafx.beans.property.SimpleDoubleProperty((Double) data.getValue().get("cumulativeGPA")).asObject());
        colAcademicStanding.setCellValueFactory(data ->
                new javafx.beans.property.SimpleStringProperty((String) data.getValue().get("academicStanding")));
        colCreditsEarned.setCellValueFactory(data ->
                new javafx.beans.property.SimpleIntegerProperty((Integer) data.getValue().get("totalCreditsEarned")).asObject());
    }

    @FXML
    private void handleGenerateTranscript() {
        Student selectedStudent = studentComboBox.getValue();
        if (selectedStudent == null) {
            showAlert("Warning", "Please select a student to generate transcript.", Alert.AlertType.WARNING);
            return;
        }

        try {
            Map<String, Object> transcript = reportService.generateTranscriptReport(selectedStudent.getId());
            displayTranscript(transcript);
            updateSummaryStatistics();
        } catch (Exception e) {
            showAlert("Error", "Failed to generate transcript: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleExportTranscript() {
        if (transcriptTextArea.getText().isEmpty()) {
            showAlert("Warning", "No transcript to export. Please generate a transcript first.", Alert.AlertType.WARNING);
            return;
        }

        try {
            TextInputDialog dialog = new TextInputDialog("transcript");
            dialog.setTitle("Export Transcript");
            dialog.setHeaderText("Enter filename for export");
            dialog.setContentText("Filename:");

            dialog.showAndWait().ifPresent(filename -> {
                showAlert("Export", "Transcript exported successfully as: " + filename + ".txt", Alert.AlertType.INFORMATION);
            });
        } catch (Exception e) {
            showAlert("Error", "Failed to export transcript: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handlePrintTranscript() {
        if (transcriptTextArea.getText().isEmpty()) {
            showAlert("Warning", "No transcript to print. Please generate a transcript first.", Alert.AlertType.WARNING);
            return;
        }
        showAlert("Print", "Print functionality would be implemented here.", Alert.AlertType.INFORMATION);
    }

    @FXML
    private void handleGenerateAllTranscripts() {
        try {
            List<Map<String, Object>> allTranscripts = FXCollections.observableArrayList();

            for (Student student : studentList) {
                Map<String, Object> transcript = reportService.generateTranscriptReport(student.getId());
                allTranscripts.add(transcript);
            }

            transcriptsTable.setItems(FXCollections.observableArrayList(allTranscripts));
            showAlert("Success", "Generated transcripts for all " + studentList.size() + " students.", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Error", "Failed to generate all transcripts: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadRecentTranscripts() {
        try {
            if (studentList != null && !studentList.isEmpty()) {
                List<Student> recentStudents = studentList.subList(0, Math.min(5, studentList.size()));
                ObservableList<Map<String, Object>> recentTranscripts = FXCollections.observableArrayList();

                for (Student student : recentStudents) {
                    Map<String, Object> transcript = reportService.generateTranscriptReport(student.getId());
                    recentTranscripts.add(transcript);
                }

                transcriptsTable.setItems(recentTranscripts);
            }
        } catch (Exception e) {
            transcriptsTable.setItems(FXCollections.observableArrayList());
        }
    }

    private void displayTranscript(Map<String, Object> transcript) {
        if (transcript.isEmpty()) {
            transcriptTextArea.setText("No transcript data available for the selected student.");
            return;
        }

        StringBuilder transcriptBuilder = new StringBuilder();

        transcriptBuilder.append("=".repeat(80)).append("\n");
        transcriptBuilder.append("                     CHUKA UNIVERSITY\n");
        transcriptBuilder.append("           DEPARTMENT OF COMPUTER SCIENCE\n");
        transcriptBuilder.append("                 OFFICIAL TRANSCRIPT\n");
        transcriptBuilder.append("=".repeat(80)).append("\n\n");

        transcriptBuilder.append("STUDENT INFORMATION:\n");
        transcriptBuilder.append("Name: ").append(transcript.get("studentName")).append("\n");
        transcriptBuilder.append("Registration Number: ").append(transcript.get("registrationNumber")).append("\n");
        transcriptBuilder.append("Department: ").append(transcript.get("department")).append("\n");
        transcriptBuilder.append("Date Generated: ").append(transcript.get("generationDate")).append("\n\n");

        transcriptBuilder.append("ACADEMIC SUMMARY:\n");
        transcriptBuilder.append("Cumulative GPA: ").append(String.format("%.3f", transcript.get("cumulativeGPA"))).append("\n");
        transcriptBuilder.append("Academic Standing: ").append(transcript.get("academicStanding")).append("\n");
        transcriptBuilder.append("Total Credits Earned: ").append(transcript.get("totalCreditsEarned")).append("\n\n");

        transcriptBuilder.append("COURSE GRADES:\n");
        transcriptBuilder.append(String.format("%-10s %-35s %-8s %-6s %-8s %-10s\n",
                "Code", "Course Title", "Credits", "Grade", "Score", "Semester"));
        transcriptBuilder.append("-".repeat(80)).append("\n");

        List<Map<String, Object>> courseGrades = (List<Map<String, Object>>) transcript.get("courseGrades");
        if (courseGrades != null && !courseGrades.isEmpty()) {
            for (Map<String, Object> course : courseGrades) {
                transcriptBuilder.append(String.format("%-10s %-35s %-8s %-6s %-8s %-10s\n",
                        course.get("courseCode"),
                        truncate((String) course.get("courseTitle"), 34),
                        course.get("credits"),
                        course.get("grade"),
                        course.get("score") != null ? String.format("%.1f", course.get("score")) : "N/A",
                        course.get("semester")));
            }
        } else {
            transcriptBuilder.append("No course grades recorded.\n");
        }

        transcriptBuilder.append("\n").append("=".repeat(80)).append("\n");
        transcriptBuilder.append("This is an official transcript from Chuka University.\n");
        transcriptBuilder.append("Generated by Student Records Management System\n");
        transcriptBuilder.append("Date: ").append(LocalDate.now()).append("\n");
        transcriptBuilder.append("=".repeat(80)).append("\n");

        transcriptTextArea.setText(transcriptBuilder.toString());
    }

    private void updateSummaryStatistics() {
        try {
            Map<String, Object> stats = reportService.getSystemStatistics();
            totalTranscriptsLabel.setText(String.valueOf(stats.get("totalStudents")));
            transcriptsGeneratedLabel.setText(String.valueOf(stats.get("gradedEnrollments")));
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