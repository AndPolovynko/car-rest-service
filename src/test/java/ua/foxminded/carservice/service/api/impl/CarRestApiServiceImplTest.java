package ua.foxminded.carservice.service.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.mapper.CarMapper;
import ua.foxminded.carservice.service.CarService;

@ExtendWith(MockitoExtension.class)
public class CarRestApiServiceImplTest {
  @Mock
  CarMapper mapper;
  @Mock
  CarService service;

  @InjectMocks
  CarRestApiServiceImpl apiService;

  @Test
  void getCarResponsesByParametersShouldCallServiceWithExpectedArguments() {
    when(service.getCarsByParameters(any(), any(Pageable.class))).thenReturn(cars());

    CarSearchParameters params = new CarSearchParameters("Manufacturer", "Model", List.of("Category-1", "Category-2"),
        "1990", "2024");
    apiService.getCarResponsesByParameters(params, PageRequest.of(0, 10));

    verify(service, atLeastOnce()).getCarsByParameters(params, PageRequest.of(0, 10));
  }

  @Test
  void getCarResponsesByParametersShouldReturnExpectedResponseIfCarsListIsEmpty() {
    when(service.getCarsByParameters(any(), any(Pageable.class))).thenReturn(new PageImpl<Car>(new ArrayList<Car>()));

    ResponseEntity<PageImpl<Car>> expetcedResponse = ResponseEntity.ok(new PageImpl<Car>(new ArrayList<Car>()));

    CarSearchParameters params = new CarSearchParameters("Manufacturer", "Model", List.of("Category-1", "Category-2"),
        "1990", "2024");
    assertThat(apiService.getCarResponsesByParameters(params, PageRequest.of(0, 10))).isEqualTo(expetcedResponse);
  }

  @Test
  void getCarResponsesByParametersShouldCallMapperIfCarsListIsNotEmpty() {
    when(service.getCarsByParameters(any(), any(Pageable.class))).thenReturn(cars());
    when(mapper.carToCarResponse(any())).thenReturn(carResponse());

    CarSearchParameters params = new CarSearchParameters("Manufacturer", "Model", List.of("Category-1", "Category-2"),
        "1990", "2024");
    apiService.getCarResponsesByParameters(params, PageRequest.of(0, 10));

    verify(mapper, atLeastOnce()).carToCarResponse(any());
  }

  @Test
  void getCarResponsesByParametersShouldReturnExpectedResponseIfCarsListIsNotEmpty() {
    when(service.getCarsByParameters(any(), any(Pageable.class))).thenReturn(cars());
    when(mapper.carToCarResponse(any())).thenReturn(carResponse());

    ResponseEntity<Page<CarResponse>> expetcedResponse = ResponseEntity.ok(carResponses());

    CarSearchParameters params = new CarSearchParameters("Manufacturer", "Model", List.of("Category-1", "Category-2"),
        "1990", "2024");
    assertThat(apiService.getCarResponsesByParameters(params, PageRequest.of(0, 10)).getBody().getContent())
        .isEqualTo(expetcedResponse.getBody().getContent());
  }

  @Test
  void getCarResponseByIdShouldCallServiceWithExpectedParams() {
    when(service.getCarById(anyString())).thenReturn(car());

    apiService.getCarResponseById("car-id");

    verify(service, atLeastOnce()).getCarById("car-id");
  }

  @Test
  void getCarResponseByIdShouldCallMapperWithExpectedParams() {
    when(service.getCarById(anyString())).thenReturn(car());
    when(mapper.carToCarResponse(any())).thenReturn(carResponse());

    apiService.getCarResponseById("car-id");

    verify(mapper, atLeastOnce()).carToCarResponse(car());
  }

  @Test
  void getCarResponseByIdShouldReturnExpectedResponse() {
    when(service.getCarById(anyString())).thenReturn(car());
    when(mapper.carToCarResponse(any())).thenReturn(carResponse());

    ResponseEntity<CarResponse> expectedResponse = ResponseEntity.ok(carResponse());

    assertThat(apiService.getCarResponseById("car-id"))
        .isEqualTo(expectedResponse);
  }

  @Test
  void saveCarShouldCallServiceWithExpectedParams() {
    when(mapper.carCreateRequestToCar(any())).thenReturn(car());
    when(service.createCar(any(), any(), any())).thenReturn(car());

    CarCreateRequest request = carCreateRequest();
    apiService.saveCar(request);

    verify(service, atLeastOnce()).createCar(car(), request.getManufacturerName(), request.getCategoryNames());
  }

  @Test
  void saveCarShouldCallMapperWithExpectedParams() {
    when(mapper.carCreateRequestToCar(any())).thenReturn(car());
    when(service.createCar(any(), any(), any())).thenReturn(car());

    apiService.saveCar(carCreateRequest());

    verify(mapper, atLeastOnce()).carCreateRequestToCar(carCreateRequest());
  }

  @Test
  void saveCarShouldReturnExpectedResponse() {
    when(mapper.carCreateRequestToCar(any())).thenReturn(car());
    when(service.createCar(any(), any(), any())).thenReturn(car());

    ResponseEntity<String> expectedResponse = new ResponseEntity<String>(car().getId(), HttpStatus.CREATED);

    assertThat(apiService.saveCar(carCreateRequest()))
        .isEqualTo(expectedResponse);
  }

  @Test
  void modifyCarShouldCallServiceWithExpectedParams() {
    when(mapper.carModifyRequestToCar(any())).thenReturn(car());
    when(service.modifyCar(any(), any(), any())).thenReturn(car());

    CarModifyRequest request = carModifyRequest();
    apiService.modifyCar(request);

    verify(service, atLeastOnce()).modifyCar(car(), request.getManufacturerName(), request.getCategoryNames());
  }

  @Test
  void modifyCarShouldCallMapperWithExpectedParams() {
    when(mapper.carModifyRequestToCar(any())).thenReturn(car());
    when(service.modifyCar(any(), any(), any())).thenReturn(car());

    apiService.modifyCar(carModifyRequest());

    verify(mapper, atLeastOnce()).carModifyRequestToCar(carModifyRequest());
    verify(mapper, atLeastOnce()).carToCarResponse(car());
  }

  @Test
  void modifyCarShouldReturnExpectedResponse() {
    when(mapper.carModifyRequestToCar(any())).thenReturn(car());
    when(service.modifyCar(any(), any(), any())).thenReturn(car());
    when(mapper.carToCarResponse(any())).thenReturn(carResponse());

    ResponseEntity<CarResponse> expectedResponse = ResponseEntity.ok(carResponse());

    assertThat(apiService.modifyCar(carModifyRequest()))
        .isEqualTo(expectedResponse);
  }

  @Test
  void deleteCarByIdShouldCallServiceWithExpectedParams() {
    apiService.deleteCarById("car-id");

    verify(service, atLeastOnce()).deleteCar("car-id");
  }

  @Test
  void deleteCarByIdShouldReturnNoContentResponse() {
    ResponseEntity<Void> expectedResponse = ResponseEntity.noContent().build();

    assertThat(apiService.deleteCarById("car-id"))
        .isEqualTo(expectedResponse);
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

  private CarResponse carResponse() {
    return CarResponse.builder()
        .id("Id")
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category-1", "Category-2"))
        .model("Model")
        .productionYear(2000).build();
  }

  private CarCreateRequest carCreateRequest() {
    return CarCreateRequest.builder()
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category-1", "Category-2"))
        .model("Model")
        .productionYear(2000).build();
  }

  private CarModifyRequest carModifyRequest() {
    return CarModifyRequest.builder()
        .id("Id")
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category-1", "Category-2"))
        .model("Model")
        .productionYear(2000).build();
  }

  private Page<Car> cars() {
    return new PageImpl<Car>(List.of(car(), car()));
  }

  private Page<CarResponse> carResponses() {
    return new PageImpl<CarResponse>(List.of(carResponse(), carResponse()));
  }
}
