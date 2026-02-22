package com.demo.studentmanagement.exception;

public class StudentNotFoundException extends RuntimeException {

  private final Long studentId;

  public StudentNotFoundException(Long studentId) {
    super("Student not found with id: " + studentId);
    this.studentId = studentId;
  }

  public Long getStudentId() {
    return studentId;
  }
}
