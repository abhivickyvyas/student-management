package com.demo.studentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudentResponse {

  private Long id;
  private String firstName;
  private String lastName;
  private String email;
  private LocalDate dateOfBirth;
  private String department;
  private Integer enrollmentYear;
  private Instant createdAt;
  private Instant updatedAt;
}
