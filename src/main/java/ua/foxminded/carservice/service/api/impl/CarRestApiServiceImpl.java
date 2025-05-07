package ua.foxminded.carservice.service.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.mapper.CarMapper;
import ua.foxminded.carservice.service.CarService;
import ua.foxminded.carservice.service.api.CarRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CarRestApiServiceImpl implements CarRestApiService {
  private final CarService service;
  private final CarMapper mapper;

  @Override
  public ResponseEntity<Page<CarResponse>> getCarResponsesByParameters(CarSearchParameters parameters, Pageable pageable) {
    Page<Car> cars = service.getCarsByParameters(parameters, pageable);
    return ResponseEntity.ok(cars.map(car -> mapper.carToCarResponse(car)));
  }

  @Override
  public ResponseEntity<CarResponse> getCarResponseById(String id) {
    Car car = service.getCarById(id);
    return ResponseEntity.ok(mapper.carToCarResponse(car));
  }

  @Override
  public ResponseEntity<String> saveCar(CarCreateRequest request) {
    Car car = service.createCar(mapper.carCreateRequestToCar(request), request.getManufacturerName(),
        request.getCategoryNames());
    return new ResponseEntity<String>(car.getId(), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<CarResponse> modifyCar(CarModifyRequest request) {
    Car car = service.modifyCar(mapper.carModifyRequestToCar(request), request.getManufacturerName(),
        request.getCategoryNames());
    return ResponseEntity.ok(mapper.carToCarResponse(car));
  }

  @Override
  public ResponseEntity<Void> deleteCarById(String id) {
    service.deleteCar(id);
    return ResponseEntity.noContent().build();
  }
}
