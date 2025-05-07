package ua.foxminded.carservice.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.repository.CarRepository;
import ua.foxminded.carservice.repository.CarSpecifications;
import ua.foxminded.carservice.repository.CategoryRepository;
import ua.foxminded.carservice.repository.ManufacturerRepository;
import ua.foxminded.carservice.service.CarService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CarServiceImpl implements CarService {
  private final CarRepository carRepo;
  private final CategoryRepository categoryRepo;
  private final ManufacturerRepository manufacturerRepo;

  @Override
  @Transactional
  public Page<Car> getCarsByParameters(CarSearchParameters parameters, Pageable pageable) {
    return carRepo.findAll(CarSpecifications.withFilters(parameters), pageable);
  }

  @Override
  @Transactional
  public Car getCarById(String id) {
    return carRepo.findById(id).orElseThrow(() -> new EntityNotFoundException("Car with id=" + id + " doesn't exist."));
  }

  @Override
  @Transactional
  public Car createCar(Car car, String manufacturerName, List<String> categoryNames) {
    return carRepo.save(setManufacturerAndCategories(car, manufacturerName, categoryNames));
  }

  @Override
  @Transactional
  public Car modifyCar(Car car, String manufacturerName, List<String> categoryNames) {
    return carRepo.save(setManufacturerAndCategories(car, manufacturerName, categoryNames));
  }

  @Override
  @Transactional
  public void deleteCar(String id) {
    carRepo.deleteById(id);
  }

  private Car setManufacturerAndCategories(Car car, String manufacturerName, List<String> categoryNames) {
    car.setManufacturer(manufacturerRepo.findByName(manufacturerName).orElseThrow(
        () -> new EntityNotFoundException("Manufacturer with name = " + manufacturerName + " doesn't exist.")));

    car.setCategories(new ArrayList<Category>());
    categoryNames.forEach(categoryName -> {
      car.getCategories().add(categoryRepo.findByName(categoryName)
          .orElseThrow(() -> new EntityNotFoundException("Category with name = " + categoryName + " doesn't exist.")));
    });

    return car;
  }
}
