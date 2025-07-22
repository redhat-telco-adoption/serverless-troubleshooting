# Kubernetes Deployment

This directory contains Kubernetes/OpenShift resources to deploy the Quarkus application in both JDK and native versions using Kustomize.

## Directory Structure

```
kubernetes/
├── deployment/            # Regular deployment resources
│   ├── base/              # Base resources common to all deployment variants
│   │   ├── configmap.yaml     # ConfigMap with application configuration
│   │   ├── deployment.yaml    # Base Deployment configuration
│   │   ├── kustomization.yaml # Kustomize configuration for base resources
│   │   └── service.yaml       # Service to expose the application
│   └── overlays/          # Deployment overlay variants
│       ├── jdk/           # JDK-specific resources
│       │   ├── deployment-patch.yaml # JDK-specific deployment configuration
│       │   ├── kustomization.yaml    # Kustomize configuration for JDK variant
│       │   └── route.yaml            # OpenShift Route for JDK variant
│       └── native/        # Native-specific resources
│           ├── deployment-patch.yaml # Native-specific deployment configuration
│           ├── kustomization.yaml    # Kustomize configuration for native variant
│           └── route.yaml            # OpenShift Route for native variant
├── serverless/            # Serverless deployment resources
│   ├── base/              # Base resources common to all serverless variants
│   │   ├── configmap.yaml     # ConfigMap with application configuration
│   │   ├── kustomization.yaml # Kustomize configuration for base resources
│   │   └── service.yaml       # Knative Service to expose the application
│   └── overlays/          # Serverless overlay variants
│       ├── jdk/           # JDK-specific resources
│       │   ├── kustomization.yaml    # Kustomize configuration for JDK variant
│       │   └── service-patch.yaml    # JDK-specific service configuration
│       └── native/        # Native-specific resources
│           ├── kustomization.yaml    # Kustomize configuration for native variant
│           └── service-patch.yaml    # Native-specific service configuration
├── argocd/                # ArgoCD deployment resources
│   ├── applicationset.yaml # ApplicationSet to deploy all four variants
│   ├── applications/      # Individual Application resources
│   │   ├── jdk-deployment.yaml   # JDK Deployment variant
│   │   ├── native-deployment.yaml # Native Deployment variant
│   │   ├── jdk-serverless.yaml   # JDK Serverless variant
│   │   └── native-serverless.yaml # Native Serverless variant
│   └── README.md          # ArgoCD deployment instructions
└── README.md              # This file
```

## Deployment Instructions

### Prerequisites

- Kubernetes cluster or OpenShift cluster
- `kubectl` or `oc` command-line tool
- `kustomize` command-line tool (optional, as it's built into kubectl/oc)

### Deploying to Kubernetes

#### JDK Version

```bash
# Apply the JDK variant
kubectl apply -k kubernetes/overlays/jdk
```

#### Native Version

```bash
# Apply the native variant
kubectl apply -k kubernetes/overlays/native
```

### Deploying to OpenShift

#### JDK Version

```bash
# Apply the JDK variant
oc apply -k kubernetes/overlays/jdk
```

#### Native Version

```bash
# Apply the native variant
oc apply -k kubernetes/overlays/native
```

### Deploying with ArgoCD

You can also deploy the application using ArgoCD, which provides GitOps-based continuous delivery:

```bash
# Deploy all four variants using ApplicationSet
kubectl apply -f kubernetes/argocd/applicationset.yaml

# Or deploy individual variants
kubectl apply -f kubernetes/argocd/applications/jdk-deployment.yaml
kubectl apply -f kubernetes/argocd/applications/native-deployment.yaml
kubectl apply -f kubernetes/argocd/applications/jdk-serverless.yaml
kubectl apply -f kubernetes/argocd/applications/native-serverless.yaml
```

For more details on ArgoCD deployment, see the [ArgoCD README](argocd/README.md).

## Accessing the Application

### Kubernetes

After deployment, you can access the application using port-forwarding:

```bash
# For JDK version
kubectl port-forward svc/jdk-deployment-code-with-quarkus 8080:8080

# For native version
kubectl port-forward svc/native-deployment-code-with-quarkus 8080:8080
```

Then access the application at http://localhost:8080

### OpenShift

In OpenShift, the application is automatically exposed via a Route:

```bash
# Get the route URL for JDK version
oc get route jdk-deployment-code-with-quarkus -o jsonpath='{.spec.host}'

# Get the route URL for native version
oc get route native-deployment-code-with-quarkus -o jsonpath='{.spec.host}'
```

Then access the application using the returned URL.

### Serverless

For serverless deployments, you can access the application using the Knative Service URL:

```bash
# Get the URL for JDK serverless
kubectl get ksvc jdk-serverless-code-with-quarkus -o jsonpath='{.status.url}'

# Get the URL for Native serverless
kubectl get ksvc native-serverless-code-with-quarkus -o jsonpath='{.status.url}'
```

## Available Endpoints

- `/hello` - Returns a simple greeting message
- `/system-info` - Returns system information in JSON format
- `/q/health/live` - Liveness health check
- `/q/health/ready` - Readiness health check

## Configuration

The application configuration is stored in a ConfigMap and mounted at `/deployments/config`. You can modify the configuration by editing the ConfigMap in `kubernetes/base/configmap.yaml`.

## Resource Requirements

### JDK Version

- Memory Request: 384Mi
- Memory Limit: 768Mi
- CPU Request: 100m

### Native Version

- Memory Request: 128Mi
- Memory Limit: 256Mi
- CPU Request: 50m

## Images

The deployment uses the following container images:

- JDK Version: `quay.io/redhat-telco-adoption/serverless-troubleshooting:latest-jdk`
- Native Version: `quay.io/redhat-telco-adoption/serverless-troubleshooting:latest-native`

You can modify the image tags in the respective `kustomization.yaml` files to use specific versions instead of `latest-jdk` and `latest-native`.