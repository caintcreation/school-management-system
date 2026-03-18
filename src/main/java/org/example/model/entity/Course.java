package org.example.model.entity;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

@Entity
@Table(name = "courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "course_code", unique = true, nullable = false)
    private String courseCode;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(name = "credits", nullable = false)
    private int credits;

    @Column(name = "course_description", length = 1000)
    private String courseDescription;

    @Column(name = "prerequisites")
    private String prerequisites;

    @OneToMany(mappedBy = "course", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();

    // JavaFX Properties for table binding
    @Transient
    private final StringProperty courseCodeProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty courseTitleProperty = new SimpleStringProperty();

    @Transient
    private final IntegerProperty creditsProperty = new SimpleIntegerProperty();

    @Transient
    private final StringProperty courseDescriptionProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty prerequisitesProperty = new SimpleStringProperty();

    // Constructors
    public Course() {
        initializeProperties();
    }

    public Course(String courseCode, String courseTitle, int credits,
                  String courseDescription, String prerequisites) {
        this.courseCode = courseCode;
        this.courseTitle = courseTitle;
        this.credits = credits;
        this.courseDescription = courseDescription;
        this.prerequisites = prerequisites;
        initializeProperties();
    }

    // Initialize properties after loading from database
    @PostLoad
    public void initializeProperties() {
        courseCodeProperty.set(courseCode);
        courseTitleProperty.set(courseTitle);
        creditsProperty.set(credits);
        courseDescriptionProperty.set(courseDescription);
        prerequisitesProperty.set(prerequisites);
    }

    // JavaFX Property getters
    public StringProperty courseCodeProperty() {
        return courseCodeProperty;
    }

    public StringProperty courseTitleProperty() {
        return courseTitleProperty;
    }

    public IntegerProperty creditsProperty() {
        return creditsProperty;
    }

    public StringProperty courseDescriptionProperty() {
        return courseDescriptionProperty;
    }

    public StringProperty prerequisitesProperty() {
        return prerequisitesProperty;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
        this.courseCodeProperty.set(courseCode);
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
        this.courseTitleProperty.set(courseTitle);
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
        this.creditsProperty.set(credits);
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
        this.courseDescriptionProperty.set(courseDescription);
    }

    public String getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(String prerequisites) {
        this.prerequisites = prerequisites;
        this.prerequisitesProperty.set(prerequisites);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public String toString() {
        return courseCode + " - " + courseTitle;
    }
}