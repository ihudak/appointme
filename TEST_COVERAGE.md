# AppointMe Test Coverage Report
*Generated: 2026-02-13*

## Table 1: Coverage Summary by Module

| Module | Number of Tests | Number of Classes | Number of Test Classes | Coverage % |
|--------|----------------|-------------------|------------------------|------------|
| Businesses | 523 | 14 | 13 | 92.9% |
| Categories | 146 | 10 | 9 | 90.0% |
| Users | 282 | 22 | 12 | 54.5% |
| Exceptions | 134 | 7 | 7 | 100.0% |
| Common | 190 | 9 | 8 | 88.9% |
| Feedback | 0 | 1 | 0 | 0.0% |
| **TOTAL** | **1,275** | **63** | **49** | **77.8%** |

---

## Table 2: Detailed Test Coverage by Class

### Module: Businesses

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| Business | BusinessTest | Unit | No |
| BusinessImage | BusinessImageTest | Unit | No |
| BusinessKeyword | BusinessKeywordTest | Unit | No |
| BusinessRepository | BusinessRepositoryTest | Integration | Yes |
| BusinessService | BusinessServiceTest | Unit | No |
| BusinessMapper | BusinessMapperTest | Unit | No |
| BusinessRequest | BusinessRequestTest | Unit | No |
| BusinessResponse | BusinessResponseTest | Unit | No |
| BusinessController | BusinessControllerTest | Component (@WebMvcTest) | No |
| OwnerBusinessController | OwnerBusinessControllerTest | Component (@WebMvcTest) | No |
| AdminBusinessController | AdminBusinessControllerTest | Component (@WebMvcTest) | No |
| CategoryFeignClient | CategoryFeignClientTest | Integration | No |
| RatingConfig | RatingConfigTest | Integration | No |
| BusinessesApplication | BusinessesApplicationTest | Integration | Yes |

---

### Module: Categories

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| Category | CategoryTest | Unit | No |
| CategoryKeyword | CategoryKeywordTest | Unit | No |
| CategoryRepository | ❌ *Not covered* | | |
| CategoryService | CategoryServiceTest | Unit | No |
| CategoryMapper | CategoryMapperTest | Unit | No |
| CategoryRequest | CategoryRequestTest | Unit | No |
| CategoryResponse | CategoryResponseTest | Unit | No |
| CategoryController | CategoryControllerTest | Component (@WebMvcTest) | No |
| AdminCategoryController | AdminCategoryControllerTest | Component (@WebMvcTest) | No |
| CategoriesApplication | CategoriesApplicationTest | Integration | Yes |

**Note:** CategoryRepository is tested indirectly via CategoryIntegrationTest

---

### Module: Users

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| User | UserTest | Unit | No |
| Group | GroupTest | Unit | No |
| Token | TokenTest | Unit | No |
| Role | RoleTest | Unit | No |
| UserRepository | ❌ *Not covered* | | |
| GroupRepository | ❌ *Not covered* | | |
| TokenRepository | ❌ *Not covered* | | |
| RoleRepository | ❌ *Not covered* | | |
| AuthenticationService | AuthenticationServiceTest | Unit | No |
| EmailService | ❌ *Not covered* | | |
| UserDetailsServiceImpl | UserDetailsServiceImplTest | Unit | No |
| JwtService | JwtServiceTest | Unit | No |
| JwtFilter | ❌ *Not covered* | | |
| SecurityConfig | ❌ *Not covered* | | |
| BeansConfig | ❌ *Not covered* | | |
| AuthenticationController | AuthenticationControllerTest | Component (@WebMvcTest) | No |
| AuthenticationRequest | ❌ *Not covered* | | |
| AuthenticationResponse | ❌ *Not covered* | | |
| RegistrationRequest | RegistrationRequestValidationTest | Unit | No |
| AuthRegBaseRequest | ❌ *Not covered* | | |
| EmailTemplateName | EmailTemplateNameTest | Unit | No |
| UsersApplication | UsersApplicationTest | Integration | Yes |

**Note:** UserRepository, GroupRepository, TokenRepository, RoleRepository are tested indirectly via UserIntegrationTest

---

### Module: Exceptions

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| UserAuthenticationException | UserAuthenticationExceptionTest | Unit | No |
| ResourceNotFoundException | ResourceNotFoundExceptionTest | Unit | No |
| OperationNotPermittedException | OperationNotPermittedExceptionTest | Unit | No |
| ActivationTokenException | ActivationTokenExceptionTest | Unit | No |
| GlobalExceptionHandler | GlobalExceptionHandlerTest | Unit | No |
| ExceptionResponse | ExceptionResponseTest | Unit | No |
| BusinessErrorCodes | BusinessErrorCodesTest | Unit | No |

---

### Module: Common

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| SecurityUtils | SecurityUtilsTest | Unit | No |
| FileStorageService | ❌ *Not covered* | | |
| PageResponse | PageResponseTest | Unit | No |
| Keyword | KeywordTest | Unit | No |
| BaseEntity | BaseEntityTest | Unit | No |
| BaseBasicEntity | BaseBasicEntityTest | Unit | No |
| Address | AddressTest | Unit | No |
| AuditConfig | AuditConfigTest | Unit | No |
| ApplicationAuditAware | ApplicationAuditAwareTest | Unit | No |

---

### Module: Feedback

| Class | Test Class | Test Type | Requires Docker |
|-------|-----------|-----------|-----------------|
| FeedbackApplication | ❌ *Not covered* | | |

**Note:** Feedback module is incomplete - only contains Application.java main class

---

## Coverage Gaps Summary

### High Priority - Users Module (10 classes)
- **Repositories** (4): UserRepository, GroupRepository, TokenRepository, RoleRepository
- **Services** (1): EmailService
- **Security** (3): JwtFilter, SecurityConfig, BeansConfig
- **DTOs** (2): AuthenticationRequest, AuthenticationResponse, AuthRegBaseRequest

### Medium Priority
- **Common**: FileStorageService
- **Categories**: CategoryRepository (tested via integration tests)

### Low Priority
- **Feedback**: FeedbackApplication (module incomplete)

---

## Test Type Distribution

| Test Type | Count | Description | Docker Required |
|-----------|-------|-------------|-----------------|
| Unit Tests | 35 | Fast, isolated, MockitoExtension | No |
| Component Tests (@WebMvcTest) | 6 | Controller testing with MockMvc | No |
| Integration Tests (@SpringBootTest) | 6 | Full context + Testcontainers | Yes |

---

## Recommendations

To reach **90% overall coverage**, add tests for:
1. Users module repositories (4 classes) → ~40-60 tests
2. EmailService → ~10-15 tests
3. JwtFilter, SecurityConfig, BeansConfig → ~15-20 tests
4. Remaining DTOs → ~10-15 tests
5. FileStorageService → ~10-15 tests

**Estimated impact:** Would bring Users module to ~80% and overall project to ~90%
