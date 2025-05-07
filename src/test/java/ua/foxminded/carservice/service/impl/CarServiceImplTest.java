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
import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.repository.CarRepository;
import ua.foxminded.carservice.repository.CarSpecifications;
import ua.foxminded.carservice.repository.CategoryRepository;
import ua.foxminded.carservice.repository.ManufacturerRepository;

@ExtendWith(MockitoExtension.class)
class CarServiceImplTest {
  @Mock
  CarRepository carRepo;
  @Mock
  CategoryRepository categoryRepo;
  @Mock
  ManufacturerRepository manufacturerRepo;

  @InjectMocks
  CarServiceImpl service;

  @Test
  void getCarsByParametersShouldCallRepoFindAllWithExpectedArguments() {
    when(carRepo.findAll(CarSpecifications.withFilters(any()), any(Pageable.class))).thenReturn(cars());

    CarSearchParameters params = new CarSearchParameters("Manufacturer", "Model", List.of("Category-1", "Category-2"),
        "1990", "2024");
    service.getCarsByParameters(params, PageRequest.of(0, 10));

    verify(carRepo, atLeastOnce()).findAll(CarSpecifications.withFilters(any()), any(Pageable.class));
  }

  @Test
  void getCarByIdShouldCallRepoFindByIdIfIdIsValid() {
    when(carRepo.findById(anyString())).thenReturn(Optional.of(car()));

    service.getCarById("id");

    verify(carRepo, atLeastOnce()).findById("id");
  }

  @Test
  void getCarByIdShouldThrowEntityNotFoundExceptionIfIdIsInvalid() {
    when(carRepo.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.getCarById("invalid-id"));

    verify(carRepo, atLeastOnce()).findById("invalid-id");
  }

  @Test
  void createCarShouldCallRepoSaveIfCarIsValid() {
    when(carRepo.save(any(Car.class))).thenReturn(car());
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));
    when(categoryRepo.findByName("Category-1")).thenReturn(Optional.of(Category.builder().name("Category-1").build()));
    when(categoryRepo.findByName("Category-2")).thenReturn(Optional.of(Category.builder().name("Category-2").build()));

    service.createCar(car(), "Manufacturer", List.of("Category-1", "Category-2"));

    verify(carRepo, atLeastOnce()).save(car());
  }

  @Test
  void createCarShouldReturnSavedCar() {
    when(carRepo.save(any(Car.class))).thenReturn(car());
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));
    when(categoryRepo.findByName("Category-1")).thenReturn(Optional.of(Category.builder().name("Category-1").build()));
    when(categoryRepo.findByName("Category-2")).thenReturn(Optional.of(Category.builder().name("Category-2").build()));

    assertThat(service.createCar(car(), "Manufacturer", List.of("Category-1", "Category-2"))).isEqualTo(car());
  }

  @Test
  void createCarShouldThrowEntityNotFoundExceptionIfManufacturerNameIsInvalid() {
    assertThrows(EntityNotFoundException.class,
        () -> service.createCar(car(), "Invalid", List.of("Category-1", "Category-2")));

    verify(manufacturerRepo, atLeastOnce()).findByName("Invalid");
  }

  @Test
  void createCarShouldThrowEntityNotFoundExceptionIfCategoryNameIsInvalid() {
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));

    assertThrows(EntityNotFoundException.class,
        () -> service.createCar(car(), "Manufacturer", List.of("Invalid", "Category-2")));

    verify(categoryRepo, atLeastOnce()).findByName("Invalid");
  }

  @Test
  void modifyCarShouldCallRepoSaveIfCarIsValid() {
    when(carRepo.save(any(Car.class))).thenReturn(car());
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));
    when(categoryRepo.findByName("Category-1")).thenReturn(Optional.of(Category.builder().name("Category-1").build()));
    when(categoryRepo.findByName("Category-2")).thenReturn(Optional.of(Category.builder().name("Category-2").build()));

    service.modifyCar(car(), "Manufacturer", List.of("Category-1", "Category-2"));

    verify(carRepo, atLeastOnce()).save(car());
  }

  @Test
  void modifyCarShouldReturnSavedCar() {
    when(carRepo.save(any(Car.class))).thenReturn(car());
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));
    when(categoryRepo.findByName("Category-1")).thenReturn(Optional.of(Category.builder().name("Category-1").build()));
    when(categoryRepo.findByName("Category-2")).thenReturn(Optional.of(Category.builder().name("Category-2").build()));

    assertThat(service.modifyCar(car(), "Manufacturer", List.of("Category-1", "Category-2"))).isEqualTo(car());
  }

  @Test
  void modifyCarShouldThrowEntityNotFoundExceptionIfManufacturerNameIsInvalid() {
    assertThrows(EntityNotFoundException.class,
        () -> service.modifyCar(car(), "Invalid", List.of("Category-1", "Category-2")));

    verify(manufacturerRepo, atLeastOnce()).findByName("Invalid");
  }

  @Test
  void modifyCarShouldThrowEntityNotFoundExceptionIfCategoryNameIsInvalid() {
    when(manufacturerRepo.findByName(anyString()))
        .thenReturn(Optional.of(Manufacturer.builder().name("Manufacturer").build()));

    assertThrows(EntityNotFoundException.class,
        () -> service.modifyCar(car(), "Manufacturer", List.of("Invalid", "Category-2")));

    verify(categoryRepo, atLeastOnce()).findByName("Invalid");
  }

  @Test
  void deleteCarShouldCallRepoDeleteByIdIfIdIsValid() {
    doNothing().when(carRepo).deleteById(anyString());

    service.deleteCar("valid-id");

    verify(carRepo, atLeastOnce()).deleteById("valid-id");
  }

  private Car car() {
    return Car.builder()
        .id("Id")
        .manufacturer(Manufacturer.builder()
            .name("Manufacturer").build())
        .categories(List.of(Category.builder()
            .name("Category-1").build(),
            Category.builder()
                .name("Category-2").build()))
        .model("Model")
        .productionYear(2000)
        .build();
  }

  private Page<Car> cars() {
    return new PageImpl<Car>(List.of(Car.builder()
        .id("Id-1")
        .manufacturer(Manufacturer.builder()
            .name("Manufacturer-1").build())
        .categories(List.of(Category.builder()
            .name("Category-1-1").build(),
            Category.builder()
                .name("Category-2-1").build()))
        .model("Model-1")
        .productionYear(2000)
        .build(),
        Car.builder()
            .id("Id-2")
            .manufacturer(Manufacturer.builder()
                .name("Manufacturer-2").build())
            .categories(List.of(Category.builder()
                .name("Category-1-2").build(),
                Category.builder()
                    .name("Category-2-2").build()))
            .model("Model-2")
            .productionYear(2000)
            .build()));
  }
}
