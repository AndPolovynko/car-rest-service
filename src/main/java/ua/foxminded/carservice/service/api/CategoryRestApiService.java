package ua.foxminded.carservice.service.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;

public interface CategoryRestApiService {
  ResponseEntity<Page<CategoryResponse>> getCategoryResponsesByName(String name, Pageable pageable);

  ResponseEntity<CategoryResponse> getCategoryResponseById(String id);

  ResponseEntity<String> saveCategory(CategoryCreateRequest request);

  ResponseEntity<CategoryResponse> modifyCategory(CategoryModifyRequest request);

  ResponseEntity<Void> deleteCategoryById(String id);
}
