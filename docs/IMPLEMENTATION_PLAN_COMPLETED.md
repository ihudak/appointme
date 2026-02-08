# ✅ COMPLETED - Implementation Plan: Businesses by Category with Subcategories

**Status:** FULLY IMPLEMENTED  
**Completion Date:** Before 2026-02-08  

---

## Summary

This feature has been **fully implemented** across both Categories and Business modules.

### What Was Implemented:

✅ **Categories Module:**
- Endpoint: `GET /categories/{categoryId}/subcategories/ids`
- Service method: `findAllActiveSubcategoryIdsRecursively(Long categoryId)`
- Returns all descendant category IDs recursively

✅ **Business Module:**
- OpenFeign dependency added
- `@EnableFeignClients` enabled in BusinessesApplication
- FeignClient: `CategoryFeignClient` communicates with Categories service
- Repository query: `findByCategoryIdIn(Set<Long> categoryIds, Pageable pageable)`
- Service method: `findByCategoryWithSubcategories(Long categoryId, int page, int size)`
- Endpoint: `GET /businesses/category/{categoryId}/with-subcategories`

### Key Files:

**Categories Module:**
- `CategoryService.java` - line 67: `findAllActiveSubcategoryIdsRecursively()`
- `CategoryController.java` - line 47-54: Endpoint implementation

**Business Module:**
- `build.gradle` - line 9: OpenFeign dependency
- `BusinessesApplication.java` - line 11: `@EnableFeignClients`
- `client/CategoryFeignClient.java` - Feign client interface
- `BusinessRepository.java` - line 18: `findByCategoryIdIn()` query
- `BusinessService.java` - line 73: Service method with Feign call
- `BusinessController.java` - line 45-56: Controller endpoint

---

# Original Implementation Plan

## Problem Statement
Currently, the Business module has an endpoint `/businesses/category/{categoryId}` that returns only businesses directly assigned to a specific category. We need a new endpoint that returns businesses from a category AND all its subcategories (recursive hierarchy).

## Proposed Approach
1. Add a new endpoint in Categories module to return all descendant category IDs recursively
2. Add OpenFeign client to Business module to communicate with Categories service
3. Add new endpoint in Business module that uses Feign client to fetch subcategories and query businesses

## Architecture Decisions
- **Separate endpoint** (not modifying existing one) to maintain performance for leaf categories
- **OpenFeign** for inter-service communication (clean code, K8s-friendly, resilience features)
- **Set<Long>** return type for category IDs (efficient, no duplicates)
- **Service-to-service call** pattern (better UX with single client API call)

## Use Case
User browses category hierarchy level by level, but at some point clicks "show me all businesses" to see results from the selected category and all its subcategories. For leaf categories (no children), the app uses the fast existing endpoint.

---

## Implementation Workplan

### Phase 1: Categories Module - Add Recursive Subcategory Endpoint

- [ ] **1.1** Add repository method to `CategoryRepository`
  - Add: `List<Category> findByParentId(Long parentId);` (non-paginated version)
  - Allows efficient recursive traversal without pagination overhead
  - **Location**: `categories/src/main/java/eu/dec21/appointme/categories/categories/repository/CategoryRepository.java`

- [ ] **1.2** Add method `findAllSubcategoryIdsRecursively(Long categoryId)` to `CategoryService`
  - Return type: `Set<Long>`
  - Algorithm: Recursive BFS/DFS traversal
    1. Verify parent category exists (throw `ResourceNotFoundException` if not)
    2. Initialize result set: `Set<Long> result = new HashSet<>();`
    3. Get immediate children: `List<Category> children = categoryRepository.findByParentId(categoryId);`
    4. For each child: add to result set and recursively process
  - Handle edge case: if no children, return empty set
  - **Location**: `categories/src/main/java/eu/dec21/appointme/categories/categories/service/CategoryService.java`
  - **Dependencies**: Inject `CategoryRepository` (already injected)

- [ ] **1.3** Add controller endpoint in `CategoryController`
  - Path: `GET /categories/{categoryId}/subcategories/ids`
  - Return: `ResponseEntity<Set<Long>>`
  - Add `@Operation` annotation:
    ```java
    @Operation(
        summary = "Get all subcategory IDs recursively",
        description = "Retrieves all subcategory IDs (children, grandchildren, etc.) of a category"
    )
    ```
  - Call service method and wrap in `ResponseEntity.ok()`
  - **Location**: `categories/src/main/java/eu/dec21/appointme/categories/categories/controller/CategoryController.java`

- [ ] **1.4** Test Categories endpoint
  - Start Categories service: `.\gradlew :categories:bootRun`
  - Test via Swagger UI at `http://localhost:<port>/swagger-ui.html`
  - Test scenarios:
    - Category with no children → returns empty set `[]`
    - Category with multiple levels → returns all descendant IDs
    - Invalid category ID → returns 404 with `ResourceNotFoundException`

---

### Phase 2: Business Module - Add OpenFeign Dependencies

- [ ] **2.1** Check Spring Cloud BOM version compatibility
  - Spring Boot 4.0.1 requires Spring Cloud 2025.0.x (or compatible version)
  - Check latest compatible version at: https://spring.io/projects/spring-cloud

- [ ] **2.2** Add Spring Cloud BOM to root `build.gradle`
  - Add to root `build.gradle` in `subprojects` block or create dependency management section:
    ```gradle
    ext {
        springCloudVersion = '2025.0.0'  // Use appropriate version
    }
    ```
  - Add BOM import (if using dependency management plugin)

- [ ] **2.3** Add OpenFeign dependency to `businesses/build.gradle`
  - Add after other Spring Boot dependencies:
    ```gradle
    implementation 'org.springframework.cloud:spring-cloud-starter-openfeign'
    ```
  - **Location**: `businesses/build.gradle`

- [ ] **2.4** Enable Feign clients in BusinessesApplication
  - Add `@EnableFeignClients` annotation to `BusinessesApplication` class
  - Update imports: `import org.springframework.cloud.openfeign.EnableFeignClients;`
  - **Location**: `businesses/src/main/java/eu/dec21/appointme/businesses/BusinessesApplication.java`

---

### Phase 3: Business Module - Create Feign Client

- [ ] **3.1** Create `client` package and `CategoryFeignClient` interface
  - **Location**: `businesses/src/main/java/eu/dec21/appointme/businesses/client/CategoryFeignClient.java`
  - Create new package: `eu.dec21.appointme.businesses.client`
  - Interface structure:
    ```java
    package eu.dec21.appointme.businesses.client;
    
    import org.springframework.cloud.openfeign.FeignClient;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.web.bind.annotation.PathVariable;
    import java.util.Set;
    
    @FeignClient(name = "categories", url = "${categories.service.url}")
    public interface CategoryFeignClient {
        
        @GetMapping("/categories/{categoryId}/subcategories/ids")
        Set<Long> getAllSubcategoryIds(@PathVariable("categoryId") Long categoryId);
    }
    ```

- [ ] **3.2** Create or modify application configuration file
  - Check if `application.yml` or `application.properties` exists in `businesses/src/main/resources/`
  - If not, create `application.yml`
  - Add configuration:
    ```yaml
    categories:
      service:
        url: http://localhost:8082  # Local dev (adjust port as needed)
        # url: http://categories-service:8080  # K8s deployment
    
    feign:
      client:
        config:
          categories:
            connectTimeout: 5000
            readTimeout: 10000
            loggerLevel: basic
    ```
  - **Note**: Port 8082 is a placeholder; adjust based on actual Categories service port

- [ ] **3.3** Add Feign client error handling (optional for MVP)
  - Can be added later for production resilience
  - Options: Custom error decoder, fallback methods, circuit breaker
  - Skip for initial implementation to keep it simple

---

### Phase 4: Business Module - Implement New Endpoint

- [ ] **4.1** Add repository method to `BusinessRepository`
  - Add to existing repository interface:
    ```java
    @Query("SELECT DISTINCT b FROM Business b JOIN b.categoryIds c WHERE c IN :categoryIds ORDER BY b.weightedRating DESC")
    Page<Business> findByCategoryIdIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);
    ```
  - **Location**: `businesses/src/main/java/eu/dec21/appointme/businesses/businesses/repository/BusinessRepository.java`
  - **Note**: `ORDER BY` in query ensures consistent sorting even with Pageable

- [ ] **4.2** Add service method to `BusinessService`
  - Method signature: `public PageResponse<BusinessResponse> findByCategoryWithSubcategories(Long categoryId, int page, int size)`
  - Inject `CategoryFeignClient` via constructor (add to existing `@RequiredArgsConstructor` dependencies)
  - Implementation logic:
    ```java
    public PageResponse<BusinessResponse> findByCategoryWithSubcategories(Long categoryId, int page, int size) {
        // 1. Get all subcategory IDs from Categories service
        Set<Long> allCategoryIds = categoryFeignClient.getAllSubcategoryIds(categoryId);
        
        // 2. Add parent category ID
        allCategoryIds.add(categoryId);
        
        // 3. Create pageable with sort by weightedRating descending
        Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
        
        // 4. Query businesses by all category IDs
        Page<Business> businesses = businessRepository.findByCategoryIdIn(allCategoryIds, pageable);
        
        // 5. Map to response (reuse existing pattern from findByCategory)
        return new PageResponse<>(
            businesses.getContent().stream().map(businessMapper::toBusinessResponse).toList(),
            businesses.getTotalElements(),
            businesses.getTotalPages(),
            businesses.getNumber(),
            businesses.getSize(),
            businesses.isLast(),
            businesses.isEmpty()
        );
    }
    ```
  - **Location**: `businesses/src/main/java/eu/dec21/appointme/businesses/businesses/service/BusinessService.java`

- [ ] **4.3** Add controller endpoint to `BusinessController`
  - Add new method to existing controller:
    ```java
    @GetMapping("category/{categoryId}/with-subcategories")
    @Operation(
        summary = "Get businesses by category including subcategories",
        description = "Retrieves all businesses in a category and all its subcategories (recursive hierarchy)"
    )
    public ResponseEntity<PageResponse<BusinessResponse>> getBusinessesByCategoryWithSubcategories(
            @PathVariable Long categoryId,
            @RequestParam(name = "page", defaultValue = "0", required = false) int page,
            @RequestParam(name = "size", defaultValue = "10", required = false) int size
    ) {
        return ResponseEntity.ok(businessService.findByCategoryWithSubcategories(categoryId, page, size));
    }
    ```
  - **Location**: `businesses/src/main/java/eu/dec21/appointme/businesses/businesses/controller/BusinessController.java`

---

### Phase 5: Integration Testing & Documentation

- [ ] **5.1** Build both modules
  - Run: `.\gradlew :categories:build :businesses:build`
  - Verify no compilation errors
  - Check that Feign client dependency is resolved

- [ ] **5.2** Start infrastructure services
  - Run: `.\gradlew composeUp`
  - Verify PostgreSQL, Keycloak, and other services are running
  - Check docker containers: `docker ps`

- [ ] **5.3** Test Categories service standalone
  - Start: `.\gradlew :categories:bootRun`
  - Note the port (check console output)
  - Access Swagger UI: `http://localhost:<port>/swagger-ui.html`
  - Test new endpoint: `GET /categories/{categoryId}/subcategories/ids`
  - Verify response format and behavior

- [ ] **5.4** Update Business service configuration
  - Update `categories.service.url` in Business module's application.yml
  - Set to actual Categories service URL (e.g., `http://localhost:8082`)

- [ ] **5.5** Start Business service
  - In new terminal: `.\gradlew :businesses:bootRun`
  - Verify Feign client initialization in logs
  - Access Swagger UI: `http://localhost:<port>/swagger-ui.html`

- [ ] **5.6** Integration testing scenarios
  - **Scenario 1**: Category with no children
    - Call: `GET /businesses/category/{leafCategoryId}/with-subcategories`
    - Verify: Returns same results as `GET /businesses/category/{leafCategoryId}`
  
  - **Scenario 2**: Category with nested subcategories
    - Create/use category hierarchy: Parent → Child → Grandchild
    - Add businesses to different levels
    - Call: `GET /businesses/category/{parentId}/with-subcategories`
    - Verify: Returns businesses from all levels
  
  - **Scenario 3**: Invalid category ID
    - Call: `GET /businesses/category/99999/with-subcategories`
    - Verify: Returns 404 with proper error response
  
  - **Scenario 4**: Pagination
    - Use category with many businesses across subcategories
    - Test with different page sizes (5, 10, 20)
    - Verify: `totalElements`, `totalPages`, `pageNumber` are correct
    - Verify: Sorting by `weightedRating` descending is maintained

- [ ] **5.7** Error handling verification
  - Test with Categories service down
    - Stop Categories service
    - Call Business endpoint
    - Verify: Graceful error (503 or appropriate message)
  
  - Test with malformed category ID
    - Call with string instead of number
    - Verify: 400 Bad Request

- [ ] **5.8** Verify API documentation
  - Check Swagger UI for both services
  - Verify operation summaries and descriptions are clear
  - Verify the difference between endpoints is documented:
    - `/businesses/category/{id}` - "Direct category only"
    - `/businesses/category/{id}/with-subcategories` - "Including all subcategories"

- [ ] **5.9** Performance check (optional)
  - Compare response times between direct and recursive endpoints
  - Check database query execution plans if needed
  - Verify no N+1 query issues

---

## Technical Notes

### Categories Module Implementation Details

**Recursive Traversal Algorithm:**
```java
private void collectSubcategoryIds(Long parentId, Set<Long> result) {
    List<Category> children = categoryRepository.findByParentId(parentId);
    for (Category child : children) {
        result.add(child.getId());
        collectSubcategoryIds(child.getId(), result);  // Recursive call
    }
}

public Set<Long> findAllSubcategoryIdsRecursively(Long categoryId) {
    // Verify parent exists
    categoryRepository.findById(categoryId)
        .orElseThrow(() -> new ResourceNotFoundException("Category not found with id " + categoryId));
    
    Set<Long> result = new HashSet<>();
    collectSubcategoryIds(categoryId, result);
    return result;
}
```

**Performance Considerations:**
- Java recursion is clean and sufficient for typical category hierarchies (depth < 10)
- Consider caching results if category hierarchy rarely changes
- PostgreSQL recursive CTEs could be an alternative but adds complexity
- Current approach: O(n) where n = total categories in subtree

### Business Module Implementation Details

**Query Performance:**
- `IN` clause with `Set<Long>` performs well for reasonable hierarchy sizes (< 100 categories)
- `DISTINCT` prevents duplicate businesses if assigned to multiple subcategories
- Index on `business_category_ids.category_id` (already exists) optimizes the JOIN
- Sorting by `weightedRating DESC` uses existing index on `businesses.weightedRating`

**Feign Client Behavior:**
- Synchronous blocking call (suitable for this use case)
- Default timeouts: connect 10s, read 60s (configured to 5s and 10s for faster failure)
- On failure: Throws `FeignException` which will be caught by `GlobalExceptionHandler`

**Error Scenarios:**
1. **Invalid category ID**: Categories service returns 404 → Feign throws `FeignException.NotFound` → Maps to 404
2. **Categories service down**: Connection timeout → Throws `FeignException` → Maps to 503 Service Unavailable
3. **Empty result**: Returns empty set `[]` → Business query returns empty page (valid response)

### OpenFeign Configuration

**application.yml structure:**
```yaml
# Business module configuration
server:
  port: 8081  # Or appropriate port

categories:
  service:
    url: http://localhost:8082  # Local development
    # url: http://categories-service:8080  # Kubernetes

feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
      categories:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: basic  # Use 'full' for debugging
```

**Logging Levels:**
- `none` - No logging (default)
- `basic` - Request method, URL, response status, execution time
- `headers` - Basic + request/response headers
- `full` - Headers + body (use in development only)

### Codebase Patterns to Follow

**Service Method Structure:**
```java
public PageResponse<BusinessResponse> findByCategoryWithSubcategories(Long categoryId, int page, int size) {
    // 1. External service call (if needed)
    Set<Long> categoryIds = categoryFeignClient.getAllSubcategoryIds(categoryId);
    
    // 2. Business logic
    categoryIds.add(categoryId);
    
    // 3. Create pageable with sort
    Pageable pageable = PageRequest.of(page, size, Sort.by("weightedRating").descending());
    
    // 4. Repository query
    Page<Business> businesses = businessRepository.findByCategoryIdIn(categoryIds, pageable);
    
    // 5. Map to response and return
    return new PageResponse<>(
        businesses.getContent().stream().map(businessMapper::toBusinessResponse).toList(),
        businesses.getTotalElements(),
        businesses.getTotalPages(),
        businesses.getNumber(),
        businesses.getSize(),
        businesses.isLast(),
        businesses.isEmpty()
    );
}
```

**Repository Query Pattern:**
```java
@Query("SELECT DISTINCT b FROM Business b JOIN b.categoryIds c WHERE c IN :categoryIds ORDER BY b.weightedRating DESC")
Page<Business> findByCategoryIdIn(@Param("categoryIds") Set<Long> categoryIds, Pageable pageable);
```
- Use `DISTINCT` when joining collections that might have duplicates
- Use named parameters with `@Param`
- Include `ORDER BY` in query for explicit sorting (even though Pageable has sort)

**Controller Pattern:**
```java
@GetMapping("category/{categoryId}/with-subcategories")
@Operation(summary = "...", description = "...")
public ResponseEntity<PageResponse<BusinessResponse>> methodName(
        @PathVariable Long categoryId,
        @RequestParam(name = "page", defaultValue = "0", required = false) int page,
        @RequestParam(name = "size", defaultValue = "10", required = false) int size
) {
    return ResponseEntity.ok(service.methodName(categoryId, page, size));
}
```

### Kubernetes Considerations

**Service Discovery:**
- Categories service exposed as K8s Service: `categories-service`
- Business module connects via: `http://categories-service:8080`
- Use K8s service name (not pod IP) for resilience

**Configuration Strategy:**
- Use ConfigMaps for `categories.service.url`
- Override via environment variable: `CATEGORIES_SERVICE_URL`
- Example K8s deployment:
  ```yaml
  env:
    - name: CATEGORIES_SERVICE_URL
      value: "http://categories-service:8080"
  ```

**Health Checks:**
- Spring Boot Actuator provides `/actuator/health` endpoint
- Configure K8s liveness and readiness probes
- Ensure Categories service is healthy before Business service starts

**Resilience (Future Enhancement):**
- Consider Spring Cloud Circuit Breaker (Resilience4j)
- Add fallback behavior if Categories service is down
- Cache category hierarchy for better availability

---

## Files to Create/Modify

### Categories Module
- **Modify**: `CategoryService.java` - add recursive method
- **Modify**: `CategoryRepository.java` - add non-paginated query method if needed
- **Modify**: `CategoryController.java` - add new endpoint

### Business Module
- **Modify**: `businesses/build.gradle` - add OpenFeign dependency
- **Create**: `CategoryFeignClient.java` - Feign client interface
- **Modify**: `BusinessRepository.java` - add query method for multiple category IDs
- **Modify**: `BusinessService.java` - add new method with Feign client call
- **Modify**: `BusinessController.java` - add new endpoint
- **Modify**: Application main class - add `@EnableFeignClients`
- **Modify**: `application.yml`/`application.properties` - add Categories service URL and Feign config

---

## Success Criteria
- ✅ Categories endpoint returns all descendant category IDs recursively
- ✅ Business endpoint returns businesses from parent category + all subcategories
- ✅ Existing `/businesses/category/{categoryId}` endpoint remains unchanged and fast
- ✅ Pagination works correctly
- ✅ OpenFeign client communicates successfully between services
- ✅ Proper error handling for invalid category IDs and service failures
- ✅ API documentation updated and clear
