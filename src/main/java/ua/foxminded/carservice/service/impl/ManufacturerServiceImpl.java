package ua.foxminded.carservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.repository.ManufacturerRepository;
import ua.foxminded.carservice.service.ManufacturerService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class ManufacturerServiceImpl implements ManufacturerService {
  private final ManufacturerRepository repo;

  @Override
  @Transactional
  public Page<Manufacturer> getManufacturersByName(String name, Pageable pageable) {
    if (name == null || name.isBlank()) {
      return repo.findAll(pageable);
    } else {
      return repo.findByNameContaining(name, pageable);
    }
  }

  @Override
  @Transactional
  public Manufacturer getManufacturerById(String id) {
    return repo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Manufacturer with id=" + id + " doesn't exist."));
  }

  @Override
  @Transactional
  public Manufacturer createManufacturer(Manufacturer manufacturer) {
    return repo.save(manufacturer);
  }

  @Override
  @Transactional
  public Manufacturer modifyManufacturer(Manufacturer manufacturer) {
    return repo.save(manufacturer);
  }

  @Override
  @Transactional
  public void deleteManufacturer(String id) {
    repo.deleteById(id);
  }
}
