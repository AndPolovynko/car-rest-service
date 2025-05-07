package ua.foxminded.carservice.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import ua.foxminded.carservice.domain.Manufacturer;
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.dto.ManufacturerResponse;

public class ManufacturerMapperTest {
  ManufacturerMapper mapper = new ManufacturerMapperImpl();

  @Test
  void manufacturerToManufacturerResponseShouldReturnNullIfManufacturerIsNull() {
    Manufacturer manufacturer = null;
    assertThat(mapper.manufacturerToManufacturerResponse(manufacturer)).isEqualTo(null);
  }

  @Test
  void manufacturerToManufacturerResponseShouldReturnExpectedManufacturerResponse() {
    ManufacturerResponse expectedResponse = ManufacturerResponse.builder()
        .id("id")
        .name("name").build();

    assertThat(mapper.manufacturerToManufacturerResponse(getManufacturer())).isEqualTo(expectedResponse);
  }

  @Test
  void manufacturersToManufacturerResponsesShouldReturnNullIfManufacturersIsNull() {
    List<Manufacturer> manufacturers = null;
    assertThat(mapper.manufacturersToManufacturerResponses(manufacturers)).isEqualTo(null);
  }

  @Test
  void manufacturersToManufacturerResponsesShouldReturnExpectedManufacturerResponses() {
    List<ManufacturerResponse> expectedResponses = new ArrayList<>();
    expectedResponses.add(ManufacturerResponse.builder()
        .id("id-1")
        .name("name-1").build());
    expectedResponses.add(ManufacturerResponse.builder()
        .id("id-2")
        .name("name-2").build());

    assertThat(mapper.manufacturersToManufacturerResponses(getManufacturers())).isEqualTo(expectedResponses);
  }

  @Test
  void manufacturerCreateRequestToManufacturerShouldReturnNullIfManufacturerCreateRequestIsNull() {
    ManufacturerCreateRequest request = null;
    assertThat(mapper.manufacturerCreateRequestToManufacturer(request)).isEqualTo(null);
  }

  @Test
  void manufacturerCreateRequestToManufacturerShouldReturnExpectedManufacturer() {
    ManufacturerCreateRequest request = ManufacturerCreateRequest.builder()
        .name("name").build();

    Manufacturer expectedManufacturer = getManufacturer();
    expectedManufacturer.setId(null);

    assertThat(mapper.manufacturerCreateRequestToManufacturer(request)).isEqualTo(expectedManufacturer);
  }

  @Test
  void manufacturerModifyRequestToManufacturerShouldReturnNullIfManufacturerModifyRequestToManufacturerIsNull() {
    ManufacturerModifyRequest manufacturer = null;
    assertThat(mapper.manufacturerModifyRequestToManufacturer(manufacturer)).isEqualTo(null);
  }

  @Test
  void manufacturerModifyRequestToManufacturerShouldReturnExpectedManufacturer() {
    ManufacturerModifyRequest request = ManufacturerModifyRequest.builder()
        .id("id")
        .name("name").build();

    Manufacturer expectedManufacturer = getManufacturer();

    assertThat(mapper.manufacturerModifyRequestToManufacturer(request)).isEqualTo(expectedManufacturer);
  }

  private Manufacturer getManufacturer() {
    return Manufacturer.builder()
        .id("id")
        .name("name").build();
  }

  private List<Manufacturer> getManufacturers() {
    return List.of(Manufacturer.builder()
        .id("id-1")
        .name("name-1").build(),
        Manufacturer.builder()
            .id("id-2")
            .name("name-2").build());
  }
}
