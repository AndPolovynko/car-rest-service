package ua.foxminded.carservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import ua.foxminded.carservice.config.SecurityConfig;
import ua.foxminded.carservice.dto.CarCreateRequest;
import ua.foxminded.carservice.dto.CarModifyRequest;
import ua.foxminded.carservice.dto.CarSearchParameters;
import ua.foxminded.carservice.service.api.CarRestApiService;

@WebMvcTest(CarController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class CarControllerTest {
  private final static String API_VERSION = "v1";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CarRestApiService service;

  @BeforeEach
  void mockJwtAuthentication() {
    Jwt jwt = Jwt.withTokenValue("mock-token")
        .header("alg", "none")
        .claim("preferred_username", "testuser")
        .claim("realm_access", Map.of("roles", List.of("MODERATOR")))
        .build();

    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(new JwtAuthenticationToken(jwt, AuthorityUtils.createAuthorityList("ROLE_MODERATOR")));
    SecurityContextHolder.setContext(context);
  }

  @Test
  void getCarsShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getCarResponsesByParameters(any(), any(Pageable.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION
        + "/cars?manufacturer_name=Manufacturer&model=Model&category_names=Category1,Category2&min_year=2020"));

    CarSearchParameters expectedParameters = new CarSearchParameters("Manufacturer", "Model",
        List.of("Category1", "Category2"), "2020", "");

    verify(service, atLeastOnce()).getCarResponsesByParameters(expectedParameters, PageRequest.of(0, 10));
  }

  @Test
  void getCarShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getCarResponseById(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION + "/cars/id"));

    verify(service, atLeastOnce()).getCarResponseById("id");
  }

  @Test
  void saveCarShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.saveCar(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(
        post("/api/" + API_VERSION + "/cars").contentType("application/json").content(getRequestBodyForCreation()));

    verify(service, atLeastOnce()).saveCar(CarCreateRequest.builder()
        .manufacturerName("Manufacturer")
        .productionYear(2020)
        .model("Model")
        .categoryNames(List.of("Category1", "Category2"))
        .build());
  }

  @Test
  void modifyCarShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.modifyCar(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(
        put("/api/" + API_VERSION + "/cars").contentType("application/json").content(getRequestBodyForModification()));

    verify(service, atLeastOnce()).modifyCar(CarModifyRequest.builder()
        .id("Identifier")
        .manufacturerName("Manufacturer")
        .productionYear(2020)
        .model("Model")
        .categoryNames(List.of("Category1", "Category2"))
        .build());
  }

  @Test
  void deleteCarShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.deleteCarById(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(delete("/api/" + API_VERSION + "/cars/id"));

    verify(service, atLeastOnce()).deleteCarById("id");
  }

  private String getRequestBodyForCreation() {
    return "{\n"
        + "  \"manufacturerName\": \"Manufacturer\",\n"
        + "  \"productionYear\": \"2020\",\n"
        + "  \"model\": \"Model\",\n"
        + "  \"categoryNames\": [\n"
        + "    \"Category1\",\n"
        + "    \"Category2\"\n"
        + "  ]\n"
        + "}";
  }

  private String getRequestBodyForModification() {
    return "{\n"
        + "  \"id\": \"Identifier\",\n"
        + "  \"manufacturerName\": \"Manufacturer\",\n"
        + "  \"productionYear\": \"2020\",\n"
        + "  \"model\": \"Model\",\n"
        + "  \"categoryNames\": [\n"
        + "    \"Category1\",\n"
        + "    \"Category2\"\n"
        + "  ]\n"
        + "}";
  }
}
