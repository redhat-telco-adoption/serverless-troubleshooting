# System Information Endpoint

This document describes the new system information endpoint added to the Quarkus application.

## Endpoint Details

- **URL**: `/system-info`
- **Method**: GET
- **Content Type**: application/json

## Response

The endpoint returns a JSON object with the following fields:

| Field | Description |
|-------|-------------|
| os | Operating system name, version, and architecture |
| availableMemory | Available memory in a human-readable format |
| totalMemory | Total memory allocated to the JVM in a human-readable format |
| maxMemory | Maximum memory that can be allocated to the JVM in a human-readable format |
| cpuCount | Number of available processors |
| currentDate | Current date and time in the format "yyyy-MM-dd HH:mm:ss" |
| kubernetesEnvironment | Boolean indicating whether the application is running in a Kubernetes environment |
| podName | Name of the Kubernetes pod (only if running in Kubernetes) |
| nodeName | Name of the Kubernetes node (only if running in Kubernetes and NODE_NAME environment variable is set) |

## Example Response

```json
{
  "os": "Linux 5.15.0-1041-azure (amd64)",
  "availableMemory": "245.5 MB",
  "totalMemory": "512.0 MB",
  "maxMemory": "4.0 GB",
  "cpuCount": 4,
  "currentDate": "2025-07-22 11:24:00",
  "kubernetesEnvironment": true,
  "podName": "my-app-pod-1234",
  "nodeName": "worker-node-1"
}
```

## Kubernetes Environment Detection

The endpoint detects if the application is running in a Kubernetes environment by checking for the presence of the `HOSTNAME` environment variable, which is typically set to the pod name in Kubernetes. If the `NODE_NAME` environment variable is also available, it will be included in the response.

## Implementation

The endpoint is implemented in the `SystemInfoResource.java` class using standard Java APIs to retrieve system information:

- `OperatingSystemMXBean` for OS information and CPU count
- `Runtime` for memory information
- `LocalDateTime` for current date and time
- `System.getenv()` for Kubernetes environment detection

## Testing

The endpoint is tested using:

- `SystemInfoResourceTest.java`: Regular unit test
- `SystemInfoResourceIT.java`: Integration test for packaged mode

Both tests verify that the endpoint returns a 200 status code, has the correct content type, and includes all the expected fields in the JSON response.