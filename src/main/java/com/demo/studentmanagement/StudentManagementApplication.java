package com.demo.studentmanagement;

import com.demo.studentmanagement.mcp.StudentMcpTools;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = {"com.demo.studentmanagement"})
@EnableJpaRepositories(basePackages = {"com.demo.studentmanagement"})
public class StudentManagementApplication {

  public static void main(String[] args) {
    SpringApplication.run(StudentManagementApplication.class, args);
  }

  @Bean
  public ToolCallbackProvider studentToolCallbackProvider(StudentMcpTools studentMcpTools) {
    return MethodToolCallbackProvider.builder().toolObjects(studentMcpTools).build();
  }
}
