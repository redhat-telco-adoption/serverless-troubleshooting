# SystemInfoResourceIT Integration Test Documentation

## Overview

This document describes the implementation of the `SystemInfoResourceIT` integration test for the system information endpoint.

## Implementation Details

The `SystemInfoResourceIT` class extends `SystemInfoResourceTest` and is annotated with `@QuarkusIntegrationTest`, which ensures that the tests run against the packaged application rather than in dev mode.

In addition to inheriting all the test methods from `SystemInfoResourceTest`, the integration test includes an additional test method `testSystemInfoEndpointDetails()` that performs more thorough validation of the system information returned by the endpoint.

### Additional Test Method

The `testSystemInfoEndpointDetails()` method verifies:

1. **OS Information**: Checks that the OS information contains expected parts (parentheses for architecture).
2. **CPU Count**: Verifies that the CPU count is a positive number.
3. **Memory Values**: Ensures that memory values (available, total, max) are formatted correctly with the expected pattern (e.g., "123.4 MB").
4. **Current Date**: Validates that the current date is within a reasonable range (5 minutes) of the current time.
5. **Kubernetes Environment**: Checks that the Kubernetes environment flag is a boolean (either true or false).

### Code Snippet

```java
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
```

## Running the Tests

The integration tests can be run using the Maven command:

```
./mvnw verify
```

Note that running integration tests requires the application to be packaged, which may involve native compilation if native mode is enabled. This can lead to environment-specific issues if the native binary is not compatible with the host operating system.

## Conclusion

The `SystemInfoResourceIT` integration test provides thorough validation of the system information endpoint in a packaged environment, ensuring that the endpoint works correctly in production-like conditions.