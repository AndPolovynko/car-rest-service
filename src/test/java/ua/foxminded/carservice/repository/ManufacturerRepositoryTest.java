package ua.foxminded.carservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import ua.foxminded.carservice.domain.Manufacturer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class ManufacturerRepositoryTest {
  @Autowired
  private ManufacturerRepository repo;

  @Test
  void findByNameShouldReturnOptionalOfExpectedEntityIfValidNameProvided() {
    Manufacturer expectedEntity = Manufacturer.builder()
        .id("m001")
        .name("Alpha Motors").build();

    assertThat(repo.findByName("Alpha Motors")).isEqualTo(Optional.of(expectedEntity));
  }

  @Test
  void findByNameShouldReturnOptionalEmptyIfInvalidNameProvided() {
    assertThat(repo.findByName("Invalid-Name")).isEqualTo(Optional.empty());
  }

  @Test
  void findByNameContainingShouldReturnExpectedResultIfValidArgumentsProvided() {
    List<Manufacturer> manufacturers = List.of(Manufacturer.builder()
        .id("m001")
        .name("Alpha Motors").build(),
        Manufacturer.builder()
            .id("m002")
            .name("Beta Cars").build());
    Page<Manufacturer> expectedResult = new PageImpl<Manufacturer>(manufacturers);

    assertThat(repo.findByNameContaining("rs", PageRequest.of(0, 10)).getContent())
        .isEqualTo(expectedResult.getContent());
  }
  
  @Test
  void findByNameContainingShouldReturnEmptyPageIfInvalidArgumentsProvided() {
    assertThat(repo.findByNameContaining("Invalid-Name", PageRequest.of(0, 10)).getContent())
        .isEqualTo(Page.empty().getContent());
  }
}
