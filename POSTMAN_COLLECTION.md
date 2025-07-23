# Postman Collection for Code with Quarkus API

This document describes the Postman collection created for testing the Code with Quarkus API endpoints.

## Collection Overview

The Postman collection (`code-with-quarkus-api.postman_collection.json`) contains requests for all endpoints in the Code with Quarkus application, organized into folders based on the deployment environment:

1. **JDK Serverless (HTTP)** - Requests to the JDK serverless deployment over HTTP
2. **Native Serverless (HTTP)** - Requests to the Native serverless deployment over HTTP
3. **JDK Serverless (HTTPS)** - Requests to the JDK serverless deployment over HTTPS
4. **Native Serverless (HTTPS)** - Requests to the Native serverless deployment over HTTPS
5. **Local Development** - Requests to the local development environment

## Endpoints Included

The collection includes the following endpoints:

1. **Get System Info** (`/system-info`) - Returns detailed system information in JSON format including OS details, memory information, CPU count, current date, and Kubernetes environment information.
2. **Get Hello** (`/hello`) - Returns a simple text greeting (only included in the Local Development folder).

## How to Import the Collection into Postman

1. Open Postman
2. Click on the "Import" button in the top left corner
3. Select "File" and choose the `code-with-quarkus-api.postman_collection.json` file
4. Click "Import"

The collection will now be available in your Postman workspace.

## Using the Collection

1. Select the appropriate folder based on your deployment environment
2. Click on the request you want to send
3. Click the "Send" button to execute the request

## Customizing the Collection

If you need to modify the base URLs or add new requests:

1. Right-click on the collection or folder and select "Edit"
2. Make your changes
3. Click "Update" to save

## Exporting the Collection

If you make changes to the collection and want to export it:

1. Click on the three dots (...) next to the collection name
2. Select "Export"
3. Choose the export format (recommended: Collection v2.1)
4. Save the file

## Relationship to rest-api.http

This Postman collection was created based on the requests in the `rest-api.http` file. It contains all the same endpoints and environments, but in a format that can be imported and used in Postman.

The main advantages of using Postman over the .http file include:
- A more user-friendly interface
- The ability to save and view responses
- Support for environment variables
- The ability to run collections as part of automated tests