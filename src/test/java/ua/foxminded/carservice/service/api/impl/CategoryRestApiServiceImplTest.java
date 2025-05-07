package ua.foxminded.carservice.service.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;
import ua.foxminded.carservice.mapper.CategoryMapper;
import ua.foxminded.carservice.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryRestApiServiceImplTest {
  @Mock
  CategoryMapper mapper;
  @Mock
  CategoryService service;

  @InjectMocks
  CategoryRestApiServiceImpl apiService;

  @Test
  void getCategoryResponsesByNameShouldCallServiceWithExpectedArguments() {
    when(service.getCategoriesByName(anyString(), any(Pageable.class))).thenReturn(categories());

    apiService.getCategoryResponsesByName("name", PageRequest.of(0, 10));

    verify(service, atLeastOnce()).getCategoriesByName("name", PageRequest.of(0, 10));
  }

  @Test
  void getCategoryResponsesByNameShouldReturnExpectedResponseIfCategoriesListIsEmpty() {
    when(service.getCategoriesByName(anyString(), any(Pageable.class)))
        .thenReturn(new PageImpl<Category>(new ArrayList<Category>()));

    ResponseEntity<Page<CategoryResponse>> expetcedResponse = ResponseEntity
        .ok(new PageImpl<CategoryResponse>(List.of()));

    assertThat(apiService.getCategoryResponsesByName("name", PageRequest.of(0, 10))).isEqualTo(expetcedResponse);
  }

  @Test
  void getCategoryResponsesByNameShouldCallMapperIfCategoriesListIsNotEmpty() {
    when(service.getCategoriesByName(anyString(), any(Pageable.class))).thenReturn(categories());
    when(mapper.categoryToCategoryResponse(any())).thenReturn(categoryResponse());

    apiService.getCategoryResponsesByName("name", PageRequest.of(0, 10));

    verify(mapper, atLeastOnce()).categoryToCategoryResponse(category());
  }

  @Test
  void getCategoryResponsesByNameShouldReturnExpectedResponseIfCategoriesListIsNotEmpty() {
    when(service.getCategoriesByName(anyString(), any(Pageable.class))).thenReturn(categories());
    when(mapper.categoryToCategoryResponse(any())).thenReturn(categoryResponse());

    ResponseEntity<Page<CategoryResponse>> expetcedResponse = ResponseEntity.ok(categoryResponses());

    assertThat(apiService.getCategoryResponsesByName("name", PageRequest.of(0, 10)).getBody().getContent())
        .isEqualTo(expetcedResponse.getBody().getContent());
  }

  @Test
  void getCategoryResponseByIdShouldCallServiceWithExpectedParams() {
    when(service.getCategoryById(anyString())).thenReturn(category());

    apiService.getCategoryResponseById("category-id");

    verify(service, atLeastOnce()).getCategoryById("category-id");
  }

  @Test
  void getCategoryResponseByIdShouldCallMapperWithExpectedParams() {
    when(service.getCategoryById(anyString())).thenReturn(category());
    when(mapper.categoryToCategoryResponse(any())).thenReturn(categoryResponse());

    apiService.getCategoryResponseById("category-id");

    verify(mapper, atLeastOnce()).categoryToCategoryResponse(category());
  }

  @Test
  void getCategoryResponseByIdShouldReturnExpectedResponse() {
    when(service.getCategoryById(anyString())).thenReturn(category());
    when(mapper.categoryToCategoryResponse(any())).thenReturn(categoryResponse());

    ResponseEntity<CategoryResponse> expectedResponse = ResponseEntity.ok(categoryResponse());

    assertThat(apiService.getCategoryResponseById("category-id")).isEqualTo(expectedResponse);
  }

  @Test
  void saveCategoryShouldCallServiceWithExpectedParams() {
    when(mapper.categoryCreateRequestToCategory(any())).thenReturn(category());
    when(service.createCategory(any())).thenReturn(category());

    apiService.saveCategory(categoryCreateRequest());

    verify(service, atLeastOnce()).createCategory(category());
  }

  @Test
  void saveCategoryShouldCallMapperWithExpectedParams() {
    when(mapper.categoryCreateRequestToCategory(any())).thenReturn(category());
    when(service.createCategory(any())).thenReturn(category());

    apiService.saveCategory(categoryCreateRequest());

    verify(mapper, atLeastOnce()).categoryCreateRequestToCategory(categoryCreateRequest());
  }

  @Test
  void saveCategoryShouldReturnExpectedResponse() {
    when(mapper.categoryCreateRequestToCategory(any())).thenReturn(category());
    when(service.createCategory(any())).thenReturn(category());

    ResponseEntity<String> expectedResponse = new ResponseEntity<String>("category-id", HttpStatus.CREATED);

    assertThat(apiService.saveCategory(categoryCreateRequest())).isEqualTo(expectedResponse);
  }

  @Test
  void modifyCategoryShouldCallServiceWithExpectedParams() {
    when(mapper.categoryModifyRequestToCategory(any())).thenReturn(category());
    when(service.modifyCategory(any())).thenReturn(category());

    apiService.modifyCategory(categoryModifyRequest());

    verify(service, atLeastOnce()).modifyCategory(category());
  }

  @Test
  void modifyCategoryShouldCallMapperWithExpectedParams() {
    when(mapper.categoryModifyRequestToCategory(any())).thenReturn(category());
    when(service.modifyCategory(any())).thenReturn(category());

    apiService.modifyCategory(categoryModifyRequest());

    verify(mapper, atLeastOnce()).categoryModifyRequestToCategory(categoryModifyRequest());
    verify(mapper, atLeastOnce()).categoryToCategoryResponse(category());
  }

  @Test
  void modifyCategoryShouldReturnExpectedResponse() {
    when(mapper.categoryModifyRequestToCategory(any())).thenReturn(category());
    when(service.modifyCategory(any())).thenReturn(category());
    when(mapper.categoryToCategoryResponse(any())).thenReturn(categoryResponse());

    ResponseEntity<CategoryResponse> expectedResponse = ResponseEntity.ok(categoryResponse());

    assertThat(apiService.modifyCategory(categoryModifyRequest())).isEqualTo(expectedResponse);
  }

  @Test
  void deleteCategoryByIdShouldCallServiceWithExpectedParams() {
    apiService.deleteCategoryById("category-id");

    verify(service, atLeastOnce()).deleteCategory("category-id");
  }

  @Test
  void deleteCategoryByIdShouldReturnNoContentResponse() {
    ResponseEntity<Void> expectedResponse = ResponseEntity.noContent().build();

    assertThat(apiService.deleteCategoryById("category-id")).isEqualTo(expectedResponse);
  }

  private Category category() {
    return Category.builder()
        .id("category-id")
        .name("category-name").build();
  }

  private CategoryResponse categoryResponse() {
    return CategoryResponse.builder()
        .id("category-id")
        .name("category-name").build();
  }

  private CategoryCreateRequest categoryCreateRequest() {
    return CategoryCreateRequest.builder()
        .name("category-name").build();
  }

  private CategoryModifyRequest categoryModifyRequest() {
    return CategoryModifyRequest.builder()
        .id("category-id")
        .name("modified-category-name").build();
  }

  private Page<Category> categories() {
    return new PageImpl<Category>(List.of(category(), category()));
  }

  private Page<CategoryResponse> categoryResponses() {
    return new PageImpl<CategoryResponse>(List.of(categoryResponse(), categoryResponse()));
  }
}
