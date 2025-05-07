package ua.foxminded.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ManufacturerCreateRequest {
  @NotBlank(message = "Name has to be specified.")
  private String name;
}
