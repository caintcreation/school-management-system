package org.example.model.entity;

import javax.persistence.*;
import java.time.LocalDate;
import javafx.beans.property.*;

@Entity
@Table(name = "enrollments")
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.EAGER) // Changed from LAZY to EAGER
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @Column(name = "score")
    private Double score;

    @Column(name = "grade")
    private String grade;

    @Column(name = "created_at")
    private LocalDate createdAt;

    @Column(name = "updated_at")
    private LocalDate updatedAt;

    // JavaFX Properties for table binding
    @Transient
    private final ObjectProperty<LocalDate> enrollmentDateProperty = new SimpleObjectProperty<>();

    @Transient
    private final DoubleProperty scoreProperty = new SimpleDoubleProperty();

    @Transient
    private final StringProperty gradeProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<LocalDate> createdAtProperty = new SimpleObjectProperty<>();

    @Transient
    private final ObjectProperty<LocalDate> updatedAtProperty = new SimpleObjectProperty<>();

    @Transient
    private final StringProperty studentNameProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty courseInfoProperty = new SimpleStringProperty();

    // Constructors
    public Enrollment() {
        this.createdAt = LocalDate.now();
        this.updatedAt = LocalDate.now();
        initializeProperties();
    }

    public Enrollment(Student student, Course course, LocalDate enrollmentDate) {
        this();
        this.student = student;
        this.course = course;
        this.enrollmentDate = enrollmentDate;
        initializeProperties();
    }

    // Initialize properties after loading from database
    @PostLoad
    public void initializeProperties() {
        enrollmentDateProperty.set(enrollmentDate);
        scoreProperty.set(score != null ? score : 0.0);
        gradeProperty.set(grade);
        createdAtProperty.set(createdAt);
        updatedAtProperty.set(updatedAt);
        studentNameProperty.set(getStudentInfo());
        courseInfoProperty.set(getCourseInfo());
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDate.now();
        this.updatedAtProperty.set(updatedAt);
    }

    // JavaFX Property getters
    public ObjectProperty<LocalDate> enrollmentDateProperty() {
        return enrollmentDateProperty;
    }

    public DoubleProperty scoreProperty() {
        return scoreProperty;
    }

    public StringProperty gradeProperty() {
        return gradeProperty;
    }

    public ObjectProperty<LocalDate> createdAtProperty() {
        return createdAtProperty;
    }

    public ObjectProperty<LocalDate> updatedAtProperty() {
        return updatedAtProperty;
    }

    public StringProperty studentNameProperty() {
        return studentNameProperty;
    }

    public StringProperty courseInfoProperty() {
        return courseInfoProperty;
    }

    // Method to calculate grade based on score
    public String calculateGrade() {
        if (this.score == null) {
            return null;
        }

        if (score >= 90) return "A";
        else if (score >= 80) return "B";
        else if (score >= 70) return "C";
        else if (score >= 60) return "D";
        else return "F";
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
        this.studentNameProperty.set(getStudentInfo());
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
        this.courseInfoProperty.set(getCourseInfo());
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
        this.enrollmentDateProperty.set(enrollmentDate);
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
        this.scoreProperty.set(score != null ? score : 0.0);

        // Auto-calculate grade when score is set
        if (score != null) {
            this.grade = calculateGrade();
            this.gradeProperty.set(grade);
        }
    }

    public String getGrade() {
        return grade;
    }

    public void setGrade(String grade) {
        this.grade = grade;
        this.gradeProperty.set(grade);
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
        this.createdAtProperty.set(createdAt);
    }

    public LocalDate getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDate updatedAt) {
        this.updatedAt = updatedAt;
        this.updatedAtProperty.set(updatedAt);
    }

    // Utility methods
    public String getStudentInfo() {
        return student != null ?
                student.getFirstName() + " " + student.getLastName() + " (" + student.getRegistrationNumber() + ")" : "";
    }

    public String getCourseInfo() {
        return course != null ?
                course.getCourseCode() + " - " + course.getCourseTitle() : "";
    }

    @Override
    public String toString() {
        return "Enrollment{" +
                "id=" + id +
                ", student=" + getStudentInfo() +
                ", course=" + getCourseInfo() +
                ", enrollmentDate=" + enrollmentDate +
                ", score=" + score +
                ", grade='" + grade + '\'' +
                '}';
    }
}