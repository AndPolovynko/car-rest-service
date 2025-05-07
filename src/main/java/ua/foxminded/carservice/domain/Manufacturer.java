package ua.foxminded.carservice.domain;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
@Table(name = "manufacturers")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class Manufacturer implements StringIdentifiable {
  @Id
  @IdentifierGenerator
  @Column(name = "id")
  private String id;
  
  @Column(name = "name")
  @NotBlank(message = "Manufacturer name has to be specified.")
  private String name;
  
  @OneToMany(mappedBy = "manufacturer")
  @ToString.Exclude
  @EqualsAndHashCode.Exclude
  private List<Car> cars;
}
