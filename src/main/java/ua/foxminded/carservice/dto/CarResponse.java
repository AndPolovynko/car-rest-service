package ua.foxminded.carservice.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class CarResponse {
  @NotBlank(message = "Identifier has to be specified.")
  private String id;
  
  @NotBlank(message = "Manufacturer has to be specified.")
  private String manufacturerName;
  
  @Min(value = 1900, message = "Production year must be 1900 or later.")
  @NotNull(message = "Production year has to be specified.")
  private Integer productionYear;
  
  @NotBlank(message = "Model has to be specified.")
  private String model;
  
  @NotEmpty(message = "At least one category has to be specified.")
  private List<String> categoryNames;
}
