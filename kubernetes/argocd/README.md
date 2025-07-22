# ArgoCD Deployment

This directory contains ArgoCD resources to deploy the Quarkus application in four different variants:

1. JDK Deployment: Standard Kubernetes Deployment using the JDK image
2. Native Deployment: Standard Kubernetes Deployment using the native image
3. JDK Serverless: Knative Service using the JDK image
4. Native Serverless: Knative Service using the native image

## Directory Structure

```
argocd/
├── applicationset.yaml       # ApplicationSet to deploy all four variants
├── applications/             # Individual Application resources
│   ├── jdk-deployment.yaml   # JDK Deployment variant
│   ├── native-deployment.yaml # Native Deployment variant
│   ├── jdk-serverless.yaml   # JDK Serverless variant
│   └── native-serverless.yaml # Native Serverless variant
└── README.md                 # This file
```

## Deployment Options

You have two options for deploying the application with ArgoCD:

### Option 1: Using ApplicationSet

The ApplicationSet will generate four separate ArgoCD Applications, one for each variant. This is the recommended approach if you want to manage all variants from a single resource.

```bash
# Apply the ApplicationSet
kubectl apply -f kubernetes/argocd/applicationset.yaml
```

### Option 2: Using Individual Applications

You can also apply individual Application resources if you prefer to manage each variant separately.

```bash
# Apply the JDK Deployment variant
kubectl apply -f kubernetes/argocd/applications/jdk-deployment.yaml

# Apply the Native Deployment variant
kubectl apply -f kubernetes/argocd/applications/native-deployment.yaml

# Apply the JDK Serverless variant
kubectl apply -f kubernetes/argocd/applications/jdk-serverless.yaml

# Apply the Native Serverless variant
kubectl apply -f kubernetes/argocd/applications/native-serverless.yaml
```

## Prerequisites

- ArgoCD installed in your cluster
- Knative Serving installed (for serverless variants)
- Access to the Git repository containing the application code

## Configuration

Before applying the ArgoCD resources, you may need to update the following:

1. Repository URL: Update the `repoURL` field in the ApplicationSet or individual Applications to point to your Git repository.
2. Target Revision: Update the `targetRevision` field if you want to deploy from a specific branch, tag, or commit.
3. Destination: Update the `destination.server` and `destination.namespace` fields to specify where the application should be deployed.

## Accessing the Applications

After deployment, you can access the applications as follows:

### Regular Deployments

For the regular deployment variants (JDK and Native), you can access the application using the Service or Route:

```bash
# Get the route URL for JDK deployment
kubectl get route jdk-deployment-code-with-quarkus -n code-with-quarkus -o jsonpath='{.spec.host}'

# Get the route URL for Native deployment
kubectl get route native-deployment-code-with-quarkus -n code-with-quarkus -o jsonpath='{.spec.host}'
```

### Serverless Deployments

For the serverless variants (JDK and Native), you can access the application using the Knative Service URL:

```bash
# Get the URL for JDK serverless
kubectl get ksvc jdk-serverless-code-with-quarkus -n code-with-quarkus -o jsonpath='{.status.url}'

# Get the URL for Native serverless
kubectl get ksvc native-serverless-code-with-quarkus -n code-with-quarkus -o jsonpath='{.status.url}'
```

## Monitoring Deployment Status

You can monitor the deployment status in the ArgoCD UI or using the ArgoCD CLI:

```bash
# Check the status of all applications
argocd app list

# Check the status of a specific application
argocd app get jdk-deployment
```