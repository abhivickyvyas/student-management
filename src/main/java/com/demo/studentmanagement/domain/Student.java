package com.demo.studentmanagement.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(
    name = "student",
    indexes = {
      @Index(name = "idx_student_email", columnList = "email", unique = true),
      @Index(name = "idx_student_department", columnList = "department")
    })
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_name", length = 100, nullable = false)
  private String firstName;

  @Column(name = "last_name", length = 100, nullable = false)
  private String lastName;

  @Column(name = "email", length = 150, nullable = false, unique = true)
  private String email;

  @Column(name = "date_of_birth")
  private LocalDate dateOfBirth;

  @Column(name = "department", length = 100)
  private String department;

  @Column(name = "enrollment_year")
  private Integer enrollmentYear;

  @Column(name = "created_at")
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;
}
