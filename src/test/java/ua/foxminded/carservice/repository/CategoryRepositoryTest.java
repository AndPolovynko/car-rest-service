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

import ua.foxminded.carservice.domain.Category;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class CategoryRepositoryTest {
  @Autowired
  private CategoryRepository repo;

  @Test
  void findByNameShouldReturnOptionalOfExpectedEntityIfValidNameProvided() {
    Category expectedEntity = Category.builder()
        .id("c001")
        .name("Compact").build();

    assertThat(repo.findByName("Compact")).isEqualTo(Optional.of(expectedEntity));
  }

  @Test
  void findByNameShouldReturnOptionalEmptyIfInvalidNameProvided() {
    assertThat(repo.findByName("Invalid-Name")).isEqualTo(Optional.empty());
  }

  @Test
  void findByNameContainingShouldReturnExpectedResultIfValidArgumentsProvided() {
    List<Category> categories = List.of(Category.builder()
        .id("c001")
        .name("Compact").build(),
        Category.builder()
            .id("c003")
            .name("Convertible").build());
    Page<Category> expectedResult = new PageImpl<Category>(categories);

    assertThat(repo.findByNameContaining("Co", PageRequest.of(0, 10)).getContent())
        .isEqualTo(expectedResult.getContent());
  }
  
  @Test
  void findByNameContainingShouldReturnEmptyPageIfInvalidArgumentsProvided() {
    assertThat(repo.findByNameContaining("Invalid-Name", PageRequest.of(0, 10)).getContent())
        .isEqualTo(Page.empty().getContent());
  }
}
