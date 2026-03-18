package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.example.model.entity.Enrollment;
import org.example.model.entity.Student;
import org.example.model.entity.Course;
import org.example.Service.EnrollmentService;
import org.example.service.StudentService;
import org.example.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class EnrollmentController implements Initializable {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private StudentService studentService;

    @Autowired
    private CourseService courseService;

    // Table and Columns
    @FXML private TableView<Enrollment> enrollmentTableView;
    @FXML private TableColumn<Enrollment, Long> colId;
    @FXML private TableColumn<Enrollment, String> colStudent;
    @FXML private TableColumn<Enrollment, String> colCourse;
    @FXML private TableColumn<Enrollment, LocalDate> colEnrollmentDate;
    @FXML private TableColumn<Enrollment, Double> colScore;
    @FXML private TableColumn<Enrollment, String> colGrade;
    @FXML private TableColumn<Enrollment, Void> colActions;

    // Form Fields
    @FXML private ComboBox<Student> studentComboBox;
    @FXML private ComboBox<Course> courseComboBox;
    @FXML private DatePicker enrollmentDatePicker;
    @FXML private TextField scoreField;
    @FXML private Label gradeLabel;
    @FXML private TextField searchField;

    // Buttons
    @FXML private Button btnEnroll;
    @FXML private Button btnUpdate;
    @FXML private Button btnClear;
    @FXML private Button btnSearch;
    @FXML private Button btnRefresh;

    private ObservableList<Enrollment> enrollmentList;
    private ObservableList<Student> studentList;
    private ObservableList<Course> courseList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadEnrollments();
        loadStudents();
        loadCourses();
        setupTableSelection();
        setupActionsColumn();
        setupScoreListener();

        // Set default date to today
        enrollmentDatePicker.setValue(LocalDate.now());

        // Initial button states
        updateButtonStates(false);
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));

        colStudent.setCellValueFactory(cellData -> {
            Enrollment enrollment = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(enrollment.getStudentInfo());
        });

        colCourse.setCellValueFactory(cellData -> {
            Enrollment enrollment = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(enrollment.getCourseInfo());
        });

        colEnrollmentDate.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));
        colScore.setCellValueFactory(new PropertyValueFactory<>("score"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
    }

    private void setupActionsColumn() {
        colActions.setCellFactory(param -> new TableCell<Enrollment, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");

            {
                editButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-padding: 5 10 5 10;");
                deleteButton.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-padding: 5 10 5 10;");

                editButton.setOnAction(event -> {
                    Enrollment enrollment = getTableView().getItems().get(getIndex());
                    handleEditEnrollment(enrollment);
                });

                deleteButton.setOnAction(event -> {
                    Enrollment enrollment = getTableView().getItems().get(getIndex());
                    handleDeleteEnrollment(enrollment);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    HBox buttons = new HBox(5, editButton, deleteButton);
                    setGraphic(buttons);
                }
            }
        });
    }

    private void setupScoreListener() {
        // Add real-time score validation and grade calculation
        scoreField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.trim().isEmpty()) {
                try {
                    double score = Double.parseDouble(newValue.trim());
                    if (score >= 0 && score <= 100) {
                        String grade = EnrollmentService.calculateGrade(score);
                        gradeLabel.setText("Grade: " + grade);

                        // Highlight FAIL grades in red
                        if ("FAIL".equals(grade)) {
                            gradeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
                        } else {
                            gradeLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                        }
                    } else {
                        gradeLabel.setText("Grade: Invalid score");
                        gradeLabel.setStyle("-fx-text-fill: #e74c3c;");
                    }
                } catch (NumberFormatException e) {
                    gradeLabel.setText("Grade: Enter numeric value");
                    gradeLabel.setStyle("-fx-text-fill: #e74c3c;");
                }
            } else {
                gradeLabel.setText("Grade: ");
                gradeLabel.setStyle("-fx-text-fill: #2c3e50;");
            }
        });
    }

    private void loadEnrollments() {
        try {
            List<Enrollment> enrollments = enrollmentService.getAllEnrollments();
            enrollmentList = FXCollections.observableArrayList(enrollments);
            enrollmentTableView.setItems(enrollmentList);
        } catch (Exception e) {
            showAlert("Error", "Failed to load enrollments: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList = FXCollections.observableArrayList(students);
            studentComboBox.setItems(studentList);
            studentComboBox.setConverter(new javafx.util.StringConverter<Student>() {
                @Override
                public String toString(Student student) {
                    return student == null ? null : student.getRegistrationNumber() + " - " +
                            student.getFirstName() + " " + student.getLastName();
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

    private void loadCourses() {
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

    private void setupTableSelection() {
        enrollmentTableView.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                        updateButtonStates(true);
                    } else {
                        clearForm();
                        updateButtonStates(false);
                    }
                }
        );
    }

    private void populateForm(Enrollment enrollment) {
        studentComboBox.setValue(enrollment.getStudent());
        courseComboBox.setValue(enrollment.getCourse());
        enrollmentDatePicker.setValue(enrollment.getEnrollmentDate());

        if (enrollment.getScore() != null) {
            scoreField.setText(String.valueOf(enrollment.getScore()));
            gradeLabel.setText("Grade: " + (enrollment.getGrade() != null ? enrollment.getGrade() : ""));
            if ("FAIL".equals(enrollment.getGrade())) {
                gradeLabel.setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold;");
            } else {
                gradeLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        } else {
            scoreField.clear();
            gradeLabel.setText("Grade: ");
            gradeLabel.setStyle("-fx-text-fill: #2c3e50;");
        }
    }

    @FXML
    private void handleEnrollStudent() {
        try {
            if (validateForm()) {
                Enrollment enrollment = new Enrollment();
                enrollment.setStudent(studentComboBox.getValue());
                enrollment.setCourse(courseComboBox.getValue());
                enrollment.setEnrollmentDate(enrollmentDatePicker.getValue());

                // Set score if provided
                if (!scoreField.getText().trim().isEmpty()) {
                    double score = Double.parseDouble(scoreField.getText().trim());
                    enrollment.setScore(score);
                }

                Enrollment savedEnrollment = enrollmentService.saveEnrollment(enrollment);
                showAlert("Success", "Student enrolled successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadEnrollments();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to enroll student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateEnrollment() {
        try {
            Enrollment selectedEnrollment = enrollmentTableView.getSelectionModel().getSelectedItem();
            if (selectedEnrollment != null && validateForm()) {
                selectedEnrollment.setStudent(studentComboBox.getValue());
                selectedEnrollment.setCourse(courseComboBox.getValue());
                selectedEnrollment.setEnrollmentDate(enrollmentDatePicker.getValue());

                // Set score if provided
                if (!scoreField.getText().trim().isEmpty()) {
                    double score = Double.parseDouble(scoreField.getText().trim());
                    selectedEnrollment.setScore(score);
                } else {
                    selectedEnrollment.setScore(null);
                }

                Enrollment updatedEnrollment = enrollmentService.updateEnrollment(selectedEnrollment);
                showAlert("Success", "Enrollment updated successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadEnrollments();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update enrollment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void handleEditEnrollment(Enrollment enrollment) {
        enrollmentTableView.getSelectionModel().select(enrollment);
        populateForm(enrollment);
        updateButtonStates(true);
    }

    private void handleDeleteEnrollment(Enrollment enrollment) {
        try {
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Enrollment");
            confirmation.setContentText("Are you sure you want to delete this enrollment record?\nThis action cannot be undone.");

            confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

            if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                enrollmentService.deleteEnrollment(enrollment.getId());
                showAlert("Success", "Enrollment deleted successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadEnrollments();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to delete enrollment: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        enrollmentTableView.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().trim();
        if (searchText.isEmpty()) {
            loadEnrollments();
        } else {
            try {
                List<Enrollment> enrollments = enrollmentService.searchEnrollments(searchText);
                enrollmentList = FXCollections.observableArrayList(enrollments);
                enrollmentTableView.setItems(enrollmentList);

                if (enrollments.isEmpty()) {
                    showAlert("Search Results", "No enrollments found matching: " + searchText, Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to search enrollments: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    @FXML
    private void handleRefresh() {
        loadEnrollments();
        loadStudents();
        loadCourses();
        searchField.clear();
        clearForm();
        showAlert("Refreshed", "Data has been refreshed successfully.", Alert.AlertType.INFORMATION);
    }

    private void clearForm() {
        studentComboBox.setValue(null);
        courseComboBox.setValue(null);
        enrollmentDatePicker.setValue(LocalDate.now());
        scoreField.clear();
        gradeLabel.setText("Grade: ");
        gradeLabel.setStyle("-fx-text-fill: #2c3e50;");
        updateButtonStates(false);

        // Set focus to first field
        studentComboBox.requestFocus();
    }

    private void updateButtonStates(boolean editing) {
        btnUpdate.setDisable(!editing);
        btnEnroll.setDisable(editing);
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (studentComboBox.getValue() == null) {
            errors.append("• Student is required\n");
        }
        if (courseComboBox.getValue() == null) {
            errors.append("• Course is required\n");
        }
        if (enrollmentDatePicker.getValue() == null) {
            errors.append("• Enrollment Date is required\n");
        } else if (enrollmentDatePicker.getValue().isAfter(LocalDate.now())) {
            errors.append("• Enrollment Date cannot be in the future\n");
        }

        // Validate score if provided
        String scoreText = scoreField.getText().trim();
        if (!scoreText.isEmpty()) {
            try {
                double score = Double.parseDouble(scoreText);
                if (score < 0 || score > 100) {
                    errors.append("• Score must be between 0 and 100\n");
                } else {
                    // Check if score results in FAIL
                    String calculatedGrade = EnrollmentService.calculateGrade(score);
                    if ("FAIL".equals(calculatedGrade)) {
                        errors.append("• Student cannot be enrolled with a failing score (" + score + "). Minimum required: 60\n");
                    }
                }
            } catch (NumberFormatException e) {
                errors.append("• Score must be a valid number between 0 and 100\n");
            }
        }

        if (errors.length() > 0) {
            showAlert("Validation Error", "Please fix the following errors:\n\n" + errors.toString(), Alert.AlertType.ERROR);
            return false;
        }

        return true;
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}