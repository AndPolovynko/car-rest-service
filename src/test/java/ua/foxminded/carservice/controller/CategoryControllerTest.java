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
import ua.foxminded.carservice.dto.CategoryCreateRequest;
import ua.foxminded.carservice.dto.CategoryModifyRequest;
import ua.foxminded.carservice.service.api.CategoryRestApiService;

@WebMvcTest(CategoryController.class)
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
public class CategoryControllerTest {
  private final static String API_VERSION = "v1";

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private CategoryRestApiService service;
  
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
  void getCategorysShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getCategoryResponsesByName(any(), any(Pageable.class)))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION + "/categories?name=Category"));

    String expectedParameter = "Category";

    verify(service, atLeastOnce()).getCategoryResponsesByName(expectedParameter, PageRequest.of(0, 10));
  }

  @Test
  void getCategoryShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.getCategoryResponseById(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(get("/api/" + API_VERSION + "/categories/id"));

    verify(service, atLeastOnce()).getCategoryResponseById("id");
  }

  @Test
  void addCategoryShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.saveCategory(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(post("/api/" + API_VERSION + "/categories").contentType("application/json")
        .content(getRequestBodyForCreation()));

    verify(service, atLeastOnce()).saveCategory(CategoryCreateRequest.builder()
        .name("Category")
        .build());
  }

  @Test
  void modifyCategoryShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.modifyCategory(any()))
        .thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(put("/api/" + API_VERSION + "/categories").contentType("application/json")
        .content(getRequestBodyForModification()));

    verify(service, atLeastOnce()).modifyCategory(CategoryModifyRequest.builder()
        .id("Identifier")
        .name("Category")
        .build());
  }

  @Test
  void deleteCategoryShouldCallServiceWithExpectedAttributes() throws Exception {
    when(service.deleteCategoryById(any())).thenReturn(new ResponseEntity<>(HttpStatus.OK));

    mockMvc.perform(delete("/api/" + API_VERSION + "/categories/id"));

    verify(service, atLeastOnce()).deleteCategoryById("id");
  }

  private String getRequestBodyForCreation() {
    return "{\n"
        + "  \"name\": \"Category\"\n"
        + "}";
  }

  private String getRequestBodyForModification() {
    return "{\n"
        + "  \"id\": \"Identifier\",\n"
        + "  \"name\": \"Category\"\n"
        + "}";
  }
}
