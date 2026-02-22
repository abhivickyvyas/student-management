package com.demo.studentmanagement.service;

import com.demo.studentmanagement.domain.Student;
import com.demo.studentmanagement.dto.StudentRequest;
import com.demo.studentmanagement.dto.StudentResponse;
import com.demo.studentmanagement.exception.DuplicateEmailException;
import com.demo.studentmanagement.exception.StudentNotFoundException;
import com.demo.studentmanagement.repository.StudentRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class StudentService {

  private final StudentRepository studentRepository;

  @Transactional
  public StudentResponse createStudent(StudentRequest request) {
    if (request.getFirstName() == null || request.getFirstName().isBlank()) {
      throw new IllegalArgumentException("First name is required");
    }
    if (request.getLastName() == null || request.getLastName().isBlank()) {
      throw new IllegalArgumentException("Last name is required");
    }
    if (request.getEmail() == null || request.getEmail().isBlank()) {
      throw new IllegalArgumentException("Email is required");
    }

    if (studentRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateEmailException(request.getEmail());
    }

    Student student =
        Student.builder()
            .firstName(request.getFirstName())
            .lastName(request.getLastName())
            .email(request.getEmail())
            .dateOfBirth(request.getDateOfBirth())
            .department(request.getDepartment())
            .enrollmentYear(request.getEnrollmentYear())
            .createdAt(Instant.now())
            .updatedAt(Instant.now())
            .build();

    student = studentRepository.save(student);
    log.info("Created student with id: {}", student.getId());

    return mapToResponse(student);
  }

  @Transactional(readOnly = true)
  public StudentResponse getStudent(Long id) {
    Student student =
        studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    return mapToResponse(student);
  }

  @Transactional(readOnly = true)
  public List<StudentResponse> getAllStudents() {
    return studentRepository.findAll().stream().map(this::mapToResponse).toList();
  }

  @Transactional(readOnly = true)
  public List<StudentResponse> getStudentsByDepartment(String department) {
    return studentRepository.findByDepartment(department).stream()
        .map(this::mapToResponse)
        .toList();
  }

  @Transactional
  public StudentResponse updateStudent(Long id, StudentRequest request) {
    Student student =
        studentRepository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));

    if (request.getEmail() != null
        && !request.getEmail().equals(student.getEmail())
        && studentRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateEmailException(request.getEmail());
    }

    if (request.getFirstName() != null && !request.getFirstName().isBlank()) {
      student.setFirstName(request.getFirstName());
    }
    if (request.getLastName() != null && !request.getLastName().isBlank()) {
      student.setLastName(request.getLastName());
    }
    if (request.getEmail() != null && !request.getEmail().isBlank()) {
      student.setEmail(request.getEmail());
    }
    if (request.getDateOfBirth() != null) {
      student.setDateOfBirth(request.getDateOfBirth());
    }
    if (request.getDepartment() != null) {
      student.setDepartment(request.getDepartment());
    }
    if (request.getEnrollmentYear() != null) {
      student.setEnrollmentYear(request.getEnrollmentYear());
    }

    student.setUpdatedAt(Instant.now());
    student = studentRepository.save(student);
    log.info("Updated student with id: {}", student.getId());

    return mapToResponse(student);
  }

  @Transactional
  public void deleteStudent(Long id) {
    if (!studentRepository.existsById(id)) {
      throw new StudentNotFoundException(id);
    }
    studentRepository.deleteById(id);
    log.info("Deleted student with id: {}", id);
  }

  private StudentResponse mapToResponse(Student student) {
    return StudentResponse.builder()
        .id(student.getId())
        .firstName(student.getFirstName())
        .lastName(student.getLastName())
        .email(student.getEmail())
        .dateOfBirth(student.getDateOfBirth())
        .department(student.getDepartment())
        .enrollmentYear(student.getEnrollmentYear())
        .createdAt(student.getCreatedAt())
        .updatedAt(student.getUpdatedAt())
        .build();
  }
}
