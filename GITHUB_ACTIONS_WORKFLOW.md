# GitHub Actions Workflow Documentation

This document describes the GitHub Actions workflow used in this project for building and pushing container images.

## Workflow Overview

The workflow (`build-and-push.yml`) is responsible for:

1. Building and pushing container images for both JDK and Native variants
2. Automatically bumping the version number when changes are pushed to the main branch
3. Updating Kubernetes manifests with the new version
4. Committing the version changes back to the repository

## Workflow Triggers

The workflow is triggered by:

- Pushes to the `main` branch (excluding changes in the `kubernetes/` directory)
- Pull requests to the `main` branch (excluding changes in the `kubernetes/` directory)
- Manual workflow dispatch

## Key Features

### Version Management

- The workflow extracts the current version from `pom.xml`
- When pushing to the main branch (not for pull requests), it bumps the patch version
- The new version is updated in `pom.xml` with the `-SNAPSHOT` suffix

### Kubernetes Manifest Updates

When the version is bumped, the workflow automatically updates:

1. **Deployment Overlays**: Using `kustomize edit set image` to update the image tags in:
   - `kubernetes/deployment/overlays/jdk/kustomization.yaml`
   - `kubernetes/deployment/overlays/native/kustomization.yaml`

2. **Serverless Overlays**: Using `sed` to update the image references in:
   - `kubernetes/serverless/overlays/jdk/service-patch.yaml`
   - `kubernetes/serverless/overlays/native/service-patch.yaml`

### Automatic Commits

After updating the version in `pom.xml` and the Kubernetes manifests, the workflow:

1. Configures git with GitHub Actions user credentials
2. Adds the modified files to the staging area
3. Commits the changes with a message indicating the version bump
4. Pushes the changes back to the repository

### Container Image Building

The workflow builds and pushes:

- JDK container image with tags:
  - `<version>-jdk`
  - `latest`
- Native container image with tag:
  - `<version>-native`

## Workflow Steps

1. Checkout repository
2. Set up JDK 21
3. Extract and bump version
4. Update Kustomize image tags
5. Commit version changes
6. Set up QEMU for multi-architecture builds
7. Set up Docker Buildx
8. Login to Quay.io
9. Build JVM application
10. Build and push JVM container image
11. Build Native application
12. Build and push Native container image
13. Add build summary

## Permissions

The workflow requires:
- `contents: read` - To read the repository contents
- `packages: write` - To push container images

## Environment Variables

- `REGISTRY`: The container registry (quay.io)
- `IMAGE_NAME`: The image name (redhat-telco-adoption/serverless-troubleshooting)