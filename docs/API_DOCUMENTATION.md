# AppointMe API Documentation Guide

**Last Updated:** 2026-02-14  
**Status:** Production Ready ✅

## Overview

Comprehensive Swagger/OpenAPI documentation has been implemented across all AppointMe microservices APIs. The documentation includes detailed descriptions, request/response examples, validation constraints, error responses, and JWT authentication documentation.

## Accessing Swagger UI

Each microservice exposes its own Swagger UI interface:

### Local Development

| Module | Swagger UI URL | OpenAPI JSON |
|--------|---------------|--------------|
| **Businesses** | http://localhost:8080/swagger-ui.html | http://localhost:8080/v3/api-docs |
| **Categories** | http://localhost:8081/swagger-ui.html | http://localhost:8081/v3/api-docs |
| **Users** | http://localhost:8082/swagger-ui.html | http://localhost:8082/v3/api-docs |

### Staging Environment

| Module | Swagger UI URL |
|--------|---------------|
| **Businesses** | https://staging-api.appointme.eu/swagger-ui.html |
| **Categories** | https://staging-categories.appointme.eu/swagger-ui.html |
| **Users** | https://staging-auth.appointme.eu/swagger-ui.html |

### Production Environment

| Module | Swagger UI URL |
|--------|---------------|
| **Businesses** | https://api.appointme.eu/swagger-ui.html |
| **Categories** | https://categories.appointme.eu/swagger-ui.html |
| **Users** | https://auth.appointme.eu/swagger-ui.html |

## Documentation Coverage

### ✅ Phase 1: Configuration (Complete)
- **3 OpenApiConfig classes** created with API metadata
- JWT Bearer authentication scheme documented
- Server URLs for local/staging/production environments
- API version, contact information, and license details

### ✅ Phase 2: DTOs (Complete - 10 classes documented)

**Request DTOs:**
- `BusinessRequest` - Business creation/update with validation
- `CategoryRequest` - Category creation with hierarchy support
- `RegistrationRequest` - User registration with email verification
- `AuthenticationRequest` - Login credentials
- `AuthRegBaseRequest` - Shared auth fields (email/password)

**Response DTOs:**
- `BusinessResponse` - Complete business details with ratings
- `CategoryResponse` - Category with hierarchy information
- `AuthenticationResponse` - JWT token
- `PageResponse<T>` - Generic pagination wrapper
- `ExceptionResponse` - Standard error response format

### ✅ Phase 3: Controllers (Complete - 6 controllers, ~25 endpoints)

**Public APIs (no authentication required):**
- `BusinessController` - 4 endpoints for browsing businesses
- `CategoryController` - 4 endpoints for browsing categories

**Authenticated APIs (requires JWT token):**
- `OwnerBusinessController` - 6 endpoints for managing your businesses
- `AdminBusinessController` - 5 endpoints for admin moderation
- `AdminCategoryController` - 5 endpoints for admin category management
- `AuthenticationController` - 3 endpoints for registration/login/verification

## Using the API with Swagger UI

### 1. Authentication Flow

1. **Register a new account:**
   ```
   POST /auth/register
   {
     "firstName": "John",
     "lastName": "Doe",
     "email": "john.doe@example.com",
     "password": "MySecureP@ss123"
   }
   ```

2. **Check email** for verification link (in development, check console logs)

3. **Verify account:**
   ```
   GET /auth/verify-account?token={token-from-email}
   ```

4. **Authenticate and get JWT token:**
   ```
   POST /auth/authenticate
   {
     "email": "john.doe@example.com",
     "password": "MySecureP@ss123"
   }
   
   Response:
   {
     "token": "eyJhbGciOiJIUzI1NiJ9..."
   }
   ```

5. **Use the token in Swagger UI:**
   - Click the 🔒 **Authorize** button at the top right
   - Enter: `Bearer {your-token-here}`
   - Click **Authorize**
   - All protected endpoints are now unlocked

### 2. Testing Public Endpoints

No authentication needed for:
- `GET /businesses` - Browse all businesses
- `GET /businesses/{id}` - View business details
- `GET /businesses/category/{categoryId}` - Filter by category
- `GET /categories` - Browse root categories
- `GET /categories/{id}` - View category details
- `GET /categories/{parentId}/children` - View subcategories

### 3. Testing Owner Endpoints

After authentication:
- `POST /businesses/owner` - Create your business
- `GET /businesses/owner` - View your businesses
- `PUT /businesses/owner/{id}` - Update your business
- `PATCH /businesses/owner/{id}/active` - Toggle active status
- `DELETE /businesses/owner/{id}` - Delete your business

### 4. Testing Admin Endpoints

Requires ADMIN role:
- `GET /businesses/admin` - View all businesses (including inactive)
- `PATCH /businesses/admin/{id}/active` - Block/unblock businesses
- `POST /categories/admin` - Create categories
- All moderation endpoints

## Features Documented

### ✅ Request Parameters
- Path parameters with examples and validation rules
- Query parameters with defaults and constraints
- Request bodies with field-level documentation

### ✅ Response Documentation
- Success responses (200, 201, 202, 204)
- Client errors (400, 401, 403, 404, 409)
- Server errors (500)
- Response schemas with field descriptions
- Example responses

### ✅ Validation Constraints
All Jakarta validation constraints are visible in Swagger:
- `@NotBlank`, `@NotNull` - Required fields
- `@Email` - Email format validation
- `@Size(min, max)` - Length constraints
- `@Min`, `@Max`, `@Positive` - Numeric constraints
- `@Pattern` - Regex validation (e.g., password complexity)

### ✅ Security Documentation
- JWT Bearer token authentication
- Token format and expiration (24 hours)
- Security requirements per endpoint
- 401/403 error responses for protected endpoints

### ✅ Pagination
All list endpoints support pagination:
- `page` parameter (zero-based, default: 0)
- `size` parameter (items per page, default: 10)
- Response includes: `totalElements`, `totalPages`, `pageNumber`, `isFirst`, `isLast`

## API Design Standards

### Response Status Codes

| Code | Meaning | Usage |
|------|---------|-------|
| 200 | OK | Successful GET, PUT, PATCH |
| 201 | Created | Successful POST (resource created) |
| 202 | Accepted | Async operation started (e.g., registration) |
| 204 | No Content | Successful DELETE |
| 400 | Bad Request | Validation errors, invalid input |
| 401 | Unauthorized | Missing or invalid JWT token |
| 403 | Forbidden | Valid token but insufficient permissions |
| 404 | Not Found | Resource doesn't exist |
| 409 | Conflict | Duplicate resource (email, name, etc.) |
| 500 | Internal Server Error | Unexpected server error |

### Error Response Format

All errors return `ExceptionResponse`:
```json
{
  "businessErrorCode": 409,
  "businessErrorDescription": "A business with this email already exists",
  "error": "Duplicate resource",
  "validationErrors": [
    "Email must be valid",
    "Password must contain uppercase letter"
  ],
  "errors": {
    "email": "Email already in use",
    "password": "Password too weak"
  }
}
```

### Pagination Response Format

All paginated responses follow this structure:
```json
{
  "content": [ /* array of items */ ],
  "totalElements": 127,
  "totalPages": 13,
  "pageNumber": 0,
  "pageSize": 10,
  "last": false,
  "empty": false
}
```

## Exporting OpenAPI Specifications

### JSON Format
```bash
curl http://localhost:8080/v3/api-docs -o docs/api/businesses-api.json
curl http://localhost:8081/v3/api-docs -o docs/api/categories-api.json
curl http://localhost:8082/v3/api-docs -o docs/api/users-api.json
```

### YAML Format
```bash
curl http://localhost:8080/v3/api-docs.yaml -o docs/api/businesses-api.yaml
curl http://localhost:8081/v3/api-docs.yaml -o docs/api/categories-api.yaml
curl http://localhost:8082/v3/api-docs.yaml -o docs/api/users-api.yaml
```

## Integration with Development Tools

### Postman
1. Import OpenAPI spec: File → Import → Paste OpenAPI URL
2. Collection auto-generated with all endpoints
3. Set Bearer token in collection authorization

### Insomnia
1. Import → From URL → Paste OpenAPI URL
2. All endpoints imported with documentation

### API Client Generation
Use OpenAPI Generator to create client SDKs:
```bash
openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g java \
  -o ./generated-clients/java

openapi-generator-cli generate \
  -i http://localhost:8080/v3/api-docs \
  -g typescript-axios \
  -o ./generated-clients/typescript
```

## Best Practices for Maintaining Documentation

1. **Update documentation when changing APIs** - Documentation is part of the code
2. **Test examples in Swagger UI** - Ensure examples actually work
3. **Keep validation rules in sync** - Jakarta validation → Swagger annotations
4. **Document breaking changes** - Version APIs and document migrations
5. **Review error responses** - Ensure all edge cases are documented
6. **Use realistic examples** - Not "string" or "123", but "Bella Italia Restaurant"

## Troubleshooting

### Swagger UI not loading
- Check module is running: `curl http://localhost:8080/actuator/health`
- Verify URL: Should be `/swagger-ui.html` (not `/swagger-ui/`)
- Check for port conflicts

### Authentication not working in Swagger UI
- Ensure you clicked Authorize button
- Token format must be: `Bearer {token}` (note the space)
- Token expires after 24 hours - re-authenticate if expired
- Check JWT token is valid: jwt.io

### Endpoints missing from Swagger
- Check `@RestController` annotation present
- Verify controller is in scanned package
- Check method has `@GetMapping`, `@PostMapping`, etc.
- Restart application after code changes

### Validation constraints not showing
- Add `@Schema` annotations to DTO fields
- Ensure Jakarta validation annotations present
- Verify springdoc-openapi dependency in build.gradle

## Summary

**Documentation Complete:** ✅
- **3 modules** documented
- **6 controllers** enhanced
- **10 DTOs** with schemas and examples
- **~25 endpoints** with comprehensive docs
- **JWT authentication** documented
- **All error responses** documented
- **Pagination** standardized
- **Validation constraints** visible

The API is now **production-ready** with comprehensive, accurate, and user-friendly documentation accessible via Swagger UI in all environments.
