package ua.foxminded.carservice.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.repository.CategoryRepository;
import ua.foxminded.carservice.service.CategoryService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Service
public class CategoryServiceImpl implements CategoryService {
  private final CategoryRepository repo;

  @Override
  @Transactional
  public Page<Category> getCategoriesByName(String name, Pageable pageable) {
    if (name == null || name.isBlank()) {
      return repo.findAll(pageable);
    } else {
      return repo.findByNameContaining(name, pageable);
    }
  }

  @Override
  @Transactional
  public Category getCategoryById(String id) {
    return repo.findById(id)
        .orElseThrow(() -> new EntityNotFoundException("Category with id=" + id + " doesn't exist."));
  }

  @Override
  @Transactional
  public Category createCategory(Category category) {
    return repo.save(category);
  }

  @Override
  @Transactional
  public Category modifyCategory(Category category) {
    return repo.save(category);
  }

  @Override
  @Transactional
  public void deleteCategory(String id) {
    repo.deleteById(id);
  }
}
