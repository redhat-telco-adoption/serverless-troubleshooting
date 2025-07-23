package org.acme;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.io.File;
import java.lang.management.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

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

            systemInfo.put("nodeName", Objects.requireNonNullElse(nodeName, "Not available"));
        } else {
            systemInfo.put("kubernetesEnvironment", false);
        }

        // Add JVM information
        systemInfo.put("jvmVersion", System.getProperty("java.version"));
        systemInfo.put("jvmVendor", System.getProperty("java.vendor"));

        // Add enhanced information
        systemInfo.put("containerInfo", getContainerInfo());
        systemInfo.put("resourceLimits", getResourceLimits());
        systemInfo.put("gcInfo", getGCInfo());

        return systemInfo;
    }

    /**
     * Gets resource limits from various sources including cgroups and JVM settings
     */
    private Map<String, Object> getResourceLimits() {
        Map<String, Object> limits = new HashMap<>();

        try {
            // Memory limits
            Map<String, Object> memoryLimits = new HashMap<>();

            // Try to read cgroup v1 memory limit
            File cgroupV1MemoryLimit = new File("/sys/fs/cgroup/memory/memory.limit_in_bytes");
            if (cgroupV1MemoryLimit.exists()) {
                try {
                    String limitStr = Files.readString(cgroupV1MemoryLimit.toPath()).trim();
                    long limitBytes = Long.parseLong(limitStr);
                    // If limit is very large, it means no limit is set
                    if (limitBytes < Long.MAX_VALUE / 2) {
                        memoryLimits.put("cgroupV1Limit", formatBytes(limitBytes));
                        memoryLimits.put("cgroupV1LimitBytes", limitBytes);
                    } else {
                        memoryLimits.put("cgroupV1Limit", "unlimited");
                    }
                } catch (Exception e) {
                    memoryLimits.put("cgroupV1LimitError", e.getMessage());
                }
            }

            // Try to read cgroup v2 memory limit
            File cgroupV2MemoryMax = new File("/sys/fs/cgroup/memory.max");
            if (cgroupV2MemoryMax.exists()) {
                try {
                    String limitStr = Files.readString(cgroupV2MemoryMax.toPath()).trim();
                    if (!"max".equals(limitStr)) {
                        long limitBytes = Long.parseLong(limitStr);
                        memoryLimits.put("cgroupV2Limit", formatBytes(limitBytes));
                        memoryLimits.put("cgroupV2LimitBytes", limitBytes);
                    } else {
                        memoryLimits.put("cgroupV2Limit", "unlimited");
                    }
                } catch (Exception e) {
                    memoryLimits.put("cgroupV2LimitError", e.getMessage());
                }
            }

            // JVM memory limits
            Runtime runtime = Runtime.getRuntime();
            memoryLimits.put("jvmMaxMemory", formatBytes(runtime.maxMemory()));
            memoryLimits.put("jvmMaxMemoryBytes", runtime.maxMemory());

            limits.put("memory", memoryLimits);

            // CPU limits
            Map<String, Object> cpuLimits = new HashMap<>();

            // Try to read cgroup v1 CPU quota and period
            File cgroupV1CpuQuota = new File("/sys/fs/cgroup/cpu/cpu.cfs_quota_us");
            File cgroupV1CpuPeriod = new File("/sys/fs/cgroup/cpu/cpu.cfs_period_us");

            if (cgroupV1CpuQuota.exists() && cgroupV1CpuPeriod.exists()) {
                try {
                    long quota = Long.parseLong(Files.readString(cgroupV1CpuQuota.toPath()).trim());
                    long period = Long.parseLong(Files.readString(cgroupV1CpuPeriod.toPath()).trim());

                    if (quota > 0) {
                        double cpuLimit = (double) quota / period;
                        cpuLimits.put("cgroupV1CpuLimit", String.format("%.2f cores", cpuLimit));
                        cpuLimits.put("cgroupV1CpuQuota", quota);
                        cpuLimits.put("cgroupV1CpuPeriod", period);
                    } else {
                        cpuLimits.put("cgroupV1CpuLimit", "unlimited");
                    }
                } catch (Exception e) {
                    cpuLimits.put("cgroupV1CpuLimitError", e.getMessage());
                }
            }

            // Try to read cgroup v2 CPU max
            File cgroupV2CpuMax = new File("/sys/fs/cgroup/cpu.max");
            if (cgroupV2CpuMax.exists()) {
                try {
                    String cpuMaxStr = Files.readString(cgroupV2CpuMax.toPath()).trim();
                    if (!"max".equals(cpuMaxStr)) {
                        String[] parts = cpuMaxStr.split(" ");
                        if (parts.length == 2) {
                            long quota = Long.parseLong(parts[0]);
                            long period = Long.parseLong(parts[1]);
                            double cpuLimit = (double) quota / period;
                            cpuLimits.put("cgroupV2CpuLimit", String.format("%.2f cores", cpuLimit));
                            cpuLimits.put("cgroupV2CpuQuota", quota);
                            cpuLimits.put("cgroupV2CpuPeriod", period);
                        }
                    } else {
                        cpuLimits.put("cgroupV2CpuLimit", "unlimited");
                    }
                } catch (Exception e) {
                    cpuLimits.put("cgroupV2CpuLimitError", e.getMessage());
                }
            }

            // Available processors
            cpuLimits.put("availableProcessors", Runtime.getRuntime().availableProcessors());

            limits.put("cpu", cpuLimits);

            // Environment variables related to limits
            Map<String, String> envLimits = new HashMap<>();
            String[] limitEnvVars = {
                    "JAVA_MAX_MEM_RATIO", "JAVA_INITIAL_MEM_RATIO", "JAVA_MAX_INITIAL_MEM",
                    "CONTAINER_MAX_MEMORY", "CONTAINER_CORE_LIMIT", "JAVA_OPTS", "JAVA_OPTS_APPEND"
            };

            for (String envVar : limitEnvVars) {
                String value = System.getenv(envVar);
                if (value != null) {
                    envLimits.put(envVar, value);
                }
            }

            if (!envLimits.isEmpty()) {
                limits.put("environmentVariables", envLimits);
            }

        } catch (Exception e) {
            limits.put("error", "Failed to read resource limits: " + e.getMessage());
        }

        return limits;
    }

    /**
     * Gets garbage collection information and statistics
     */
    private Map<String, Object> getGCInfo() {
        Map<String, Object> gcInfo = new HashMap<>();

        try {
            // Get garbage collector beans
            List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();
            List<Map<String, Object>> gcCollectors = new ArrayList<>();

            long totalCollections = 0;
            long totalCollectionTime = 0;

            for (GarbageCollectorMXBean gcBean : gcBeans) {
                Map<String, Object> collector = new HashMap<>();
                collector.put("name", gcBean.getName());
                collector.put("collectionCount", gcBean.getCollectionCount());
                collector.put("collectionTime", gcBean.getCollectionTime() + "ms");
                collector.put("memoryPoolNames", gcBean.getMemoryPoolNames());
                collector.put("valid", gcBean.isValid());

                totalCollections += gcBean.getCollectionCount();
                totalCollectionTime += gcBean.getCollectionTime();

                gcCollectors.add(collector);
            }

            gcInfo.put("collectors", gcCollectors);
            gcInfo.put("totalCollections", totalCollections);
            gcInfo.put("totalCollectionTime", totalCollectionTime + "ms");

            // Get memory information
            MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

            Map<String, Object> heapMemory = new HashMap<>();
            MemoryUsage heapUsage = memoryBean.getHeapMemoryUsage();
            heapMemory.put("init", formatBytes(heapUsage.getInit()));
            heapMemory.put("used", formatBytes(heapUsage.getUsed()));
            heapMemory.put("committed", formatBytes(heapUsage.getCommitted()));
            heapMemory.put("max", formatBytes(heapUsage.getMax()));
            heapMemory.put("usedPercentage", String.format("%.1f%%",
                    (double) heapUsage.getUsed() / heapUsage.getMax() * 100));

            Map<String, Object> nonHeapMemory = new HashMap<>();
            MemoryUsage nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
            nonHeapMemory.put("init", formatBytes(nonHeapUsage.getInit()));
            nonHeapMemory.put("used", formatBytes(nonHeapUsage.getUsed()));
            nonHeapMemory.put("committed", formatBytes(nonHeapUsage.getCommitted()));
            nonHeapMemory.put("max", nonHeapUsage.getMax() == -1 ? "undefined" : formatBytes(nonHeapUsage.getMax()));

            gcInfo.put("heapMemoryUsage", heapMemory);
            gcInfo.put("nonHeapMemoryUsage", nonHeapMemory);
            gcInfo.put("objectPendingFinalizationCount", memoryBean.getObjectPendingFinalizationCount());

            // Calculate average collection time if there were collections
            if (totalCollections > 0) {
                gcInfo.put("averageCollectionTime", String.format("%.2fms", (double) totalCollectionTime / totalCollections));
            } else {
                gcInfo.put("averageCollectionTime", "N/A");
            }

            // Try to get additional GC information
            try {
                // Check if we can access com.sun.management.OperatingSystemMXBean
                if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean sunOsBean) {

                    gcInfo.put("processPhysicalMemoryUsed", formatBytes(sunOsBean.getCommittedVirtualMemorySize()));
                    gcInfo.put("processCpuTime", sunOsBean.getProcessCpuTime() / 1_000_000 + "ms");
                }
            } catch (Exception e) {
                gcInfo.put("extendedGCInfoNote", "Extended GC info not available: " + e.getMessage());
            }

        } catch (Exception e) {
            gcInfo.put("error", "Failed to read GC information: " + e.getMessage());
        }

        return gcInfo;
    }

    private Map<String, Object> getContainerInfo() {
        Map<String, Object> containerInfo = new HashMap<>();

        try {
            // Check for container limits
            File memoryLimitFile = new File("/sys/fs/cgroup/memory/memory.limit_in_bytes");
            if (memoryLimitFile.exists()) {
                // Read cgroup memory limit
                containerInfo.put("cgroupMemoryLimit",
                        Files.readString(memoryLimitFile.toPath()).trim());
            }

            File cpuLimitFile = new File("/sys/fs/cgroup/cpu/cpu.cfs_quota_us");
            if (cpuLimitFile.exists()) {
                containerInfo.put("cgroupCpuQuota",
                        Files.readString(cpuLimitFile.toPath()).trim());
            }

            // Check if running in container
            File dockerEnv = new File("/.dockerenv");
            containerInfo.put("dockerEnvironment", dockerEnv.exists());

            // Check for Kubernetes service account
            File k8sServiceAccount = new File("/var/run/secrets/kubernetes.io/serviceaccount");
            containerInfo.put("kubernetesServiceAccount", k8sServiceAccount.exists());

        } catch (Exception e) {
            containerInfo.put("error", e.getMessage());
        }

        return containerInfo;
    }

    private String formatBytes(long bytes) {
        if (bytes < 0) return "undefined";
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}