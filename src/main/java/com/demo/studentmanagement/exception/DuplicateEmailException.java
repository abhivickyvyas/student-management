package com.demo.studentmanagement.exception;

public class DuplicateEmailException extends RuntimeException {

  public DuplicateEmailException(String email) {
    super("Student with email already exists: " + email);
  }
}
