package org.example.model.entity;

import javax.persistence.*;
import java.time.LocalDateTime;
import javafx.beans.property.*;

@Entity
@Table(name = "grades")
public class Grade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @Column(name = "grade_value", nullable = false)
    private String gradeValue;

    @Column(name = "grade_date", nullable = false)
    private LocalDateTime gradeDate;

    @Column(name = "remarks")
    private String remarks;

    // JavaFX Properties for table binding
    @Transient
    private final StringProperty gradeValueProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<LocalDateTime> gradeDateProperty = new SimpleObjectProperty<>();

    @Transient
    private final StringProperty remarksProperty = new SimpleStringProperty();

    // Constructors
    public Grade() {
        this.gradeDate = LocalDateTime.now();
        initializeProperties();
    }

    public Grade(Enrollment enrollment, String gradeValue) {
        this.enrollment = enrollment;
        this.gradeValue = gradeValue;
        this.gradeDate = LocalDateTime.now();
        initializeProperties();
    }

    // Initialize properties after loading from database
    @PostLoad
    public void initializeProperties() {
        gradeValueProperty.set(gradeValue);
        gradeDateProperty.set(gradeDate);
        remarksProperty.set(remarks);
    }

    // JavaFX Property getters
    public StringProperty gradeValueProperty() {
        return gradeValueProperty;
    }

    public ObjectProperty<LocalDateTime> gradeDateProperty() {
        return gradeDateProperty;
    }

    public StringProperty remarksProperty() {
        return remarksProperty;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Enrollment getEnrollment() {
        return enrollment;
    }

    public void setEnrollment(Enrollment enrollment) {
        this.enrollment = enrollment;
    }

    public String getGradeValue() {
        return gradeValue;
    }

    public void setGradeValue(String gradeValue) {
        this.gradeValue = gradeValue;
        this.gradeValueProperty.set(gradeValue);
    }

    public LocalDateTime getGradeDate() {
        return gradeDate;
    }

    public void setGradeDate(LocalDateTime gradeDate) {
        this.gradeDate = gradeDate;
        this.gradeDateProperty.set(gradeDate);
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
        this.remarksProperty.set(remarks);
    }
}