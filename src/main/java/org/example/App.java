package org.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.net.URL;

@SpringBootApplication
public class App extends Application {

    private ConfigurableApplicationContext springContext;
    private Parent root;

    public static void main(String[] args) {
        System.out.println("🚀 Starting Student Records Management System...");
        launch(args);
    }

    @Override
    public void init() throws Exception {
        try {
            System.out.println("🔧 Initializing Spring context...");
            // Start Spring context
            springContext = SpringApplication.run(App.class);

            System.out.println("📁 Loading FXML file...");
            // Load FXML file from resources
            URL fxmlUrl = getClass().getClassLoader().getResource("view/Dashboard.fxml");

            if (fxmlUrl == null) {
                throw new RuntimeException(
                        "FXML file not found at: src/main/resources/view/Dashboard.fxml\n" +
                                "Please ensure the file exists in the resources directory."
                );
            }

            System.out.println("✅ FXML found at: " + fxmlUrl);

            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            loader.setControllerFactory(springContext::getBean);
            root = loader.load();

            System.out.println("✅ Application initialized successfully");

        } catch (Exception e) {
            System.err.println("❌ Error during initialization: " + e.getMessage());
            e.printStackTrace();

            // Show error dialog and exit
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Initialization Error");
                alert.setHeaderText("Failed to initialize application");
                alert.setContentText(
                        "Error: " + e.getMessage() + "\n\n" +
                                "Please check:\n" +
                                "1. FXML files are in src/main/resources/view/\n" +
                                "2. All dependencies are properly installed\n" +
                                "3. XAMPP MySQL is running\n" +
                                "4. Database configuration is correct"
                );
                alert.showAndWait();
                Platform.exit();
            });
            throw e;
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            primaryStage.setTitle("Student Records Management System - Chuka University");
            primaryStage.setScene(new Scene(root, 800, 500));
            primaryStage.setMinWidth(1000);
            primaryStage.setMinHeight(700);

            // Set application icon (optional)
            // primaryStage.getIcons().add(new Image("/icon.png"));

            primaryStage.show();
            System.out.println("🎯 Application started successfully");

        } catch (Exception e) {
            System.err.println("❌ Error starting application: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void stop() throws Exception {
        System.out.println("🛑 Shutting down application...");
        if (springContext != null) {
            springContext.close();
        }
        Platform.exit();
        System.out.println("✅ Application shutdown complete");
    }
}