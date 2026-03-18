package org.example.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.example.config.SpringContextProvider;
import org.springframework.stereotype.Controller;

import java.util.Optional;

@Controller
public class DashboardController {

    @FXML
    private void handleExit() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Exit Application");
        alert.setHeaderText("Confirm Exit");
        alert.setContentText("Are you sure you want to exit the Student Records Management System?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    @FXML
    private void showStudentManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StudentManagement.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent studentManagementView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Student Management - SRMS");
            stage.setScene(new Scene(studentManagementView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Student Management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showCourseManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CourseManagement.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent courseManagementView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Course Management - SRMS");
            stage.setScene(new Scene(courseManagementView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Course Management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showEnrollmentManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EnrollmentManagement.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent enrollmentManagementView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Enrollment Management - SRMS");
            stage.setScene(new Scene(enrollmentManagementView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Enrollment Management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showGradeManagement() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/GradeManagement.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent gradeManagementView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Grade Management - SRMS");
            stage.setScene(new Scene(gradeManagementView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Grade Management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // COURSE REPORTS
    @FXML
    private void showCourseReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CourseReport.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent gradeManagementView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Grade Management - SRMS");
            stage.setScene(new Scene(gradeManagementView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(800);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Grade Management: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    // STUDENT REPORTS
    @FXML
    private void showStudentReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/StudentReport.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent studentReportView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Student Reports - SRMS");
            stage.setScene(new Scene(studentReportView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Student Reports: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Add this for debugging
        }
    }

    // TRANSCRIPT REPORTS
    @FXML
    private void showTranscriptReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/TranscriptReport.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent transcriptReportView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Transcript Reports - SRMS");
            stage.setScene(new Scene(transcriptReportView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Transcript Reports: " + e.getMessage(), Alert.AlertType.ERROR);
            e.printStackTrace(); // Add this for debugging
        }
    }

    // ENROLLMENT REPORTS - Keep for compatibility
    @FXML
    private void showEnrollmentReport() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/CourseReport.fxml"));
            loader.setControllerFactory(SpringContextProvider.getApplicationContext()::getBean);
            Parent enrollmentReportView = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Enrollment Reports - SRMS");
            stage.setScene(new Scene(enrollmentReportView, 1200, 800));
            stage.setMinWidth(1000);
            stage.setMinHeight(700);
            stage.show();

        } catch (Exception e) {
            showAlert("Error", "Failed to open Enrollment Reports: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    @FXML
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About Student Records Management System");
        alert.setHeaderText("Student Records Management System v1.0");
        alert.setContentText(
                "Developed for: Chuka University\n" +
                        "Course: ACSC 332 - Desktop Application Development\n" +
                        "Project: CAT 2 - Student Records Management System\n\n" +
                        "Technical Stack:\n" +
                        "• Java 17\n" +
                        "• JavaFX 17\n" +
                        "• Spring Boot 2.7.18\n" +
                        "• MySQL Database\n" +
                        "• Maven Build Tool\n\n" +
                        "This application demonstrates modern desktop application\ndevelopment with layered architecture (MVC pattern)."
        );
        alert.showAndWait();
    }

    private void showAlert(String title, String message, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}