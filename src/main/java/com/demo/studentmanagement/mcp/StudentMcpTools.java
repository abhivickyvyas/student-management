package com.demo.studentmanagement.mcp;

import com.demo.studentmanagement.dto.StudentRequest;
import com.demo.studentmanagement.dto.StudentResponse;
import com.demo.studentmanagement.service.StudentService;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class StudentMcpTools {

  private final StudentService studentService;

  @Tool(description = "Create a new student record with the given details")
  public StudentResponse create_student(
      @ToolParam(description = "Student's first name") String firstName,
      @ToolParam(description = "Student's last name") String lastName,
      @ToolParam(description = "Student's email address") String email,
      @ToolParam(description = "Student's date of birth in YYYY-MM-DD format", required = false)
          String dateOfBirth,
      @ToolParam(description = "Department the student belongs to", required = false)
          String department,
      @ToolParam(description = "Year the student enrolled", required = false)
          Integer enrollmentYear) {

    StudentRequest request =
        StudentRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
            .department(department)
            .enrollmentYear(enrollmentYear)
            .build();

    return studentService.createStudent(request);
  }

  @Tool(description = "Get a student by their ID")
  public StudentResponse get_student(
      @ToolParam(description = "The student's unique ID") Long id) {
    return studentService.getStudent(id);
  }

  @Tool(description = "Get a list of all students")
  public List<StudentResponse> get_all_students() {
    return studentService.getAllStudents();
  }

  @Tool(description = "Get all students in a specific department")
  public List<StudentResponse> get_students_by_department(
      @ToolParam(description = "The department name to filter by") String department) {
    return studentService.getStudentsByDepartment(department);
  }

  @Tool(description = "Update an existing student's details. Only provided fields will be updated.")
  public StudentResponse update_student(
      @ToolParam(description = "The student's unique ID") Long id,
      @ToolParam(description = "New first name", required = false) String firstName,
      @ToolParam(description = "New last name", required = false) String lastName,
      @ToolParam(description = "New email address", required = false) String email,
      @ToolParam(description = "New date of birth in YYYY-MM-DD format", required = false)
          String dateOfBirth,
      @ToolParam(description = "New department", required = false) String department,
      @ToolParam(description = "New enrollment year", required = false) Integer enrollmentYear) {

    StudentRequest request =
        StudentRequest.builder()
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .dateOfBirth(dateOfBirth != null ? LocalDate.parse(dateOfBirth) : null)
            .department(department)
            .enrollmentYear(enrollmentYear)
            .build();

    return studentService.updateStudent(id, request);
  }

  @Tool(description = "Delete a student by their ID")
  public String delete_student(
      @ToolParam(description = "The student's unique ID") Long id) {
    studentService.deleteStudent(id);
    return "Student with id " + id + " has been deleted successfully.";
  }
}
