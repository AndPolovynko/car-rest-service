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
import ua.foxminded.carservice.service.api.ManufacturerRestApiService;

class ManufacturerControllerIntegrationTest {

  @Nested
  @TestPropertySource(properties = "spring.flyway.enabled=false")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class ManufacturerControllerTest {
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
          .get("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Alpha Motors", "Beta Cars", "Gamma Auto"));
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Alpha Motors", "Beta Cars", "Gamma Auto"));
    }

    @Test
    void postRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "name": "Electric Motors"
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void postRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "name": "Electric Motors"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void postRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "name": "Electric Motors"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .body("content.name", hasItem("Electric Motors"));

      jdbcTemplate.update("DELETE FROM manufacturers WHERE name = 'Electric Motors'");
    }

    @Test
    void putRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "id": "m001",
              "name": "Alpha Motors Updated"
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void putRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "id": "m001",
              "name": "Alpha Motors Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void putRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "id": "m001",
              "name": "Alpha Motors Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("name", equalTo("Alpha Motors Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/m001")
          .then()
          .body("name", equalTo("Alpha Motors Updated"));

      jdbcTemplate.update("UPDATE manufacturers SET name = 'Alpha Motors' WHERE id = 'm001'");
    }

    @Test
    void deleteRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void deleteRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void deleteRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      jdbcTemplate.update("INSERT INTO manufacturers (id, name) VALUES ('m-delete', 'Delete')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getManufacturersShouldReturnAllManufacturersWhenNoFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(3))
          .body("content.name", containsInAnyOrder("Alpha Motors", "Beta Cars", "Gamma Auto"));
    }

    @Test
    void getManufacturersShouldReturnFilteredManufacturersWhenNameFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .param("name", "Alpha")
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(1))
          .body("content[0].name", equalTo("Alpha Motors"));
    }

    @Test
    void getManufacturersShouldReturnCorrectlyPaginatedResultsWhenPageableParamsProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers?page=0&size=2")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(2))
          .body("page.totalPages", equalTo(2));
    }

    @Test
    void getManufacturerByIdShouldReturnManufacturerWhenValidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/m001")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("id", equalTo("m001"))
          .body("name", equalTo("Alpha Motors"));
    }

    @Test
    void getManufacturerByIdShouldReturnNotFoundWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/non-existent")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void saveManufacturerShouldCreateNewManufacturerWhenValidRequestProvided() {
      String requestBody = """
          {
              "name": "Electric Motors"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers")
          .then()
          .body("content.name", hasItem("Electric Motors"));

      jdbcTemplate.update("DELETE FROM manufacturers WHERE name = 'Electric Motors'");
    }

    @Test
    void saveManufacturerShouldReturnBadRequestWhenNameIsEmpty() {
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
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void saveManufacturerShouldReturnConflictWhenManufacturerWithProvidedNameAlreadyExists() {
      String requestBody = """
          {
              "name": "Alpha Motors"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.CONFLICT.value());
    }

    @Test
    void modifyManufacturerShouldUpdateManufacturerWhenValidRequestProvided() {
      String requestBody = """
          {
              "id": "m001",
              "name": "Alpha Motors Updated"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("name", equalTo("Alpha Motors Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/m001")
          .then()
          .body("name", equalTo("Alpha Motors Updated"));

      jdbcTemplate.update("UPDATE manufacturers SET name = 'Alpha Motors' WHERE id = 'm001'");
    }

    @Test
    void modifyManufacturerShouldReturnNotFoundWhenInvalidIdProvided() {
      String requestBody = """
          {
              "id": "non-existent",
              "name": "Non Existent"
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/manufacturers")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteManufacturerShouldDeleteManufacturerWhenValidIdProvided() {
      jdbcTemplate.update("INSERT INTO manufacturers (id, name) VALUES ('m-delete', 'Delete')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/m-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteManufacturerShouldReturnNoContentWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/non-existent")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    void deleteManufacturerShouldReturnConflictWhenManufacturerIsInUse() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/manufacturers/m001")
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
    private ManufacturerRestApiService mockService;

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
      doThrow(new DataIntegrityViolationException("DB error")).when(mockService).getManufacturerResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/provoke-error")
          .then()
          .body("message", equalTo("Database Error"))
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenObjectOptimisticLockingFailureExceptionWithUnknownCauseIsThrown()
        throws Exception {
      doThrow(new ObjectOptimisticLockingFailureException("", null)).when(mockService)
          .getManufacturerResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnNotImplementedWhenNotImplementedExceptionIsThrown() throws Exception {
      doThrow(new NotImplementedException("")).when(mockService).getManufacturerResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/provoke-error")
          .then()
          .statusCode(HttpStatus.NOT_IMPLEMENTED.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenUnknownExceptionIsThrown() throws Exception {
      doThrow(new RuntimeException("")).when(mockService).getManufacturerResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/manufacturers/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
