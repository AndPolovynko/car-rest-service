package ua.foxminded.carservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;

class CarMapperTest {
  CarMapper mapper = new CarMapperImpl();

  @Test
  void carToCarResponseShouldReturnNullIfCarIsNull() {
    Car car = null;
    assertThat(mapper.carToCarResponse(car)).isEqualTo(null);
  }

  @Test
  void carToCarResponseShouldReturnExpectedCarResponse() {
    CarResponse expectedResponse = CarResponse.builder()
        .id("id")
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category")).build();

    assertThat(mapper.carToCarResponse(getCar())).isEqualTo(expectedResponse);
  }

  @Test
  void carToCarResponseShouldReturnExpectedCarResponseIfManufacturerIsNull() {
    CarResponse expectedResponse = CarResponse.builder()
        .id("id")
        .manufacturerName(null)
        .categoryNames(List.of("Category")).build();

    Car car = getCar();
    car.setManufacturer(null);

    assertThat(mapper.carToCarResponse(car)).isEqualTo(expectedResponse);
  }

  @Test
  void carToCarResponseShouldReturnExpectedCarResponseIfCategoriesIsNull() {
    CarResponse expectedResponse = CarResponse.builder()
        .id("id")
        .manufacturerName("Manufacturer")
        .categoryNames(new ArrayList<String>()).build();

    Car car = getCar();
    car.setCategories(null);

    assertThat(mapper.carToCarResponse(car)).isEqualTo(expectedResponse);
  }

  @Test
  void carsToCarResponsesShouldReturnNullIfCarsIsNull() {
    List<Car> cars = null;
    assertThat(mapper.carsToCarResponses(cars)).isEqualTo(null);
  }

  @Test
  void carsToCarResponsesShouldReturnExpectedCarResponses() {
    List<CarResponse> expectedResponses = new ArrayList<>();
    expectedResponses.add(CarResponse.builder()
        .id("id-1")
        .categoryNames(new ArrayList<String>())
        .manufacturerName("Manufacturer-1").build());
    expectedResponses.add(CarResponse.builder()
        .id("id-2")
        .categoryNames(new ArrayList<String>())
        .manufacturerName("Manufacturer-2").build());

    assertThat(mapper.carsToCarResponses(getCars())).isEqualTo(expectedResponses);
  }

  @Test
  void carCreateRequestToCarShouldReturnNullIfCarCreateRequestIsNull() {
    CarCreateRequest request = null;
    assertThat(mapper.carCreateRequestToCar(request)).isEqualTo(null);
  }

  @Test
  void carCreateRequestToCarShouldReturnExpectedCar() {
    CarCreateRequest request = CarCreateRequest.builder()
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category")).build();

    Car expectedCar = getCar();
    expectedCar.setId(null);
    expectedCar.setManufacturer(null);
    expectedCar.setCategories(null);

    assertThat(mapper.carCreateRequestToCar(request)).isEqualTo(expectedCar);
  }

  @Test
  void carModifyRequestToCarShouldReturnNullIfCarModifyRequestToCarIsNull() {
    CarModifyRequest car = null;
    assertThat(mapper.carModifyRequestToCar(car)).isEqualTo(null);
  }

  @Test
  void carModifyRequestToCarShouldReturnExpectedCar() {
    CarModifyRequest request = CarModifyRequest.builder()
        .id("id")
        .manufacturerName("Manufacturer")
        .categoryNames(List.of("Category")).build();

    Car expectedCar = getCar();
    expectedCar.setManufacturer(null);
    expectedCar.setCategories(null);

    assertThat(mapper.carModifyRequestToCar(request)).isEqualTo(expectedCar);
  }

  private Car getCar() {
    return Car.builder()
        .id("id")
        .manufacturer(Manufacturer.builder()
            .name("Manufacturer").build())
        .categories(List.of(Category.builder()
            .name("Category").build()))
        .build();
  }

  private List<Car> getCars() {
    return List.of(Car.builder()
        .id("id-1")
        .manufacturer(Manufacturer.builder()
            .name("Manufacturer-1").build())
        .build(),
        Car.builder()
            .id("id-2")
            .manufacturer(Manufacturer.builder()
                .name("Manufacturer-2").build())
            .build());
  }
}
