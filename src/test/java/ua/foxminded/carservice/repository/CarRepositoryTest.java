package ua.foxminded.carservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.CarSearchParameters;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class CarRepositoryTest {
  @Autowired
  private CarRepository repo;

  @Test
  void findAllShouldReturnExpectedResultsIfCarSearchParametersProvided() {
    CarSearchParameters params = new CarSearchParameters("Alpha Motors", "", null, "", "");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .extracting(Car::getModel)
        .containsExactlyInAnyOrder("Zeta", "Theta");

    assertThat(result)
        .flatExtracting(Car::getCategories)
        .extracting(Category::getName)
        .contains("Compact", "Luxury");

    assertThat(result)
        .extracting(c -> c.getManufacturer().getName())
        .containsOnly("Alpha Motors");
  }
  
  @Test
  void findAllShouldReturnAllCarsIfCarSearchParametersNotProvided() {
    CarSearchParameters params = new CarSearchParameters("", "", null, "", "");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .extracting(Car::getId)
        .containsExactlyInAnyOrder("car001", "car002", "car003", "car004", "car005");
  }
  
  @Test
  void findAllShouldReturnCarsByManufacturerIfProvided() {
    CarSearchParameters params = new CarSearchParameters("Alpha Motors", "", null, "", "");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .extracting(Car::getManufacturer)
        .extracting(Manufacturer::getName)
        .containsOnly("Alpha Motors");
  }

  @Test
  void findAllShouldReturnCarsByModelIfProvided() {
    CarSearchParameters params = new CarSearchParameters("", "Zeta", null, "", "");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .extracting(Car::getModel)
        .containsOnly("Zeta");
  }

  @Test
  void findAllShouldReturnCarsByProductionYearRangeIfProvided() {
    CarSearchParameters params = new CarSearchParameters("", "", null, "2020", "2022");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .extracting(Car::getProductionYear)
        .containsOnly(2020, 2021, 2022);
  }

  @Test
  void findAllShouldReturnCarsByCategoryIfProvided() {
    CarSearchParameters params = new CarSearchParameters("", "", List.of("Compact"), "", "");

    List<Car> result = repo.findAll(CarSpecifications.withFilters(params), PageRequest.of(0, 10)).getContent();

    assertThat(result)
        .flatExtracting(Car::getCategories)
        .extracting(Category::getName)
        .contains("Compact");
  }
}
