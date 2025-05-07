package ua.foxminded.carservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ua.foxminded.carservice.domain.Manufacturer;

public interface ManufacturerService {
  Page<Manufacturer> getManufacturersByName(String name, Pageable pageable);
  
  Manufacturer getManufacturerById(String id);
  
  Manufacturer createManufacturer(Manufacturer manufacturer);
  
  Manufacturer modifyManufacturer(Manufacturer manufacturer);
  
  void deleteManufacturer(String id);
}
