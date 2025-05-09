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
import ua.foxminded.carservice.dto.ErrorResponse;
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;
import ua.foxminded.carservice.service.api.ManufacturerRestApiService;

@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RestController
@RequestMapping("api/v1/manufacturers")
@Tag(name = "Manufacturer API", description = "Operations related to manufacturers.")
public class ManufacturerController {
  private final ManufacturerRestApiService service;

  @GetMapping
  @Operation(summary = "Retrieve a list of all available manufacturers with optional filtering.", description = "Fetches a list of manufacturers with optional filters such as name.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content) })
  public ResponseEntity<Page<ManufacturerResponse>> getManufacturers(
      @RequestParam(name = "name", required = false, defaultValue = "") String name,
      @PageableDefault Pageable pageable) {
    return service.getManufacturerResponsesByName(name, pageable);
  }

  @GetMapping("/{id}")
  @Operation(summary = "Find manufacturer by ID.", description = "Finds and returns a manufacturer by its ID.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "404", description = "Manufacturer with the provided ID doesn't exist.", content = @Content) })
  public ResponseEntity<ManufacturerResponse> getManufacturer(@PathVariable String id) {
    return service.getManufacturerResponseById(id);
  }

  @PostMapping
  @Operation(summary = "Save a new manufacturer to the database.", description = "Creates a new manufacturer using the provided details in the request body.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "New manufacturer was successfully created.", content = {
          @Content(mediaType = "text/plain", schema = @Schema(type = "string"))
      }),
      @ApiResponse(responseCode = "400", description = "Bad request.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not authorized to perform this action.", content = @Content) })
  public ResponseEntity<String> saveManufacturer(@Valid @RequestBody ManufacturerCreateRequest request) {
    return service.saveManufacturer(request);
  }

  @PutMapping
  @Operation(summary = "Update manufacturer details by ID.", description = "Modifies the manufacturer's information based on the provided ID and update details.")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "OK.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = PagedModel.class))
      }),
      @ApiResponse(responseCode = "400", description = "Bad request.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }),
      @ApiResponse(responseCode = "401", description = "User is not authenticated.", content = @Content),
      @ApiResponse(responseCode = "403", description = "User is not authorized to perform this action.", content = @Content),
      @ApiResponse(responseCode = "404", description = "Manufacturer with the provided ID doesn't exist.", content = {
          @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class))
      }) })
  public ResponseEntity<ManufacturerResponse> modifyManufacturer(
      @Valid @RequestBody ManufacturerModifyRequest request) {
    return service.modifyManufacturer(request);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a manufacturer by ID.", description = "Removes the manufacturer with the specified ID from the database.")
  @ApiResponse(responseCode = "204", description = "Manufacturer with the provided ID doesn't exist.", content = @Content)
  public ResponseEntity<Void> deleteManufacturer(@PathVariable String id) {
    return service.deleteManufacturerById(id);
  }
}
