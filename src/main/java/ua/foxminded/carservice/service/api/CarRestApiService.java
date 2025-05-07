package ua.foxminded.carservice.service.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;
import ua.foxminded.carservice.dto.CarSearchParameters;

public interface CarRestApiService {
  ResponseEntity<Page<CarResponse>> getCarResponsesByParameters(CarSearchParameters parameters, Pageable pageable);

  ResponseEntity<CarResponse> getCarResponseById(String id);

  ResponseEntity<String> saveCar(CarCreateRequest request);

  ResponseEntity<CarResponse> modifyCar(CarModifyRequest request);

  ResponseEntity<Void> deleteCarById(String id);
}
