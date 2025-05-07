package ua.foxminded.carservice.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import ua.foxminded.carservice.repository.IdentifierGenerator;

@Entity
@Table(name = "categories")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Category implements StringIdentifiable {
  @Id
  @IdentifierGenerator
  @Column(name = "id")
  private String id;

  @Column(name = "name")
  @NotBlank(message = "Category name has to be specified.")
  private String name;

  @ManyToMany(mappedBy = "categories")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Car> cars;
}
