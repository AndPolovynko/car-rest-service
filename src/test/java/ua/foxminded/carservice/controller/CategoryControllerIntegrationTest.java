package ua.foxminded.carservice.controller;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import ua.foxminded.carservice.service.api.CategoryRestApiService;

class CategoryControllerIntegrationTest {

  @Nested
  @TestPropertySource(properties = "spring.flyway.enabled=false")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class CategoryControllerTest {
    @LocalServerPort
    private Integer port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    static KeycloakContainer keycloak = new KeycloakContainer("keycloak/keycloak:26.2");

    private static final String REALM_NAME = "car-service";
    private static final String CLIENT_ID = "car-service-client";
    private static final String CLIENT_SECRET = "test-client-secret";

    @BeforeAll
    static void beforeAll() {
      postgres.withInitScript("test_db_initialization.sql").start();
      keycloak.withRealmImportFile("car-service-realm.json").start();
    }

    @AfterAll
    static void afterAll() {
      postgres.stop();
      keycloak.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);

      String issuerUri = keycloak.getAuthServerUrl() + "/realms/" + REALM_NAME;
      String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
      registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
      registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> jwkSetUri);
    }

    @BeforeEach
    void setUp() {
      RestAssured.baseURI = "http://localhost:" + port;
    }

    private String obtainAccessToken(String username, String password) {
      return RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", CLIENT_ID)
          .formParam("client_secret", CLIENT_SECRET)
          .formParam("username", username)
          .formParam("password", password)
          .when()
          .post(keycloak.getAuthServerUrl() + "/realms/" + REALM_NAME + "/protocol/openid-connect/token")
          .then()
          .statusCode(HttpStatus.OK.value())
          .extract()
          .path("access_token");
    }

    @Test
    void getRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Compact", "Luxury", "Convertible"));
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Compact", "Luxury", "Convertible"));
    }

    @Test
    void postRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "name": "Electric"
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void postRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "name": "Electric"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void postRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "name": "Electric"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .body("content.name", hasItem("Electric"));

      jdbcTemplate.update("DELETE FROM categories WHERE name = 'Electric'");
    }

    @Test
    void putRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "id": "c001",
              "name": "Compact Updated"
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void putRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "id": "c001",
              "name": "Compact Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void putRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "id": "c001",
              "name": "Compact Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("name", equalTo("Compact Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/c001")
          .then()
          .body("name", equalTo("Compact Updated"));

      jdbcTemplate.update("UPDATE categories SET name = 'Compact' WHERE id = 'c001'");
    }

    @Test
    void deleteRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void deleteRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void deleteRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      jdbcTemplate.update("INSERT INTO categories (id, name) VALUES ('c-delete', 'Delete')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getCategoriesShouldReturnAllCategoriesWhenNoFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Compact", "Luxury", "Convertible"));
    }

    @Test
    void getCategoriesShouldReturnFilteredCategoriesWhenNameFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .param("name", "Lux")
          .when()
          .get("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(1))
          .body("content[0].name", equalTo("Luxury"));
    }

    @Test
    void getCategoriesShouldReturnCorrectlyPaginatedResultsWhenPageableParamsProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories?page=0&size=2")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(2))
          .body("page.totalPages", equalTo(2));
    }

    @Test
    void getCategoryByIdShouldReturnCategoryWhenValidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/c001")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("id", equalTo("c001"))
          .body("name", equalTo("Compact"));
    }

    @Test
    void getCategoryByIdShouldReturnNotFoundWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/non-existent")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void saveCategoryShouldCreateNewCategoryWhenValidRequestProvided() {
      String requestBody = """
          {
              "name": "Electric"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories")
          .then()
          .body("content.name", hasItem("Electric"));

      jdbcTemplate.update("DELETE FROM categories WHERE name = 'Electric'");
    }

    @Test
    void saveCategoryShouldReturnBadRequestWhenNameIsEmpty() {
      String requestBody = """
          {
              "name": ""
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void saveCategoryShouldReturnConflictWhenCategoryWithProvidedNameAlreadyExists() {
      String requestBody = """
          {
              "name": "Compact"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void modifyCategoryShouldUpdateCategoryWhenValidRequestProvided() {
      String requestBody = """
          {
              "id": "c001",
              "name": "Compact Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("name", equalTo("Compact Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/c001")
          .then()
          .body("name", equalTo("Compact Updated"));

      jdbcTemplate.update("UPDATE categories SET name = 'Compact' WHERE id = 'c001'");
    }

    @Test
    void modifyCategoryShouldReturnNotFoundWhenInvalidIdProvided() {
      String requestBody = """
          {
              "id": "non-existent",
              "name": "Non Existent"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/categories")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCategoryShouldDeleteCategoryWhenValidIdProvided() {
      jdbcTemplate.update("INSERT INTO categories (id, name) VALUES ('c-delete', 'Delete')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/c-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCategoryShouldReturnNoContentWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/non-existent")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void deleteCategoryShouldReturnConflictWhenCategoryIsInUse() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/categories/c001")
          .then()
          .statusCode(HttpStatus.CONFLICT.value());
    }
  }

  @Nested
  @TestPropertySource(properties = "spring.flyway.enabled=false")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class ExceptionHandlingTest {
    @LocalServerPort
    private Integer port;

    @MockitoBean
    private CategoryRestApiService mockService;

    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");
    static KeycloakContainer keycloak = new KeycloakContainer();

    private static final String REALM_NAME = "car-service";
    private static final String CLIENT_ID = "car-service-client";
    private static final String CLIENT_SECRET = "test-client-secret";

    @BeforeAll
    static void beforeAll() {
      postgres.withInitScript("test_db_initialization.sql").start();
      keycloak.withRealmImportFile("car-service-realm.json").start();
    }

    @AfterAll
    static void afterAll() {
      postgres.stop();
      keycloak.stop();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
      registry.add("spring.datasource.url", postgres::getJdbcUrl);
      registry.add("spring.datasource.username", postgres::getUsername);
      registry.add("spring.datasource.password", postgres::getPassword);

      String issuerUri = keycloak.getAuthServerUrl() + "/realms/" + REALM_NAME;
      String jwkSetUri = issuerUri + "/protocol/openid-connect/certs";
      registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> issuerUri);
      registry.add("spring.security.oauth2.resourceserver.jwt.jwk-set-uri", () -> jwkSetUri);
    }

    @BeforeEach
    void setUp() {
      RestAssured.baseURI = "http://localhost:" + port;
    }

    private String obtainAccessToken(String username, String password) {
      return RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", CLIENT_ID)
          .formParam("client_secret", CLIENT_SECRET)
          .formParam("username", username)
          .formParam("password", password)
          .when()
          .post(keycloak.getAuthServerUrl() + "/realms/" + REALM_NAME + "/protocol/openid-connect/token")
          .then()
          .statusCode(HttpStatus.OK.value())
          .extract()
          .path("access_token");
    }

    @Test
    void anyMethodShouldReturnDatabaseErrorMessageWhenDataIntegrityViolationExceptionWithUnknownCauseIsThrown()
        throws Exception {
      doThrow(new DataIntegrityViolationException("DB error")).when(mockService).getCategoryResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/provoke-error")
          .then()
          .body("message", equalTo("Database Error"))
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenObjectOptimisticLockingFailureExceptionWithUnknownCauseIsThrown()
        throws Exception {
      doThrow(new ObjectOptimisticLockingFailureException("", null)).when(mockService).getCategoryResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnNotImplementedWhenNotImplementedExceptionIsThrown() throws Exception {
      doThrow(new NotImplementedException("")).when(mockService).getCategoryResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/provoke-error")
          .then()
          .statusCode(HttpStatus.NOT_IMPLEMENTED.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenUnknownExceptionIsThrown() throws Exception {
      doThrow(new RuntimeException("")).when(mockService).getCategoryResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/categories/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
