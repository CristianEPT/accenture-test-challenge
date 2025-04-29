package com.accenture.test.accenturetestchallenge.application.exception;

import java.time.ZonedDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@Setter
@ToString
@AllArgsConstructor
@Builder
public class ApiError {

  private ZonedDateTime timeStamp;
  private HttpStatus status;
  private String error;
  private String message;
}
