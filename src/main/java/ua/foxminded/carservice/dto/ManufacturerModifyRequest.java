package ua.foxminded.carservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ManufacturerModifyRequest {
  @NotBlank(message = "Identifier has to be specified.")
  private String id;

  @NotBlank(message = "Name has to be specified.")
  private String name;
}
