package ua.foxminded.carservice.repository;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import ua.foxminded.carservice.domain.Car;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CarSearchParameters;

public class CarSpecifications {

  public static Specification<Car> withFilters(CarSearchParameters params) {
    return (root, query, criteriaBuilder) -> {
      List<Predicate> predicates = new ArrayList<>();

      if (params.manufacturerName() != null && !params.manufacturerName().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("manufacturer").get("name"), params.manufacturerName()));
      }
      if (params.model() != null && !params.model().isBlank()) {
        predicates.add(criteriaBuilder.equal(root.get("model"), params.model()));
      }
      if (params.minProductionYear() != null && !params.minProductionYear().isBlank()) {
        predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("productionYear"),
            Integer.valueOf(params.minProductionYear())));
      }
      if (params.maxProductionYear() != null && !params.maxProductionYear().isBlank()) {
        predicates.add(
            criteriaBuilder.lessThanOrEqualTo(root.get("productionYear"), Integer.valueOf(params.maxProductionYear())));
      }
      if (params.categoryNames() != null && !params.categoryNames().isEmpty()) {
        Join<Car, Category> categories = root.join("categories", JoinType.INNER);
        predicates.add(categories.get("name").in(params.categoryNames()));
      }

      return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };
  }
}
