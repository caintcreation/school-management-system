package org.example.service;

import org.example.model.entity.Student;
import org.example.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StudentService {

    private final StudentRepository studentRepository;

    @Autowired
    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    /**
     * Save a student with validation
     */
    public Student saveStudent(Student student) {
        // Validation logic as per project requirements
        if (student.getRegistrationNumber() == null || student.getRegistrationNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Registration number is required");
        }
        if (student.getFirstName() == null || student.getFirstName().trim().isEmpty()) {
            throw new IllegalArgumentException("First name is required");
        }
        if (student.getLastName() == null || student.getLastName().trim().isEmpty()) {
            throw new IllegalArgumentException("Last name is required");
        }
        if (student.getEnrollmentDate() == null) {
            student.setEnrollmentDate(LocalDate.now());
        }

        // Check for duplicate registration number
        Student existingStudent = studentRepository.findByRegistrationNumber(student.getRegistrationNumber());
        if (existingStudent != null && !existingStudent.getId().equals(student.getId())) {
            throw new IllegalArgumentException("Student with registration number " + student.getRegistrationNumber() + " already exists");
        }

        return studentRepository.save(student);
    }

    /**
     * Get all students
     */
    @Transactional(readOnly = true)
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    /**
     * Get student by ID
     */
    @Transactional(readOnly = true)
    public Optional<Student> getStudentById(Long id) {
        return studentRepository.findById(id);
    }

    /**
     * Get student by registration number
     */
    @Transactional(readOnly = true)
    public Student getStudentByRegistrationNumber(String registrationNumber) {
        return studentRepository.findByRegistrationNumber(registrationNumber);
    }

    /**
     * Update student information
     */
    public Student updateStudent(Long id, Student studentDetails) {
        Student student = studentRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Student not found with id: " + id));

        // Update fields
        student.setFirstName(studentDetails.getFirstName());
        student.setLastName(studentDetails.getLastName());
        student.setEmail(studentDetails.getEmail());
        student.setDateOfBirth(studentDetails.getDateOfBirth());
        student.setDepartment(studentDetails.getDepartment());

        // Only update registration number if it's different
        if (!student.getRegistrationNumber().equals(studentDetails.getRegistrationNumber())) {
            // Check if new registration number already exists
            Student existing = studentRepository.findByRegistrationNumber(studentDetails.getRegistrationNumber());
            if (existing != null) {
                throw new IllegalArgumentException("Registration number already exists: " + studentDetails.getRegistrationNumber());
            }
            student.setRegistrationNumber(studentDetails.getRegistrationNumber());
        }

        return studentRepository.save(student);
    }

    /**
     * Delete student by ID
     */
    public void deleteStudent(Long id) {
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found with id: " + id);
        }
        studentRepository.deleteById(id);
    }

    /**
     * Search students by name
     */
    @Transactional(readOnly = true)
    public List<Student> searchStudentsByName(String name) {
        return studentRepository.findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(name, name);
    }

    /**
     * Get students by department
     */
    @Transactional(readOnly = true)
    public List<Student> getStudentsByDepartment(String department) {
        return studentRepository.findByDepartment(department);
    }

    /**
     * Check if registration number exists
     */
    @Transactional(readOnly = true)
    public boolean registrationNumberExists(String registrationNumber) {
        return studentRepository.findByRegistrationNumber(registrationNumber) != null;
    }
}