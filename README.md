# code-with-quarkus

This project uses Quarkus, the Supersonic Subatomic Java Framework.

If you want to learn more about Quarkus, please visit its website: <https://quarkus.io/>.

## Running the application in dev mode

You can run your application in dev mode that enables live coding using:

```shell script
./mvnw quarkus:dev
```

> **_NOTE:_**  Quarkus now ships with a Dev UI, which is available in dev mode only at <http://localhost:8080/q/dev/>.

## Packaging and running the application

The application can be packaged using:

```shell script
./mvnw package
```

It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `target/quarkus-app/lib/` directory.

The application is now runnable using `java -jar target/quarkus-app/quarkus-run.jar`.

If you want to build an _über-jar_, execute the following command:

```shell script
./mvnw package -Dquarkus.package.jar.type=uber-jar
```

The application, packaged as an _über-jar_, is now runnable using `java -jar target/*-runner.jar`.

## Creating a native executable

You can create a native executable using:

```shell script
./mvnw package -Dnative
```

Or, if you don't have GraalVM installed, you can run the native executable build in a container using:

```shell script
./mvnw package -Dnative -Dquarkus.native.container-build=true
```

You can then execute your native executable with: `./target/code-with-quarkus-1.0.0-SNAPSHOT-runner`

If you want to learn more about building native executables, please consult <https://quarkus.io/guides/maven-tooling>.

## Related Guides

- REST ([guide](https://quarkus.io/guides/rest)): A Jakarta REST implementation utilizing build time processing and Vert.x. This extension is not compatible with the quarkus-resteasy extension, or any of the extensions that depend on it.
- SmallRye Health ([guide](https://quarkus.io/guides/smallrye-health)): Monitor service health

## Provided Code

### REST

Easily start your REST Web Services

[Related guide section...](https://quarkus.io/guides/getting-started-reactive#reactive-jax-rs-resources)

### SmallRye Health

Monitor your application's health using SmallRye Health

[Related guide section...](https://quarkus.io/guides/smallrye-health)

## Container Images with GitHub Actions

This project includes a GitHub Actions workflow that automatically builds and pushes container images to Quay.io. The workflow:

1. Builds both JVM and Native versions of the application
2. Creates multi-architecture (amd64, arm64) container images for both versions
3. Pushes the images to Quay.io registry

### Container Image Tags

The workflow creates the following container images:

- `quay.io/<owner>/code-with-quarkus:<version>-jdk` - JVM version of the application
- `quay.io/<owner>/code-with-quarkus:<version>-native` - Native version of the application
- `quay.io/<owner>/code-with-quarkus:latest` - Same as the JVM version

Where `<version>` is the semantic version (e.g., 1.0.1) extracted from the project's pom.xml.

### Automatic Version Bumping

The workflow automatically bumps the patch version of the project with each push to the main branch:

1. It extracts the current version from pom.xml
2. Removes the "-SNAPSHOT" suffix for container image tags
3. Increments the patch version (e.g., 1.0.0 → 1.0.1)
4. Updates the pom.xml with the new version (adding back the "-SNAPSHOT" suffix)
5. Uses the new version for container image tags

This ensures that each new build pushed to the container registry has a unique version number.

### Required GitHub Secrets

To use this workflow, you need to set up the following secrets in your GitHub repository:

- `QUAY_USERNAME`: Your Quay.io username
- `QUAY_PASSWORD`: Your Quay.io password or token

### Running the Container Images

Once the images are built and pushed, you can run them using:

```shell script
# Run the JVM version (replace <version> with the actual version, e.g., 1.0.1)
docker run -i --rm -p 8080:8080 quay.io/<owner>/code-with-quarkus:<version>-jdk

# Run the Native version (replace <version> with the actual version, e.g., 1.0.1)
docker run -i --rm -p 8080:8080 quay.io/<owner>/code-with-quarkus:<version>-native

# Or use the latest JVM version
docker run -i --rm -p 8080:8080 quay.io/<owner>/code-with-quarkus:latest
```

Replace `<owner>` with your GitHub username or organization name and `<version>` with the current version of the application.

## Kubernetes/OpenShift Deployment

This project includes Kubernetes/OpenShift resources to deploy both JDK and native versions of the application using Kustomize. The resources are located in the `kubernetes` directory.

### Directory Structure

```
kubernetes/
├── base/                  # Base resources common to all variants
├── overlays/              # Overlay variants
│   ├── jdk/               # JDK-specific resources
│   └── native/            # Native-specific resources
└── README.md              # Detailed deployment instructions
```

### Deploying to Kubernetes

```shell script
# Deploy JDK version
kubectl apply -k kubernetes/overlays/jdk

# Deploy Native version
kubectl apply -k kubernetes/overlays/native
```

### Deploying to OpenShift

```shell script
# Deploy JDK version
oc apply -k kubernetes/overlays/jdk

# Deploy Native version
oc apply -k kubernetes/overlays/native
```

For more detailed instructions, including how to access the application, available endpoints, and configuration options, see the [Kubernetes Deployment README](kubernetes/README.md).
