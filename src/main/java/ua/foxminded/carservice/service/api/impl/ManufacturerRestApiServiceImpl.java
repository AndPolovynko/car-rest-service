package ua.foxminded.carservice.service.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;
import ua.foxminded.carservice.mapper.ManufacturerMapper;
import ua.foxminded.carservice.service.ManufacturerService;
import ua.foxminded.carservice.service.api.ManufacturerRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ManufacturerRestApiServiceImpl implements ManufacturerRestApiService {
  private final ManufacturerService service;
  private final ManufacturerMapper mapper;

  @Override
  public ResponseEntity<Page<ManufacturerResponse>> getManufacturerResponsesByName(String name, Pageable pageable) {

    Page<Manufacturer> manufacturers = service.getManufacturersByName(name, pageable);

    return ResponseEntity
        .ok(manufacturers.map(manufacturer -> mapper.manufacturerToManufacturerResponse(manufacturer)));
  }

  @Override
  public ResponseEntity<ManufacturerResponse> getManufacturerResponseById(String id) {
    Manufacturer manufacturer = service.getManufacturerById(id);
    return ResponseEntity.ok(mapper.manufacturerToManufacturerResponse(manufacturer));
  }

  @Override
  public ResponseEntity<String> saveManufacturer(ManufacturerCreateRequest request) {
    Manufacturer manufacturer = service.createManufacturer(mapper.manufacturerCreateRequestToManufacturer(request));
    return new ResponseEntity<String>(manufacturer.getId(), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<ManufacturerResponse> modifyManufacturer(ManufacturerModifyRequest request) {
    Manufacturer manufacturer = service.modifyManufacturer(mapper.manufacturerModifyRequestToManufacturer(request));
    return ResponseEntity.ok(mapper.manufacturerToManufacturerResponse(manufacturer));
  }

  @Override
  public ResponseEntity<Void> deleteManufacturerById(String id) {
    service.deleteManufacturer(id);
    return ResponseEntity.noContent().build();
  }
}
