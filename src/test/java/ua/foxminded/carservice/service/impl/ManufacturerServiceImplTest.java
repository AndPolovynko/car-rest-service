package ua.foxminded.carservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.repository.ManufacturerRepository;

@ExtendWith(MockitoExtension.class)
class ManufacturerServiceImplTest {
  @Mock
  ManufacturerRepository repo;

  @InjectMocks
  ManufacturerServiceImpl service;

  @Test
  void getManufacturersByNameShouldCallRepoFindAllIfNameIsBlank() {
    when(repo.findAll(any(Pageable.class))).thenReturn(manufacturers());

    service.getManufacturersByName(" ", PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findAll(any(Pageable.class));
  }

  @Test
  void getManufacturersByNameShouldCallRepoFindAllIfNameIsNull() {
    when(repo.findAll(any(Pageable.class))).thenReturn(manufacturers());

    service.getManufacturersByName(null, PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findAll(any(Pageable.class));
  }

  @Test
  void getManufacturersByNameShouldCallRepofindByNameContainingIfNameIsValid() {
    when(repo.findByNameContaining(anyString(), any(Pageable.class))).thenReturn(manufacturers());

    service.getManufacturersByName("name", PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findByNameContaining("name", PageRequest.of(0, 10));
  }

  @Test
  void getManufacturerByIdShouldCallRepoFindByIdIfIdIsValid() {
    when(repo.findById(anyString())).thenReturn(Optional.of(manufacturer()));

    service.getManufacturerById("id");

    verify(repo, atLeastOnce()).findById("id");
  }

  @Test
  void getManufacturerByIdShouldThrowEntityNotFoundExceptionIfIdIsInvalid() {
    when(repo.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.getManufacturerById("invalid-id"));

    verify(repo, atLeastOnce()).findById("invalid-id");
  }

  @Test
  void createManufacturerShouldCallRepoSaveIfManufacturerIsValid() {
    when(repo.save(any(Manufacturer.class))).thenReturn(manufacturer());

    service.createManufacturer(manufacturer());

    verify(repo, atLeastOnce()).save(manufacturer());
  }

  @Test
  void createManufacturerShouldReturnSavedManufacturer() {
    when(repo.save(any(Manufacturer.class))).thenReturn(manufacturer());

    assertThat(service.createManufacturer(manufacturer())).isEqualTo(manufacturer());
  }

  @Test
  void modifyManufacturerShouldCallRepoSaveIfManufacturerIsValid() {
    when(repo.save(any(Manufacturer.class))).thenReturn(manufacturer());

    service.modifyManufacturer(manufacturer());

    verify(repo, atLeastOnce()).save(manufacturer());
  }

  @Test
  void modifyManufacturerShouldReturnSavedManufacturer() {
    when(repo.save(any(Manufacturer.class))).thenReturn(manufacturer());

    assertThat(service.modifyManufacturer(manufacturer())).isEqualTo(manufacturer());
  }

  @Test
  void deleteManufacturerShouldCallRepoDeleteByIdIfIdIsValid() {
    doNothing().when(repo).deleteById(anyString());

    service.deleteManufacturer("valid-id");

    verify(repo, atLeastOnce()).deleteById("valid-id");
  }

  private Manufacturer manufacturer() {
    return Manufacturer.builder()
        .id("manufacturer-id")
        .name("manufacturer-name").build();
  }

  private Page<Manufacturer> manufacturers() {
    return new PageImpl<Manufacturer>(List.of(manufacturer(), manufacturer()));
  }
}
