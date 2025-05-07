package ua.foxminded.carservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;

@Mapper(componentModel = "spring")
public interface ManufacturerMapper {
  ManufacturerResponse manufacturerToManufacturerResponse(Manufacturer manufacturer);
  
  List<ManufacturerResponse> manufacturersToManufacturerResponses(List<Manufacturer> manufacturer);
 
  Manufacturer manufacturerCreateRequestToManufacturer(ManufacturerCreateRequest request);
  
  Manufacturer manufacturerModifyRequestToManufacturer(ManufacturerModifyRequest request);
}
