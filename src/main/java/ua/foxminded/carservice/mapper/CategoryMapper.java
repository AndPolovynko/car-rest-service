package ua.foxminded.carservice.mapper;

import java.util.List;

import org.mapstruct.Mapper;

import ua.foxminded.carservice.domain.Category;
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
  CategoryResponse categoryToCategoryResponse(Category category);
  
  List<CategoryResponse> categoriesToCategoryResponses(List<Category> category);
 
  Category categoryCreateRequestToCategory(CategoryCreateRequest request);
  
  Category categoryModifyRequestToCategory(CategoryModifyRequest request);
}
