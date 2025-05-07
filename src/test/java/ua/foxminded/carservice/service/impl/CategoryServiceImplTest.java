package ua.foxminded.carservice.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import jakarta.persistence.EntityNotFoundException;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.repository.CategoryRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
  @Mock
  CategoryRepository repo;

  @InjectMocks
  CategoryServiceImpl service;

  @Test
  void getCategoriesByNameShouldCallRepoFindAllIfNameIsBlank() {
    when(repo.findAll(any(Pageable.class))).thenReturn(categories());

    service.getCategoriesByName(" ", PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findAll(any(Pageable.class));
  }

  @Test
  void getCategoriesByNameShouldCallRepoFindAllIfNameIsNull() {
    when(repo.findAll(any(Pageable.class))).thenReturn(categories());

    service.getCategoriesByName(null, PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findAll(any(Pageable.class));
  }

  @Test
  void getCategoriesByNameShouldCallRepofindByNameContainingIfNameIsValid() {
    when(repo.findByNameContaining(anyString(), any(Pageable.class))).thenReturn(categories());

    service.getCategoriesByName("name", PageRequest.of(0, 10));

    verify(repo, atLeastOnce()).findByNameContaining("name", PageRequest.of(0, 10));
  }

  @Test
  void getCategoryByIdShouldCallRepoFindByIdIfIdIsValid() {
    when(repo.findById(anyString())).thenReturn(Optional.of(category()));

    service.getCategoryById("id");

    verify(repo, atLeastOnce()).findById("id");
  }

  @Test
  void getCategoryByIdShouldThrowEntityNotFoundExceptionIfIdIsInvalid() {
    when(repo.findById(anyString())).thenReturn(Optional.empty());

    assertThrows(EntityNotFoundException.class, () -> service.getCategoryById("invalid-id"));

    verify(repo, atLeastOnce()).findById("invalid-id");
  }

  @Test
  void createCategoryShouldCallRepoSaveIfCategoryIsValid() {
    when(repo.save(any(Category.class))).thenReturn(category());

    service.createCategory(category());

    verify(repo, atLeastOnce()).save(category());
  }

  @Test
  void createCategoryShouldReturnSavedCategory() {
    when(repo.save(any(Category.class))).thenReturn(category());

    assertThat(service.createCategory(category())).isEqualTo(category());
  }

  @Test
  void modifyCategoryShouldCallRepoSaveIfCategoryIsValid() {
    when(repo.save(any(Category.class))).thenReturn(category());

    service.modifyCategory(category());

    verify(repo, atLeastOnce()).save(category());
  }

  @Test
  void modifyCategoryShouldReturnSavedCategory() {
    when(repo.save(any(Category.class))).thenReturn(category());

    assertThat(service.modifyCategory(category())).isEqualTo(category());
  }

  @Test
  void deleteCategoryShouldCallRepoDeleteByIdIfIdIsValid() {
    doNothing().when(repo).deleteById(anyString());

    service.deleteCategory("valid-id");

    verify(repo, atLeastOnce()).deleteById("valid-id");
  }

  private Category category() {
    return Category.builder()
        .id("category-id")
        .name("category-name").build();
  }

  private Page<Category> categories() {
    return new PageImpl<Category>(List.of(Category.builder()
        .id("category-id-1")
        .name("category-name-1").build(),
        Category.builder()
            .id("category-id-1")
            .name("category-name-1").build()));
  }
}
