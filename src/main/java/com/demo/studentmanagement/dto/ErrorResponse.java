package com.demo.studentmanagement.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

  private int status;
  private String error;
  private String message;
  private String path;
  private Instant timestamp;

  public static ErrorResponse of(int status, String error, String message, String path) {
    return ErrorResponse.builder()
        .status(status)
        .error(error)
        .message(message)
        .path(path)
        .timestamp(Instant.now())
        .build();
  }
}
