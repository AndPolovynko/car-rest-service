package ua.foxminded.carservice.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ua.foxminded.carservice.domain.Manufacturer;

@Repository
public interface ManufacturerRepository extends JpaRepository<Manufacturer, String> {
  Optional<Manufacturer> findByName(String name);
  
  Page<Manufacturer> findByNameContaining(String name, Pageable pageable);
}
