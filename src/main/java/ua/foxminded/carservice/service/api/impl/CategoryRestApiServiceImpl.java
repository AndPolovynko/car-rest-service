package ua.foxminded.carservice.service.api.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;
import ua.foxminded.carservice.mapper.CategoryMapper;
import ua.foxminded.carservice.service.CategoryService;
import ua.foxminded.carservice.service.api.CategoryRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CategoryRestApiServiceImpl implements CategoryRestApiService {
  private final CategoryService service;
  private final CategoryMapper mapper;

  @Override
  public ResponseEntity<Page<CategoryResponse>> getCategoryResponsesByName(String name, Pageable pageable) {
    Page<Category> categories = service.getCategoriesByName(name, pageable);
    return ResponseEntity.ok(categories.map(category -> mapper.categoryToCategoryResponse(category)));
  }

  @Override
  public ResponseEntity<CategoryResponse> getCategoryResponseById(String id) {
    Category category = service.getCategoryById(id);
    return ResponseEntity.ok(mapper.categoryToCategoryResponse(category));
  }

  @Override
  public ResponseEntity<String> saveCategory(CategoryCreateRequest request) {
    Category category = service.createCategory(mapper.categoryCreateRequestToCategory(request));
    return new ResponseEntity<String>(category.getId(), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<CategoryResponse> modifyCategory(CategoryModifyRequest request) {
    Category category = service.modifyCategory(mapper.categoryModifyRequestToCategory(request));
    return ResponseEntity.ok(mapper.categoryToCategoryResponse(category));
  }

  @Override
  public ResponseEntity<Void> deleteCategoryById(String id) {
    service.deleteCategory(id);
    return ResponseEntity.noContent().build();
  }
}
