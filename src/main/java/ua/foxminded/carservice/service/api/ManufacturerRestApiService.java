package ua.foxminded.carservice.service.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;

public interface ManufacturerRestApiService {
  ResponseEntity<Page<ManufacturerResponse>> getManufacturerResponsesByName(String name, Pageable pageable);

  ResponseEntity<ManufacturerResponse> getManufacturerResponseById(String id);

  ResponseEntity<String> saveManufacturer(ManufacturerCreateRequest request);

  ResponseEntity<ManufacturerResponse> modifyManufacturer(ManufacturerModifyRequest request);

  ResponseEntity<Void> deleteManufacturerById(String id);
}
