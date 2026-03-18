package org.example.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import org.example.model.entity.Student;
import org.example.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

@Controller
public class StudentController implements Initializable {

    @Autowired
    private StudentService studentService;

    // Form fields
    @FXML private TextField txtId;
    @FXML private TextField txtRegistrationNumber;
    @FXML private TextField txtFirstName;
    @FXML private TextField txtLastName;
    @FXML private TextField txtEmail;
    @FXML private TextField txtDepartment;
    @FXML private DatePicker dateDateOfBirth;
    @FXML private DatePicker dateEnrollmentDate;

    // Search field
    @FXML private TextField txtSearch;

    // Buttons
    @FXML private Button btnAdd;
    @FXML private Button btnUpdate;
    @FXML private Button btnDelete;
    @FXML private Button btnSearch;
    @FXML private Button btnClear;

    // Table and columns
    @FXML private TableView<Student> studentTable;
    @FXML private TableColumn<Student, Long> colId;
    @FXML private TableColumn<Student, String> colRegNumber;
    @FXML private TableColumn<Student, String> colFirstName;
    @FXML private TableColumn<Student, String> colLastName;
    @FXML private TableColumn<Student, String> colEmail;
    @FXML private TableColumn<Student, String> colDepartment;
    @FXML private TableColumn<Student, LocalDate> colEnrollmentDate;

    private ObservableList<Student> studentList;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTableColumns();
        loadAllStudents();
        setupTableSelection();
        disableUpdateDeleteButtons();
    }

    private void setupTableColumns() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colRegNumber.setCellValueFactory(new PropertyValueFactory<>("registrationNumber"));
        colFirstName.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        colLastName.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colDepartment.setCellValueFactory(new PropertyValueFactory<>("department"));
        colEnrollmentDate.setCellValueFactory(new PropertyValueFactory<>("enrollmentDate"));

        studentList = FXCollections.observableArrayList();
        studentTable.setItems(studentList);
    }

    private void loadAllStudents() {
        try {
            List<Student> students = studentService.getAllStudents();
            studentList.clear();
            studentList.addAll(students);
        } catch (Exception e) {
            showAlert("Error", "Failed to load students: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void setupTableSelection() {
        studentTable.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        populateFormWithStudent(newValue);
                        enableUpdateDeleteButtons();
                    } else {
                        clearForm();
                        disableUpdateDeleteButtons();
                    }
                }
        );
    }

    private void populateFormWithStudent(Student student) {
        txtId.setText(student.getId().toString());
        txtRegistrationNumber.setText(student.getRegistrationNumber());
        txtFirstName.setText(student.getFirstName());
        txtLastName.setText(student.getLastName());
        txtEmail.setText(student.getEmail() != null ? student.getEmail() : "");
        txtDepartment.setText(student.getDepartment() != null ? student.getDepartment() : "");
        dateDateOfBirth.setValue(student.getDateOfBirth());
        dateEnrollmentDate.setValue(student.getEnrollmentDate());
    }

    private void clearForm() {
        txtId.clear();
        txtRegistrationNumber.clear();
        txtFirstName.clear();
        txtLastName.clear();
        txtEmail.clear();
        txtDepartment.clear();
        dateDateOfBirth.setValue(null);
        dateEnrollmentDate.setValue(null);
    }

    private void enableUpdateDeleteButtons() {
        btnUpdate.setDisable(false);
        btnDelete.setDisable(false);
        btnAdd.setDisable(true);
    }

    private void disableUpdateDeleteButtons() {
        btnUpdate.setDisable(true);
        btnDelete.setDisable(true);
        btnAdd.setDisable(false);
    }

    @FXML
    private void handleAddStudent() {
        try {
            if (!validateForm()) {
                return;
            }

            Student student = createStudentFromForm();
            Student savedStudent = studentService.saveStudent(student);

            studentList.add(savedStudent);
            clearForm();
            showAlert("Success", "Student added successfully!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Error", "Failed to add student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleUpdateStudent() {
        try {
            if (!validateForm()) {
                return;
            }

            Long studentId = Long.parseLong(txtId.getText());
            Student studentDetails = createStudentFromForm();

            Student updatedStudent = studentService.updateStudent(studentId, studentDetails);

            // Update the table
            int selectedIndex = studentTable.getSelectionModel().getSelectedIndex();
            studentList.set(selectedIndex, updatedStudent);

            showAlert("Success", "Student updated successfully!", Alert.AlertType.INFORMATION);

        } catch (Exception e) {
            showAlert("Error", "Failed to update student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleDeleteStudent() {
        try {
            Student selectedStudent = studentTable.getSelectionModel().getSelectedItem();
            if (selectedStudent == null) {
                showAlert("Warning", "Please select a student to delete.", Alert.AlertType.WARNING);
                return;
            }

            // Confirmation dialog
            Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
            confirmation.setTitle("Confirm Delete");
            confirmation.setHeaderText("Delete Student");
            confirmation.setContentText("Are you sure you want to delete student: " +
                    selectedStudent.getFirstName() + " " + selectedStudent.getLastName() + "?");

            Optional<ButtonType> result = confirmation.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                studentService.deleteStudent(selectedStudent.getId());
                studentList.remove(selectedStudent);
                clearForm();
                disableUpdateDeleteButtons();
                showAlert("Success", "Student deleted successfully!", Alert.AlertType.INFORMATION);
            }

        } catch (Exception e) {
            showAlert("Error", "Failed to delete student: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleSearch() {
        String searchText = txtSearch.getText().trim();
        if (searchText.isEmpty()) {
            loadAllStudents();
            return;
        }

        try {
            List<Student> searchResults = studentService.searchStudentsByName(searchText);
            studentList.clear();
            studentList.addAll(searchResults);

            if (searchResults.isEmpty()) {
                showAlert("Info", "No students found with name: " + searchText, Alert.AlertType.INFORMATION);
            }
        } catch (Exception e) {
            showAlert("Error", "Search failed: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void handleClearForm() {
        clearForm();
        studentTable.getSelectionModel().clearSelection();
        disableUpdateDeleteButtons();
        txtSearch.clear();
        loadAllStudents();
    }

    private Student createStudentFromForm() {
        return new Student(
                txtRegistrationNumber.getText().trim(),
                txtFirstName.getText().trim(),
                txtLastName.getText().trim(),
                txtEmail.getText().trim(),
                dateDateOfBirth.getValue(),
                txtDepartment.getText().trim(),
                dateEnrollmentDate.getValue() != null ? dateEnrollmentDate.getValue() : LocalDate.now()
        );
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        if (txtRegistrationNumber.getText().trim().isEmpty()) {
            errors.append("• Registration Number is required\n");
        }
        if (txtFirstName.getText().trim().isEmpty()) {
            errors.append("• First Name is required\n");
        }
        if (txtLastName.getText().trim().isEmpty()) {
            errors.append("• Last Name is required\n");
        }
        if (dateEnrollmentDate.getValue() == null) {
            errors.append("• Enrollment Date is required\n");
        }

        if (errors.length() > 0) {
            showAlert("Validation Error",
                    "Please fix the following errors:\n\n" + errors.toString(),
                    Alert.AlertType.ERROR);
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