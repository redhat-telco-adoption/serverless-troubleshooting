package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.util.Map;

@Path("/startup")
public class StartupInfoResource {

    private static final long JVM_START_TIME = System.currentTimeMillis();

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getStartupInfo() {
        return Map.of("jvmStartTime", JVM_START_TIME, "currentTime", System.currentTimeMillis(), "uptimeMs", System.currentTimeMillis() - JVM_START_TIME, "isWarmup", System.currentTimeMillis() - JVM_START_TIME > 30000);
    }
}
