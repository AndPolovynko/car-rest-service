package ua.foxminded.carservice.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ua.foxminded.carservice.repository.IdentifierGenerator;

@Entity
@Table(name = "cars")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Car implements StringIdentifiable {
  @Id
  @IdentifierGenerator
  @Column(name = "id")
  private String id;

  @ManyToOne
  @JoinColumn(name = "manufacturer_id", referencedColumnName = "id")
  @NotNull(message = "Manufacturer has to be specified.")
  private Manufacturer manufacturer;

  @Column(name = "production_year")
  @NotNull(message = "Production year has to be specified.")
  @Min(value = 1900, message = "Production year must be 1900 or later.")
  private Integer productionYear;

  @Column(name = "model")
  @NotBlank(message = "Model has to be specified.")
  private String model;

  @ManyToMany(fetch = FetchType.EAGER)
  @JoinTable(
      name = "cars_categories",
      joinColumns = @JoinColumn(name = "car_id"),
      inverseJoinColumns = @JoinColumn(name = "category_id"))
  @NotNull(message = "At least one category has to be specified.")
  private List<Category> categories;
}
