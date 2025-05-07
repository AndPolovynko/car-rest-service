package ua.foxminded.carservice.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.service.api.CarRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("api/v1/cars")
@Tag(name = "Car API", description = "Operations related to cars.")
public class CarController {
  private final CarRestApiService service;

  @GetMapping
  @Operation(summary = "Retrieve a list of all available cars with optional filtering.", description = "Fetches a list of cars with optional filters such as manufacturer, model, categories, and production year.")
  public ResponseEntity<Page<CarResponse>> getCars(
      @RequestParam(name = "manufacturer_name", required = false, defaultValue = "") String manufacturerName,
      @RequestParam(name = "model", required = false, defaultValue = "") String model,
      @RequestParam(name = "category_names", required = false) List<String> categoryNames,
      @RequestParam(name = "min_year", required = false, defaultValue = "") String minProductionYear,
      @RequestParam(name = "max_year", required = false, defaultValue = "") String maxProductionYear,
      @PageableDefault Pageable pageable) {

    return service.getCarResponsesByParameters(
        new CarSearchParameters(manufacturerName, model, categoryNames, minProductionYear, maxProductionYear), pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Find car by ID.", description = "Finds and returns a car by its ID.")
  public ResponseEntity<CarResponse> getCar(@PathVariable String id) {
    return service.getCarResponseById(id);
  }

  @PostMapping
  @Operation(summary = "Save a new car to the database.", description = "Creates a new car using the provided details in the request body.")
  public ResponseEntity<String> saveCar(@Valid @RequestBody CarCreateRequest request) {
    return service.saveCar(request);
  }

  @PutMapping
  @Operation(summary = "Update car details by ID.", description = "Modifies the car's information based on the provided ID and update details.")
  public ResponseEntity<CarResponse> modifyCar(@Valid @RequestBody CarModifyRequest request) {
    return service.modifyCar(request);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a car by ID.", description = "Removes the car with the specified ID from the database.")
  public ResponseEntity<Void> deleteCar(@PathVariable String id) {
    return service.deleteCarById(id);
  }
}
