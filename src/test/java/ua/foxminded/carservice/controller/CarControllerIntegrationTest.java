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
import ua.foxminded.carservice.service.api.CarRestApiService;

class CarControllerIntegrationTest {

  @Nested
  @TestPropertySource(properties = "spring.flyway.enabled=false")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class CarControllerTest {
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
          .get("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(5))
          .body("content.model", containsInAnyOrder("Zeta", "Delta", "Epsilon", "Theta", "Kappa"));
    }

    @Test
    void getRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(5))
          .body("content.model", containsInAnyOrder("Zeta", "Delta", "Epsilon", "Theta", "Kappa"));
    }

    @Test
    void postRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "manufacturerName": "Alpha Motors",
              "productionYear": 1900,
              "model": "Test",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void postRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "manufacturerName": "Alpha Motors",
              "productionYear": 1900,
              "model": "Test",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void postRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "manufacturerName": "Alpha Motors",
              "productionYear": 1900,
              "model": "Test",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars")
          .then()
          .body("content.model", hasItem("Test"));

      jdbcTemplate.update("DELETE FROM cars WHERE model = 'Test'");
    }

    @Test
    void putRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      String requestBody = """
          {
              "id": "car001",
              "manufacturerName": "Alpha Motors",
              "productionYear": 2020,
              "model": "Zeta Updated",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void putRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      String requestBody = """
          {
              "id": "car001",
              "manufacturerName": "Alpha Motors",
              "productionYear": 2020,
              "model": "Zeta Updated",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void putRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      String requestBody = """
          {
              "id": "car001",
              "manufacturerName": "Alpha Motors",
              "productionYear": 2020,
              "model": "Zeta Updated",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("model", equalTo("Zeta Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/car001")
          .then()
          .body("model", equalTo("Zeta Updated"));

      jdbcTemplate.update("UPDATE cars SET model = 'Zeta' WHERE id = 'car001'");
    }

    @Test
    void deleteRequestShouldReturnUnauthorizedStatusCodeIfUserIsNotAuthenticated() {
      given()
          .auth().oauth2("invalid-token")
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void deleteRequestShouldReturnForbiddenStatusCodeIfUserIsAuthorizedWithUserRole() {
      given()
          .auth().oauth2(obtainAccessToken("test-user", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void deleteRequestShouldReturnValidResponseIfUserIsAuthorizedWithModeratorRole() {
      jdbcTemplate.update(
          "INSERT INTO cars (id, production_year, model, manufacturer_id) VALUES ('car-delete', 2020, 'Zeta', 'm001')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void getCarsShouldReturnAllCarsWhenNoFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(5))
          .body("content.model", containsInAnyOrder("Zeta", "Delta", "Epsilon", "Theta", "Kappa"));
    }

    @Test
    void getCarsShouldReturnFilteredCarsWhenNameFilterApplied() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .param("model", "Epsilon")
          .when()
          .get("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(1))
          .body("content[0].model", equalTo("Epsilon"));
    }

    @Test
    void getCarsShouldReturnCorrectlyPaginatedResultsWhenPageableParamsProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars?page=0&size=2")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("content", hasSize(2))
          .body("page.totalPages", equalTo(3));
    }

    @Test
    void getCarByIdShouldReturnCarWhenValidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/car001")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("id", equalTo("car001"))
          .body("manufacturerName", equalTo("Alpha Motors"))
          .body("productionYear", equalTo(2020))
          .body("model", equalTo("Zeta"));
    }

    @Test
    void getCarByIdShouldReturnNotFoundWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/non-existent")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void saveCarShouldCreateNewCarWhenValidRequestProvided() {
      String requestBody = """
          {
              "manufacturerName": "Alpha Motors",
              "productionYear": 1900,
              "model": "Test",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.CREATED.value())
          .body(notNullValue());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars")
          .then()
          .body("content.model", hasItem("Test"));

      jdbcTemplate.update("DELETE FROM cars WHERE model = 'Test'");
    }

    @Test
    void saveCarShouldReturnBadRequestWhenRequestIsInvalid() {
      String requestBody = """
          {
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .post("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void modifyCarShouldUpdateCarWhenValidRequestProvided() {
      String requestBody = """
          {
              "id": "car001",
              "manufacturerName": "Alpha Motors",
              "productionYear": 2020,
              "model": "Zeta Updated",
              "categoryNames": [
                "Compact",
                "Luxury"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.OK.value())
          .body("model", equalTo("Zeta Updated"));

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/car001")
          .then()
          .body("model", equalTo("Zeta Updated"));

      jdbcTemplate.update("UPDATE cars SET model = 'Zeta' WHERE id = 'car001'");
    }

    @Test
    void modifyCarShouldReturnNotFoundWhenInvalidIdProvided() {
      String requestBody = """
          {
              "id": "non-existent",
              "manufacturerName": "Non Existent",
              "productionYear": 2020,
              "model": "Non Existent",
              "categoryNames": [
                "Non Existent"
              ]
          }
          """;

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .body(requestBody)
          .when()
          .put("/api/v1/cars")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCarShouldDeleteCarWhenValidIdProvided() {
      jdbcTemplate.update(
          "INSERT INTO cars (id, production_year, model, manufacturer_id) VALUES ('car-delete', 2020, 'Zeta', 'm001')");

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/car-delete")
          .then()
          .statusCode(HttpStatus.NOT_FOUND.value());
    }

    @Test
    void deleteCarShouldReturnNoContentWhenInvalidIdProvided() {
      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .delete("/api/v1/cars/non-existent")
          .then()
          .statusCode(HttpStatus.NO_CONTENT.value());
    }
  }

  @Nested
  @TestPropertySource(properties = "spring.flyway.enabled=false")
  @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
  class ExceptionHandlingTest {
    @LocalServerPort
    private Integer port;

    @MockitoBean
    private CarRestApiService mockService;

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
      doThrow(new DataIntegrityViolationException("DB error")).when(mockService).getCarResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/provoke-error")
          .then()
          .body("message", equalTo("Database Error"))
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenObjectOptimisticLockingFailureExceptionWithUnknownCauseIsThrown()
        throws Exception {
      doThrow(new ObjectOptimisticLockingFailureException("", null)).when(mockService).getCarResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @Test
    void anyMethodShouldReturnNotImplementedWhenNotImplementedExceptionIsThrown() throws Exception {
      doThrow(new NotImplementedException("")).when(mockService).getCarResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/provoke-error")
          .then()
          .statusCode(HttpStatus.NOT_IMPLEMENTED.value());
    }

    @Test
    void anyMethodShouldReturnInternalServerErrorWhenUnknownExceptionIsThrown() throws Exception {
      doThrow(new RuntimeException("")).when(mockService).getCarResponseById(any());

      given()
          .auth().oauth2(obtainAccessToken("test-moderator", "test"))
          .contentType(ContentType.JSON)
          .when()
          .get("/api/v1/cars/provoke-error")
          .then()
          .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
  }
}
