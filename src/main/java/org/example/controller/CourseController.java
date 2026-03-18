package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import org.example.model.entity.Course;
import org.example.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Controller
public class CourseController implements Initializable {

    @Autowired
    private CourseService courseService;

    // Table and Columns
    @FXML private TableView<Course> courseTable;
    @FXML private TableColumn<Course, Long> colId;
    @FXML private TableColumn<Course, String> colCourseCode;
    @FXML private TableColumn<Course, String> colCourseTitle;
    @FXML private TableColumn<Course, Integer> colCredits;
    @FXML private TableColumn<Course, String> colDescription;
    @FXML private TableColumn<Course, String> colPrerequisites;

    // Form Fields
    @FXML private TextField txtId;
    @FXML private TextField txtCourseCode;
    @FXML private TextField txtCourseTitle;
    @FXML private TextField txtCredits;
    @FXML private TextArea txtCourseDescription;
    @FXML private TextField txtPrerequisites;

    // Buttons
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnClear;

    // Search
    @FXML private TextField txtSearch;
    @FXML private Button btnSearch;

    private ObservableList<Course> courseList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadCourses();
        setupTableSelection();
        setupFormListeners();
        clearForm();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colCourseTitle.setCellValueFactory(new PropertyValueFactory<>("courseTitle"));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("credits"));
        colDescription.setCellValueFactory(new PropertyValueFactory<>("courseDescription"));
        colPrerequisites.setCellValueFactory(new PropertyValueFactory<>("prerequisites"));

        // Make description column wrap text
        colDescription.setCellFactory(tc -> {
            TableCell<Course, String> cell = new TableCell<Course, String>() {
                private Text text = new Text();

                {
                    text.wrappingWidthProperty().bind(colDescription.widthProperty().subtract(10));
                    setGraphic(text);
                    setPrefHeight(Control.USE_COMPUTED_SIZE);
                }

                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        text.setText(null);
                        setGraphic(null);
                    } else {
                        text.setText(item);
                        setGraphic(text);
                    }
                }
            };
            return cell;
        });
    }

    private void setupFormListeners() {
        // Add input validation listeners
        txtCredits.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtCredits.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void loadCourses() {
        try {
            List<Course> courses = courseService.getAllCourses();
            courseList = FXCollections.observableArrayList(courses);
            courseTable.setItems(courseList);
        } catch (Exception e) {
            showAlert("Error", "Failed to load courses: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTableSelection() {
        courseTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateForm(newValue);
                        enableUpdateDeleteButtons(true);
                        btnAdd.setDisable(true);
                    }
                }
        );
    }

    private void populateForm(Course course) {
        txtId.setText(course.getId().toString());
        txtCourseCode.setText(course.getCourseCode());
        txtCourseTitle.setText(course.getCourseTitle());
        txtCredits.setText(String.valueOf(course.getCredits()));
        txtCourseDescription.setText(course.getCourseDescription() != null ? course.getCourseDescription() : "");
        txtPrerequisites.setText(course.getPrerequisites() != null ? course.getPrerequisites() : "");
    }

    @FXML
    private void handleAddCourse() {
        try {
            if (validateForm()) {
                Course course = new Course();
                course.setCourseCode(txtCourseCode.getText().trim());
                course.setCourseTitle(txtCourseTitle.getText().trim());
                course.setCredits(Integer.parseInt(txtCredits.getText().trim()));
                course.setCourseDescription(txtCourseDescription.getText().trim().isEmpty() ? null : txtCourseDescription.getText().trim());
                course.setPrerequisites(txtPrerequisites.getText().trim().isEmpty() ? null : txtPrerequisites.getText().trim());

                Course savedCourse = courseService.saveCourse(course);
                showAlert("Success", "Course added successfully!\nCourse Code: " + savedCourse.getCourseCode(), Alert.AlertType.INFORMATION);
                clearForm();
                loadCourses();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to add course: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleUpdateCourse() {
        try {
            if (validateForm() && txtId.getText() != null && !txtId.getText().isEmpty()) {
                Course course = courseService.getCourseById(Long.parseLong(txtId.getText()))
                        .orElseThrow(() -> new IllegalArgumentException("Course not found"));

                course.setCourseCode(txtCourseCode.getText().trim());
                course.setCourseTitle(txtCourseTitle.getText().trim());
                course.setCredits(Integer.parseInt(txtCredits.getText().trim()));
                course.setCourseDescription(txtCourseDescription.getText().trim().isEmpty() ? null : txtCourseDescription.getText().trim());
                course.setPrerequisites(txtPrerequisites.getText().trim().isEmpty() ? null : txtPrerequisites.getText().trim());

                Course updatedCourse = courseService.updateCourse(course.getId(), course);
                showAlert("Success", "Course updated successfully!", Alert.AlertType.INFORMATION);
                clearForm();
                loadCourses();
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to update course: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleDeleteCourse() {
        try {
            if (txtId.getText() != null && !txtId.getText().isEmpty()) {
                Long courseId = Long.parseLong(txtId.getText());

                Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
                confirmation.setTitle("Confirm Delete");
                confirmation.setHeaderText("Delete Course");
                confirmation.setContentText("Are you sure you want to delete this course? This action cannot be undone and may affect existing enrollments.");

                confirmation.getButtonTypes().setAll(ButtonType.YES, ButtonType.NO);

                if (confirmation.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    courseService.deleteCourse(courseId);
                    showAlert("Success", "Course deleted successfully!", Alert.AlertType.INFORMATION);
                    clearForm();
                    loadCourses();
                }
            } else {
                showAlert("Error", "No course selected to delete.", Alert.AlertType.ERROR);
            }
        } catch (Exception e) {
            showAlert("Error", "Failed to delete course: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace();
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        courseTable.getSelectionModel().clearSelection();
    }

    @FXML
    private void handleSearch() {
        String searchTerm = txtSearch.getText().trim();
        if (searchTerm.isEmpty()) {
            loadCourses();
        } else {
            try {
                List<Course> courses = courseService.searchCourses(searchTerm);
                courseList = FXCollections.observableArrayList(courses);
                courseTable.setItems(courseList);

                if (courses.isEmpty()) {
                    showAlert("Search Results", "No courses found matching: " + searchTerm, Alert.AlertType.INFORMATION);
                }
            } catch (Exception e) {
                showAlert("Error", "Failed to search courses: " + e.getMessage(), Alert.AlertType.ERROR);
            }
        }
    }

    private void clearForm() {
        txtId.clear();
        txtCourseCode.clear();
        txtCourseTitle.clear();
        txtCredits.clear();
        txtCourseDescription.clear();
        txtPrerequisites.clear();
        enableUpdateDeleteButtons(false);
        btnAdd.setDisable(false);

        // Set focus to first field for better UX
        txtCourseCode.requestFocus();
    }

    private void enableUpdateDeleteButtons(boolean enable) {
        btnUpdate.setDisable(!enable);
        btnDelete.setDisable(!enable);
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtCourseCode.getText().trim().isEmpty()) {
            errors.append("• Course Code is required\n");
            txtCourseCode.requestFocus();
        } else if (!txtCourseCode.getText().trim().matches("[A-Za-z]{2,6}\\d{3,4}")) {
            errors.append("• Course Code format is invalid (e.g., CS101, MATH101)\n");
            txtCourseCode.requestFocus();
        }

        if (txtCourseTitle.getText().trim().isEmpty()) {
            errors.append("• Course Title is required\n");
            if (errors.length() == 0) txtCourseTitle.requestFocus();
        }

        if (txtCredits.getText().trim().isEmpty()) {
            errors.append("• Credits are required\n");
            if (errors.length() == 0) txtCredits.requestFocus();
        } else {
            try {
                int credits = Integer.parseInt(txtCredits.getText().trim());
                if (credits <= 0 || credits > 10) {
                    errors.append("• Credits must be between 1 and 10\n");
                    if (errors.length() == 0) txtCredits.requestFocus();
                }
            } catch (NumberFormatException e) {
                errors.append("• Credits must be a valid number\n");
                if (errors.length() == 0) txtCredits.requestFocus();
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