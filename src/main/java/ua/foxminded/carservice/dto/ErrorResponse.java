package ua.foxminded.carservice.dto;

import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ErrorResponse {
  private Integer status;
  private String message;
  private Map<String, String> errorDetails;
}
