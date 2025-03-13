# Mobile Money B2C Payment Service

A Spring Boot microservice that handles Business-to-Consumer (B2C) mobile money payments for various mobile money providers in Kenya.

## Features

- RESTful API for initiating B2C payments
- Support for multiple mobile money providers (M-Pesa, Airtel Money, Equitel)
- OAuth2 security with role-based access control
- SMS notifications for transaction status updates
- In-memory H2 database for transaction storage
- Swagger/OpenAPI documentation
- Containerized with Docker
- Handle payment callbacks
- Transaction status tracking
- Callback forwarding with Basic Auth support

## Prerequisites

- Java 11 or higher
- Maven 3.6 or higher
- Docker
- Spring Boot 2.7.0

## Configuration

The application can be configured using environment variables:

- `OAUTH_ISSUER_URI`: OAuth2 issuer URI
- `OAUTH_JWK_URI`: OAuth2 JWK Set URI

## Building and Running

### Using Maven

```bash
# Build the application
mvn clean install

# Run the application
mvn spring-boot:run
```

### Using Docker

```bash
# Build Docker image
docker build -t mobile-money-service .

# Run Docker container
docker run -p 8080:8080 mobile-money-service
```

## API Documentation

Once the application is running, you can access the API documentation at:

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI Spec: http://localhost:8080/v3/api-docs

## API Endpoints

### 1. Initiate Payment

```http
POST /api/v1/payments
Authorization: Bearer <jwt_token>

{
  "phoneNumber": "254712345678",
  "amount": 100.00,
  "provider": "MPESA",
  "callbackUrl": "https://customer-domain.com/callback",
  "requestId": "CLIENT_REQ_001",
  "callbackUsername": "your-username",
  "callbackPassword": "your-password"
}
```

### 2. Get Transaction Status

```http
GET /api/v1/payments/{referenceNumber}
Authorization: Bearer <jwt_token>
```

### 3. Payment Callback

```http
POST /api/v1/payments/callback?referenceNumber=REF123&resultCode=00&resultDescription=Success
```

## Security

The API is secured using OAuth2/JWT. The following roles are required:

- `PAYMENT_INITIATOR`: Required for initiating payments
- `PAYMENT_VIEWER`: Required for viewing transaction status

## Testing

Run the tests using Maven:

```bash
mvn test
```

## Error Handling

The service includes comprehensive error handling for:

- Invalid phone numbers
- Unsupported payment providers
- Transaction not found
- Invalid amount
- Authentication/Authorization failures

## Monitoring and Logging

- Application logs are available in the configured log level (DEBUG for com.kcb.mobilemoney package)
- H2 Console available at: http://localhost:8080/h2-console (when running locally)

## Getting Started

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher
- Spring Boot 2.7.0

### Installation
1. Clone the repository
2. Run `mvn clean install`
3. Start the application with `mvn spring-boot:run`

## Usage

### Callback Authentication

The service supports Basic Authentication for callbacks to secure your endpoint. When you receive a callback, it will include the Basic Auth header if credentials were provided in the initial request.

#### Sample Callback Implementation

```java
@PostMapping("/callback")
public ResponseEntity<Void> handleCallback(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody PaymentResponse response) {
    // Verify Basic Auth header
    // Process the callback
    return ResponseEntity.ok().build();
}
```

#### Test Credentials
For testing purposes, you can use these credentials:
- Username: `test-callback-user`
- Password: `test-callback-pass`

### Response Format

```json
{
    "referenceNumber": "ABC123XYZ456",
    "status": "PROCESSING",
    "message": "Payment processing initiated successfully",
    "requestId": "CLIENT_REQ_001"
}
```