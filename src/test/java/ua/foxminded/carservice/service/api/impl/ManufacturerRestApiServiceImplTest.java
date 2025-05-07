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

import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;
import ua.foxminded.carservice.mapper.ManufacturerMapper;
import ua.foxminded.carservice.service.ManufacturerService;

@ExtendWith(MockitoExtension.class)
class ManufacturerRestApiServiceImplTest {
  @Mock
  ManufacturerMapper mapper;
  @Mock
  ManufacturerService service;

  @InjectMocks
  ManufacturerRestApiServiceImpl apiService;

  @Test
  void getManufacturerResponsesByNameShouldCallServiceWithExpectedArguments() {
    when(service.getManufacturersByName(anyString(), any(Pageable.class))).thenReturn(manufacturers());

    apiService.getManufacturerResponsesByName("name", PageRequest.of(0, 10));

    verify(service, atLeastOnce()).getManufacturersByName("name", PageRequest.of(0, 10));
  }

  @Test
  void getManufacturerResponsesByNameShouldReturnExpectedResponseIfManufacturersListIsEmpty() {
    when(service.getManufacturersByName(anyString(), any(Pageable.class)))
        .thenReturn(new PageImpl<Manufacturer>(new ArrayList<Manufacturer>()));

    ResponseEntity<Page<ManufacturerResponse>> expetcedResponse = ResponseEntity
        .ok(new PageImpl<ManufacturerResponse>(List.of()));

    assertThat(apiService.getManufacturerResponsesByName("name", PageRequest.of(0, 10)))
        .isEqualTo(expetcedResponse);
  }

  @Test
  void getManufacturerResponsesByNameShouldCallMapperIfManufacturersListIsNotEmpty() {
    when(service.getManufacturersByName(anyString(), any(Pageable.class))).thenReturn(manufacturers());
    when(mapper.manufacturerToManufacturerResponse(any())).thenReturn(manufacturerResponse());

    apiService.getManufacturerResponsesByName("name", PageRequest.of(0, 10));

    verify(mapper, atLeastOnce()).manufacturerToManufacturerResponse(manufacturer());
  }

  @Test
  void getManufacturerResponsesByNameShouldReturnExpectedResponseIfManufacturersListIsNotEmpty() {
    when(service.getManufacturersByName(anyString(), any(Pageable.class))).thenReturn(manufacturers());
    when(mapper.manufacturerToManufacturerResponse(any())).thenReturn(manufacturerResponse());

    ResponseEntity<Page<ManufacturerResponse>> expetcedResponse = ResponseEntity.ok(manufacturerResponses());

    assertThat(apiService.getManufacturerResponsesByName("name", PageRequest.of(0, 10))).isEqualTo(expetcedResponse);
  }

  @Test
  void getManufacturerResponseByIdShouldCallServiceWithExpectedParams() {
    when(service.getManufacturerById(anyString())).thenReturn(manufacturer());

    apiService.getManufacturerResponseById("manufacturer-id");

    verify(service, atLeastOnce()).getManufacturerById("manufacturer-id");
  }

  @Test
  void getManufacturerResponseByIdShouldCallMapperWithExpectedParams() {
    when(service.getManufacturerById(anyString())).thenReturn(manufacturer());
    when(mapper.manufacturerToManufacturerResponse(any())).thenReturn(manufacturerResponse());

    apiService.getManufacturerResponseById("manufacturer-id");

    verify(mapper, atLeastOnce()).manufacturerToManufacturerResponse(manufacturer());
  }

  @Test
  void getManufacturerResponseByIdShouldReturnExpectedResponse() {
    when(service.getManufacturerById(anyString())).thenReturn(manufacturer());
    when(mapper.manufacturerToManufacturerResponse(any())).thenReturn(manufacturerResponse());

    ResponseEntity<ManufacturerResponse> expectedResponse = ResponseEntity.ok(manufacturerResponse());

    assertThat(apiService.getManufacturerResponseById("manufacturer-id"))
        .isEqualTo(expectedResponse);
  }

  @Test
  void saveManufacturerShouldCallServiceWithExpectedParams() {
    when(mapper.manufacturerCreateRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.createManufacturer(any())).thenReturn(manufacturer());

    apiService.saveManufacturer(manufacturerCreateRequest());

    verify(service, atLeastOnce()).createManufacturer(manufacturer());
  }

  @Test
  void saveManufacturerShouldCallMapperWithExpectedParams() {
    when(mapper.manufacturerCreateRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.createManufacturer(any())).thenReturn(manufacturer());

    apiService.saveManufacturer(manufacturerCreateRequest());

    verify(mapper, atLeastOnce()).manufacturerCreateRequestToManufacturer(manufacturerCreateRequest());
  }

  @Test
  void saveManufacturerShouldReturnExpectedResponse() {
    when(mapper.manufacturerCreateRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.createManufacturer(any())).thenReturn(manufacturer());

    ResponseEntity<String> expectedResponse = new ResponseEntity<String>(manufacturer().getId(), HttpStatus.CREATED);

    assertThat(apiService.saveManufacturer(manufacturerCreateRequest()))
        .isEqualTo(expectedResponse);
  }

  @Test
  void modifyManufacturerShouldCallServiceWithExpectedParams() {
    when(mapper.manufacturerModifyRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.modifyManufacturer(any())).thenReturn(manufacturer());

    apiService.modifyManufacturer(manufacturerModifyRequest());

    verify(service, atLeastOnce()).modifyManufacturer(manufacturer());
  }

  @Test
  void modifyManufacturerShouldCallMapperWithExpectedParams() {
    when(mapper.manufacturerModifyRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.modifyManufacturer(any())).thenReturn(manufacturer());

    apiService.modifyManufacturer(manufacturerModifyRequest());

    verify(mapper, atLeastOnce()).manufacturerModifyRequestToManufacturer(manufacturerModifyRequest());
    verify(mapper, atLeastOnce()).manufacturerToManufacturerResponse(manufacturer());
  }

  @Test
  void modifyManufacturerShouldReturnExpectedResponse() {
    when(mapper.manufacturerModifyRequestToManufacturer(any())).thenReturn(manufacturer());
    when(service.modifyManufacturer(any())).thenReturn(manufacturer());
    when(mapper.manufacturerToManufacturerResponse(any())).thenReturn(manufacturerResponse());

    ResponseEntity<ManufacturerResponse> expectedResponse = ResponseEntity.ok(manufacturerResponse());

    assertThat(apiService.modifyManufacturer(manufacturerModifyRequest()))
        .isEqualTo(expectedResponse);
  }

  @Test
  void deleteManufacturerByIdShouldCallServiceWithExpectedParams() {
    apiService.deleteManufacturerById("manufacturer-id");

    verify(service, atLeastOnce()).deleteManufacturer("manufacturer-id");
  }

  @Test
  void deleteManufacturerByIdShouldReturnNoContentResponse() {
    ResponseEntity<Void> expectedResponse = ResponseEntity.noContent().build();

    assertThat(apiService.deleteManufacturerById("manufacturer-id"))
        .isEqualTo(expectedResponse);
  }

  private Manufacturer manufacturer() {
    return Manufacturer.builder()
        .id("manufacturer-id")
        .name("manufacturer-name").build();
  }

  private ManufacturerResponse manufacturerResponse() {
    return ManufacturerResponse.builder()
        .id("manufacturer-id")
        .name("manufacturer-name").build();
  }

  private ManufacturerCreateRequest manufacturerCreateRequest() {
    return ManufacturerCreateRequest.builder()
        .name("manufacturer-name").build();
  }

  private ManufacturerModifyRequest manufacturerModifyRequest() {
    return ManufacturerModifyRequest.builder()
        .id("manufacturer-id")
        .name("modified-manufacturer-name").build();
  }

  private Page<Manufacturer> manufacturers() {
    return new PageImpl<Manufacturer>(List.of(manufacturer(), manufacturer()));
  }

  private Page<ManufacturerResponse> manufacturerResponses() {
    return new PageImpl<ManufacturerResponse>(List.of(manufacturerResponse(), manufacturerResponse()));
  }
}
