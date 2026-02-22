package com.demo.studentmanagement.controller;

import com.demo.studentmanagement.dto.StudentRequest;
import com.demo.studentmanagement.dto.StudentResponse;
import com.demo.studentmanagement.service.StudentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

  private final StudentService studentService;

  @PostMapping
  public ResponseEntity<StudentResponse> createStudent(@RequestBody StudentRequest request) {
    log.info("Creating student: {} {}", request.getFirstName(), request.getLastName());
    StudentResponse response = studentService.createStudent(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<StudentResponse> getStudent(@PathVariable Long id) {
    log.debug("Fetching student with id: {}", id);
    StudentResponse response = studentService.getStudent(id);
    return ResponseEntity.ok(response);
  }

  @GetMapping
  public ResponseEntity<List<StudentResponse>> getAllStudents(
      @RequestParam(required = false) String department) {
    if (department != null && !department.isBlank()) {
      log.debug("Fetching students by department: {}", department);
      return ResponseEntity.ok(studentService.getStudentsByDepartment(department));
    }
    log.debug("Fetching all students");
    return ResponseEntity.ok(studentService.getAllStudents());
  }

  @PutMapping("/{id}")
  public ResponseEntity<StudentResponse> updateStudent(
      @PathVariable Long id, @RequestBody StudentRequest request) {
    log.info("Updating student with id: {}", id);
    StudentResponse response = studentService.updateStudent(id, request);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<Void> deleteStudent(@PathVariable Long id) {
    log.info("Deleting student with id: {}", id);
    studentService.deleteStudent(id);
    return ResponseEntity.noContent().build();
  }
}
