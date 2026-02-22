package com.demo.studentmanagement.repository;

import com.demo.studentmanagement.domain.Student;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

  Optional<Student> findByEmail(String email);

  List<Student> findByDepartment(String department);

  boolean existsByEmail(String email);
}
