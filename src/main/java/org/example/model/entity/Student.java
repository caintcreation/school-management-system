package org.example.model.entity;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registration_number", unique = true, nullable = false)
    private String registrationNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "date_of_birth")
    private LocalDate dateOfBirth;

    @Column(name = "department")
    private String department;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;

    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Enrollment> enrollments = new ArrayList<>();

    // JavaFX Properties for table binding
    @Transient
    private final StringProperty registrationNumberProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty firstNameProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty lastNameProperty = new SimpleStringProperty();

    @Transient
    private final StringProperty emailProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<LocalDate> dateOfBirthProperty = new SimpleObjectProperty<>();

    @Transient
    private final StringProperty departmentProperty = new SimpleStringProperty();

    @Transient
    private final ObjectProperty<LocalDate> enrollmentDateProperty = new SimpleObjectProperty<>();

    // Constructors
    public Student() {
        initializeProperties();
    }

    public Student(String registrationNumber, String firstName, String lastName,
                   String email, LocalDate dateOfBirth, String department, LocalDate enrollmentDate) {
        this.registrationNumber = registrationNumber;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.department = department;
        this.enrollmentDate = enrollmentDate;
        initializeProperties();
    }

    // Initialize properties after loading from database
    @PostLoad
    public void initializeProperties() {
        registrationNumberProperty.set(registrationNumber);
        firstNameProperty.set(firstName);
        lastNameProperty.set(lastName);
        emailProperty.set(email);
        dateOfBirthProperty.set(dateOfBirth);
        departmentProperty.set(department);
        enrollmentDateProperty.set(enrollmentDate);
    }

    // JavaFX Property getters
    public StringProperty registrationNumberProperty() {
        return registrationNumberProperty;
    }

    public StringProperty firstNameProperty() {
        return firstNameProperty;
    }

    public StringProperty lastNameProperty() {
        return lastNameProperty;
    }

    public StringProperty emailProperty() {
        return emailProperty;
    }

    public ObjectProperty<LocalDate> dateOfBirthProperty() {
        return dateOfBirthProperty;
    }

    public StringProperty departmentProperty() {
        return departmentProperty;
    }

    public ObjectProperty<LocalDate> enrollmentDateProperty() {
        return enrollmentDateProperty;
    }

    // Regular getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
        this.registrationNumberProperty.set(registrationNumber);
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.firstNameProperty.set(firstName);
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.lastNameProperty.set(lastName);
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
        this.emailProperty.set(email);
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
        this.dateOfBirthProperty.set(dateOfBirth);
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
        this.departmentProperty.set(department);
    }

    public LocalDate getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(LocalDate enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
        this.enrollmentDateProperty.set(enrollmentDate);
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public void setEnrollments(List<Enrollment> enrollments) {
        this.enrollments = enrollments;
    }

    @Override
    public String toString() {
        return firstName + " " + lastName + " (" + registrationNumber + ")";
    }
}