package org.acme;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
class SystemInfoResourceTest {
    @Test
    void testSystemInfoEndpoint() {
        given()
          .when().get("/system-info")
          .then()
             .statusCode(200)
             .contentType("application/json")
             .body("os", notNullValue())
             .body("availableMemory", notNullValue())
             .body("totalMemory", notNullValue())
             .body("maxMemory", notNullValue())
             .body("cpuCount", notNullValue())
             .body("currentDate", notNullValue())
             .body("kubernetesEnvironment", notNullValue());
    }
}