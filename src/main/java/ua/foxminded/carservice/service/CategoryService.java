package ua.foxminded.carservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import ua.foxminded.carservice.domain.Category;

public interface CategoryService {
  Page<Category> getCategoriesByName(String name, Pageable pageable);

  Category getCategoryById(String id);

  Category createCategory(Category category);

  Category modifyCategory(Category category);

  void deleteCategory(String id);
}
