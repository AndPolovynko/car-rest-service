package ua.foxminded.carservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;

class CategoryMapperTest {
  CategoryMapper mapper = new CategoryMapperImpl();

  @Test
  void categoryToCategoryResponseShouldReturnNullIfCategoryIsNull() {
    Category category = null;
    assertThat(mapper.categoryToCategoryResponse(category)).isEqualTo(null);
  }

  @Test
  void categoryToCategoryResponseShouldReturnExpectedCategoryResponse() {
    CategoryResponse expectedResponse = CategoryResponse.builder()
        .id("id")
        .name("name").build();

    assertThat(mapper.categoryToCategoryResponse(getCategory())).isEqualTo(expectedResponse);
  }

  @Test
  void categoriesToCategoryResponsesShouldReturnNullIfCategoriesIsNull() {
    List<Category> categories = null;
    assertThat(mapper.categoriesToCategoryResponses(categories)).isEqualTo(null);
  }

  @Test
  void categoriesToCategoryResponsesShouldReturnExpectedCategoryResponses() {
    List<CategoryResponse> expectedResponses = new ArrayList<>();
    expectedResponses.add(CategoryResponse.builder()
        .id("id-1")
        .name("name-1").build());
    expectedResponses.add(CategoryResponse.builder()
        .id("id-2")
        .name("name-2").build());

    assertThat(mapper.categoriesToCategoryResponses(getCategories())).isEqualTo(expectedResponses);
  }

  @Test
  void categoryCreateRequestToCategoryShouldReturnNullIfCategoryCreateRequestIsNull() {
    CategoryCreateRequest request = null;
    assertThat(mapper.categoryCreateRequestToCategory(request)).isEqualTo(null);
  }

  @Test
  void categoryCreateRequestToCategoryShouldReturnExpectedCategory() {
    CategoryCreateRequest request = CategoryCreateRequest.builder()
        .name("name").build();

    Category expectedCategory = getCategory();
    expectedCategory.setId(null);

    assertThat(mapper.categoryCreateRequestToCategory(request)).isEqualTo(expectedCategory);
  }

  @Test
  void categoryModifyRequestToCategoryShouldReturnNullIfCategoryModifyRequestToCategoryIsNull() {
    CategoryModifyRequest category = null;
    assertThat(mapper.categoryModifyRequestToCategory(category)).isEqualTo(null);
  }

  @Test
  void categoryModifyRequestToCategoryShouldReturnExpectedCategory() {
    CategoryModifyRequest request = CategoryModifyRequest.builder()
        .id("id")
        .name("name").build();

    Category expectedCategory = getCategory();

    assertThat(mapper.categoryModifyRequestToCategory(request)).isEqualTo(expectedCategory);
  }

  private Category getCategory() {
    return Category.builder()
        .id("id")
        .name("name").build();
  }

  private List<Category> getCategories() {
    return List.of(Category.builder()
        .id("id-1")
        .name("name-1").build(),
        Category.builder()
            .id("id-2")
            .name("name-2").build());
  }
}
