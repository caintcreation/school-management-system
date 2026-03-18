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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
public class GradeController {

    @FXML private TableView<Grade> gradeTable;
    @FXML private TableColumn<Grade, Long> colId;
    @FXML private TableColumn<Grade, String> colStudentName;
    @FXML private TableColumn<Grade, String> colCourseCode;
    @FXML private TableColumn<Grade, String> colGradeValue;
    @FXML private TableColumn<Grade, LocalDateTime> colGradeDate;
    @FXML private TableColumn<Grade, String> colRemarks;

    @FXML private TextField enrollmentIdField;
    @FXML private TextField gradeValueField;
    @FXML private TextArea remarksArea;
    @FXML private TextField searchField;

    @FXML private ComboBox<String> searchTypeComboBox;

    private final GradeService gradeService;
    private final ObservableList<Grade> gradeList = FXCollections.observableArrayList();

    @Autowired
    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @FXML
    public void initialize() {
        setupTableColumns();
        setupSearchComboBox();
        loadAllGrades();
        setupTableSelection();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colStudentName.setCellValueFactory(cellData -> {
            try {
                String fullName = cellData.getValue().getEnrollment().getStudent().getFirstName() + " " +
                        cellData.getValue().getEnrollment().getStudent().getLastName();
                return new SimpleStringProperty(fullName);
            } catch (Exception e) {
                return new SimpleStringProperty("Error loading student");
            }
        });

        colCourseCode.setCellValueFactory(cellData -> {
            try {
                return new SimpleStringProperty(cellData.getValue().getEnrollment().getCourse().getCourseCode());
            } catch (Exception e) {
                return new SimpleStringProperty("Error loading course");
            }
        });

        colGradeValue.setCellValueFactory(new PropertyValueFactory<>("gradeValue"));
        colGradeDate.setCellValueFactory(new PropertyValueFactory<>("gradeDate"));
        colRemarks.setCellValueFactory(new PropertyValueFactory<>("remarks"));

        gradeTable.setItems(gradeList);
    }

    private void setupSearchComboBox() {
        searchTypeComboBox.setItems(FXCollections.observableArrayList(
                "All", "Student ID", "Course ID", "Grade Value", "Student Registration"
        ));
        searchTypeComboBox.setValue("All");
    }

    private void setupTableSelection() {
        gradeTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> populateFields(newValue));
    }

    @FXML
    private void handleAddGrade() {
        try {
            Long enrollmentId = Long.parseLong(enrollmentIdField.getText().trim());
            String gradeValue = gradeValueField.getText().trim();
            String remarks = remarksArea.getText().trim();

            Grade grade = gradeService.assignGradeToEnrollment(enrollmentId, gradeValue, remarks);
            showAlert("Success", "Grade added successfully!", Alert.AlertType.INFORMATION);
            clearFields();
            loadAllGrades();

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid enrollment ID", Alert.AlertType.ERROR);
        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to add grade: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateGrade() {
        Grade selectedGrade = gradeTable.getSelectionModel().getSelectedItem();
        if (selectedGrade == null) {
            showAlert("Error", "Please select a grade to update", Alert.AlertType.ERROR);
            return;
        }

        try {
            String gradeValue = gradeValueField.getText().trim();
            String remarks = remarksArea.getText().trim();

            selectedGrade.setGradeValue(gradeValue);
            selectedGrade.setRemarks(remarks);
            selectedGrade.setGradeDate(LocalDateTime.now());

            gradeService.updateGrade(selectedGrade.getId(), selectedGrade);
            showAlert("Success", "Grade updated successfully!", Alert.AlertType.INFORMATION);
            clearFields();
            loadAllGrades();

        } catch (IllegalArgumentException e) {
            showAlert("Error", e.getMessage(), Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Failed to update grade: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteGrade() {
        Grade selectedGrade = gradeTable.getSelectionModel().getSelectedItem();
        if (selectedGrade == null) {
            showAlert("Error", "Please select a grade to delete", Alert.AlertType.ERROR);
            return;
        }

        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Confirm Delete");
        confirmAlert.setHeaderText("Delete Grade");
        confirmAlert.setContentText("Are you sure you want to delete this grade?");

        Optional<ButtonType> result = confirmAlert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                gradeService.deleteGrade(selectedGrade.getId());
                showAlert("Success", "Grade deleted successfully!", Alert.AlertType.INFORMATION);
                clearFields();
                loadAllGrades();
            } catch (Exception e) {
                showAlert("Error", "Failed to delete grade: " + e.getMessage(), Alert.AlertType.ERROR);
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleSearch() {
        String searchTerm = searchField.getText().trim();
        String searchType = searchTypeComboBox.getValue();

        try {
            List<Grade> results;

            switch (searchType) {
                case "Student ID":
                    Long studentId = Long.parseLong(searchTerm);
                    results = gradeService.findGradesByStudentId(studentId);
                    break;
                case "Course ID":
                    Long courseId = Long.parseLong(searchTerm);
                    results = gradeService.findGradesByCourseId(courseId);
                    break;
                case "Grade Value":
                    results = gradeService.findGradesByGradeValue(searchTerm);
                    break;
                case "Student Registration":
                    results = gradeService.findGradesByStudentRegistrationNumber(searchTerm);
                    break;
                default:
                    results = gradeService.findAllGradesWithEnrollment(); // Use the method that forces initialization
                    break;
            }

            gradeList.setAll(results);

        } catch (NumberFormatException e) {
            showAlert("Error", "Please enter a valid numeric ID", Alert.AlertType.ERROR);
        } catch (Exception e) {
            showAlert("Error", "Search failed: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearSearch() {
        searchField.clear();
        loadAllGrades();
    }

    @FXML
    private void handleGenerateReport() {
        try {
            List<Grade> allGrades = gradeService.findAllGradesWithEnrollment(); // Use the method that forces initialization
            generateGradeReport(allGrades);
        } catch (Exception e) {
            showAlert("Error", "Failed to generate report: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void populateFields(Grade grade) {
        if (grade != null) {
            try {
                enrollmentIdField.setText(String.valueOf(grade.getEnrollment().getId()));
                gradeValueField.setText(grade.getGradeValue());
                remarksArea.setText(grade.getRemarks());
            } catch (Exception e) {
                showAlert("Error", "Failed to load grade details: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void clearFields() {
        enrollmentIdField.clear();
        gradeValueField.clear();
        remarksArea.clear();
    }

    private void loadAllGrades() {
        try {
            // Use the method that forces initialization of lazy relationships
            List<Grade> grades = gradeService.findAllGradesWithEnrollment();
            gradeList.setAll(grades);
        } catch (Exception e) {
            showAlert("Error", "Failed to load grades: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    private void generateGradeReport(List<Grade> grades) {
        StringBuilder report = new StringBuilder();
        report.append("GRADE REPORT\n");
        report.append("Generated on: ").append(LocalDateTime.now()).append("\n\n");
        report.append("Total Grades: ").append(grades.size()).append("\n\n");

        report.append(String.format("%-5s %-20s %-15s %-10s %-20s %s\n",
                "ID", "Student", "Course", "Grade", "Date", "Remarks"));
        report.append("--------------------------------------------------------------------------------\n");

        for (Grade grade : grades) {
            try {
                String studentName = grade.getEnrollment().getStudent().getFirstName() + " " +
                        grade.getEnrollment().getStudent().getLastName();
                String courseCode = grade.getEnrollment().getCourse().getCourseCode();

                report.append(String.format("%-5d %-20s %-15s %-10s %-20s %s\n",
                        grade.getId(),
                        studentName.length() > 19 ? studentName.substring(0, 19) : studentName,
                        courseCode,
                        grade.getGradeValue(),
                        grade.getGradeDate().toLocalDate().toString(),
                        grade.getRemarks() != null ?
                                (grade.getRemarks().length() > 30 ? grade.getRemarks().substring(0, 30) + "..." : grade.getRemarks())
                                : "N/A"
                ));
            } catch (Exception e) {
                report.append(String.format("%-5d %-20s %-15s %-10s %-20s %s\n",
                        grade.getId(),
                        "Error loading student",
                        "Error loading course",
                        grade.getGradeValue(),
                        grade.getGradeDate().toLocalDate().toString(),
                        grade.getRemarks() != null ? grade.getRemarks() : "N/A"
                ));
            }
        }

        // Calculate statistics
        long aGrades = grades.stream().filter(g -> g.getGradeValue().toUpperCase().startsWith("A")).count();
        long bGrades = grades.stream().filter(g -> g.getGradeValue().toUpperCase().startsWith("B")).count();
        long cGrades = grades.stream().filter(g -> g.getGradeValue().toUpperCase().startsWith("C")).count();
        long dGrades = grades.stream().filter(g -> g.getGradeValue().toUpperCase().startsWith("D")).count();
        long fGrades = grades.stream().filter(g -> g.getGradeValue().toUpperCase().equals("F")).count();

        report.append("\nGRADE DISTRIBUTION:\n");
        report.append("A Grades: ").append(aGrades).append("\n");
        report.append("B Grades: ").append(bGrades).append("\n");
        report.append("C Grades: ").append(cGrades).append("\n");
        report.append("D Grades: ").append(dGrades).append("\n");
        report.append("F Grades: ").append(fGrades).append("\n");

        showReportDialog("Grade Report", report.toString());
    }

    private void showReportDialog(String title, String content) {
        TextArea textArea = new TextArea(content);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefSize(600, 400);

        ScrollPane scrollPane = new ScrollPane(textArea);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        Stage stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(scrollPane, 620, 450));
        stage.show();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}