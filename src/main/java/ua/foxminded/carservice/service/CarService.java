package ua.foxminded.carservice.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.dto.CarSearchParameters;

public interface CarService {
  Page<Car> getCarsByParameters(CarSearchParameters parameters, Pageable pageable);

  Car getCarById(String id);

  Car createCar(Car car, String manufacturerName, List<String> categoryNames);

  Car modifyCar(Car car, String manufacturerName, List<String> categoryNames);

  void deleteCar(String id);
}
