package com.demo.studentmanagement.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StudentRequest {

  private String firstName;
  private String lastName;
  private String email;
  private LocalDate dateOfBirth;
  private String department;
  private Integer enrollmentYear;
}
