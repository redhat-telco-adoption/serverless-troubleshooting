package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

@Path("/system-info")
public class SystemInfoResource {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // Get OS information
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        systemInfo.put("os", osBean.getName() + " " + osBean.getVersion() + " (" + osBean.getArch() + ")");
        
        // Get available memory
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long maxMemory = runtime.maxMemory();
        
        systemInfo.put("availableMemory", formatBytes(freeMemory));
        systemInfo.put("totalMemory", formatBytes(totalMemory));
        systemInfo.put("maxMemory", formatBytes(maxMemory));
        
        // Get CPU count
        systemInfo.put("cpuCount", osBean.getAvailableProcessors());
        
        // Get current date
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        systemInfo.put("currentDate", now.format(formatter));
        
        // Check if running in Kubernetes environment
        String podName = System.getenv("HOSTNAME");
        String nodeName = System.getenv("NODE_NAME");
        
        if (podName != null) {
            systemInfo.put("kubernetesEnvironment", true);
            systemInfo.put("podName", podName);
            
            if (nodeName != null) {
                systemInfo.put("nodeName", nodeName);
            } else {
                systemInfo.put("nodeName", "Not available");
            }
        } else {
            systemInfo.put("kubernetesEnvironment", false);
        }
        
        return systemInfo;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp-1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}