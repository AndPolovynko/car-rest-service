package ua.foxminded.carservice.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarResponse;

@Mapper(componentModel = "spring")
public interface CarMapper {
  @Mapping(target = "manufacturerName", source = "manufacturer", qualifiedByName = "manufacturerToString")
  @Mapping(target = "categoryNames", source = "categories", qualifiedByName = "categoriesToStrings")
  CarResponse carToCarResponse(Car car);

  List<CarResponse> carsToCarResponses(List<Car> car);

  @Mapping(target = "manufacturer", ignore = true)
  @Mapping(target = "categories", ignore = true)
  Car carCreateRequestToCar(CarCreateRequest request);

  @Mapping(target = "manufacturer", ignore = true)
  @Mapping(target = "categories", ignore = true)
  Car carModifyRequestToCar(CarModifyRequest request);

  @Named("manufacturerToString")
  default String manufacturerToString(Manufacturer manufacturer) {
    return manufacturer != null ? manufacturer.getName() : null;
  }

  @Named("categoriesToStrings")
  default List<String> categoriesToStrings(List<Category> categories) {
    return categories != null ? categories.stream()
        .map(Category::getName)
        .collect(Collectors.toList()) : new ArrayList<String>();
  }
}
