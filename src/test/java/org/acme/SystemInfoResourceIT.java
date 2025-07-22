package org.acme;

import io.quarkus.test.junit.QuarkusIntegrationTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.matchesPattern;
import static org.hamcrest.MatcherAssert.assertThat;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@QuarkusIntegrationTest
class SystemInfoResourceIT extends SystemInfoResourceTest {
    // Execute the same tests but in packaged mode.
    
    @Test
    void testSystemInfoEndpointDetails() {
        // This test verifies specific details of the system information
        // that are particularly relevant in a packaged environment
        
        // Get the response
        var response = given()
            .when().get("/system-info")
            .then()
                .statusCode(200)
                .contentType("application/json")
                .extract().response();
        
        // Verify OS information contains expected parts
        String os = response.path("os");
        assertThat(os, notNullValue());
        assertThat(os, containsString("("));
        assertThat(os, containsString(")"));
        
        // Verify CPU count is positive
        Integer cpuCount = response.path("cpuCount");
        assertThat(cpuCount, greaterThan(0));
        
        // Verify memory values are formatted correctly
        String availableMemory = response.path("availableMemory");
        assertThat(availableMemory, matchesPattern("\\d+(\\.\\d+)? [KMGTPE]B"));
        
        String totalMemory = response.path("totalMemory");
        assertThat(totalMemory, matchesPattern("\\d+(\\.\\d+)? [KMGTPE]B"));
        
        String maxMemory = response.path("maxMemory");
        assertThat(maxMemory, matchesPattern("\\d+(\\.\\d+)? [KMGTPE]B"));
        
        // Verify current date is within a reasonable range
        String currentDate = response.path("currentDate");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime dateTime = LocalDateTime.parse(currentDate, formatter);
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime fiveMinutesAgo = now.minusMinutes(5);
        LocalDateTime fiveMinutesFromNow = now.plusMinutes(5);
        
        // Check if the date is after fiveMinutesAgo
        assertThat("Date should be after 5 minutes ago", 
            dateTime.isAfter(fiveMinutesAgo), is(true));
            
        // Check if the date is before fiveMinutesFromNow
        assertThat("Date should be before 5 minutes from now", 
            dateTime.isBefore(fiveMinutesFromNow), is(true));
        
        // Verify kubernetes environment flag is a boolean
        Boolean kubernetesEnvironment = response.path("kubernetesEnvironment");
        assertThat(kubernetesEnvironment, anyOf(is(true), is(false)));
    }
}