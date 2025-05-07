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
import ua.foxminded.carservice.dto.ManufacturerCreateRequest;
import ua.foxminded.carservice.dto.ManufacturerModifyRequest;
import ua.foxminded.carservice.service.api.ManufacturerRestApiService;

@WebMvcTest(ManufacturerController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class ManufacturerControllerTest {
  private final static String API_VERSION = "v1";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private ManufacturerRestApiService service;
  
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
  void getManufacturersShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getManufacturerResponsesByName(any(), any(Pageable.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION + "/manufacturers?name=Manufacturer"));

    String expectedParameter = "Manufacturer";

    verify(service, atLeastOnce()).getManufacturerResponsesByName(expectedParameter, PageRequest.of(0, 10));
  }

  @Test
  void getManufacturerShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getManufacturerResponseById(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION + "/manufacturers/id"));

    verify(service, atLeastOnce()).getManufacturerResponseById("id");
  }

  @Test
  void addManufacturerShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.saveManufacturer(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(post("/api/" + API_VERSION + "/manufacturers").contentType("application/json")
        .content(getRequestBodyForCreation()));

    verify(service, atLeastOnce())
        .saveManufacturer(ManufacturerCreateRequest.builder()
            .name("Manufacturer")
            .build());
  }

  @Test
  void modifyManufacturerShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.modifyManufacturer(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(put("/api/" + API_VERSION + "/manufacturers").contentType("application/json")
        .content(getRequestBodyForModification()));

    verify(service, atLeastOnce())
        .modifyManufacturer(ManufacturerModifyRequest.builder()
            .id("Identifier")
            .name("Manufacturer")
            .build());
  }

  @Test
  void deleteManufacturerShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.deleteManufacturerById(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(delete("/api/" + API_VERSION + "/manufacturers/id"));

    verify(service, atLeastOnce()).deleteManufacturerById("id");
  }

  private String getRequestBodyForCreation() {
    return "{\n"
        + "  \"name\": \"Manufacturer\"\n"
        + "}";
  }

  private String getRequestBodyForModification() {
    return "{\n"
        + "  \"id\": \"Identifier\",\n"
        + "  \"name\": \"Manufacturer\"\n"
        + "}";
  }
}
