package ua.foxminded.carservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.dto.CategoryResponse;
import ua.foxminded.carservice.dto.ErrorResponse;
import ua.foxminded.carservice.service.api.CategoryRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("api/v1/categories")
@Tag(name = "Category API", description = "Operations related to categories.")
public class CategoryController {
  private final CategoryRestApiService service;

  @GetMapping
  @Operation(summary = "Retrieve a list of all available categories with optional filtering.", description = "Fetches a list of categories with optional filters such as name.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content) })
  public ResponseEntity<Page<CategoryResponse>> getCategorys(
      @RequestParam(name = "name", required = false, defaultValue = "") String name,
      @PageableDefault Pageable pageable) {
    return service.getCategoryResponsesByName(name, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Find category by ID.", description = "Finds and returns a category by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "404", description = "Category with the provided ID doesn't exist.", content = @Content) })
  public ResponseEntity<CategoryResponse> getCategory(@PathVariable String id) {
    return service.getCategoryResponseById(id);
  }

  @PostMapping
  @Operation(summary = "Save a new category to the database.", description = "Creates a new category using the provided details in the request body.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "New category was successfully created.", content = {
          @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
      }),
      @ApiResponse(responseCode = "400", description = "Bad request.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not authorized to perform this action.", content = @Content) })
  public ResponseEntity<String> saveCategory(@Valid @RequestBody CategoryCreateRequest request) {
    return service.saveCategory(request);
  }

  @PutMapping
  @Operation(summary = "Update category details by ID.", description = "Modifies the category's information based on the provided ID and update details.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "400", description = "Bad request.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not authorized to perform this action.", content = @Content),
      @ApiResponse(responseCode = "404", description = "Category with the provided ID doesn't exist.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }) })
  public ResponseEntity<CategoryResponse> modifyCategory(@Valid @RequestBody CategoryModifyRequest request) {
    return service.modifyCategory(request);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a category by ID.", description = "Removes the category with the specified ID from the database.")
  @ApiResponse(responseCode = "204", description = "Category with the provided ID doesn't exist.", content = @Content)
  public ResponseEntity<Void> deleteCategory(@PathVariable String id) {
    return service.deleteCategoryById(id);
  }
}
