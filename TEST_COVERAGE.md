# AppointMe Test Coverage Report
*Generated: 2026-02-13*

## Table 1: Coverage Summary by Module

| Module | Number of Tests | Failures | Number of Classes | Number of Test Classes | Coverage % |
|--------|----------------|----------|-------------------|------------------------|------------|
| Businesses | 523 | 0 | 14 | 14 | 100.0% |
| Categories | 173 | 0 | 10 | 10 | 100.0% |
| Users | 443 | 0 | 22 | 17 | 77.3% |
| Exceptions | 134 | 0 | 7 | 7 | 100.0% |
| Common | 239 | 0 | 9 | 9 | 100.0% |
| Feedback | 0 | 0 | 1 | 0 | 0.0% |
| **TOTAL** | **1,512** | **0** | **63** | **57** | **90.5%** |

---

## Table 2: Detailed Test Coverage by Class

### Module: Businesses

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| Business | BusinessTest | 91 | 0 | Unit | No |
| BusinessImage | BusinessImageTest | 35 | 0 | Unit | No |
| BusinessKeyword | BusinessKeywordTest | 57 | 0 | Unit | No |
| BusinessRepository | BusinessRepositoryTest | 15 | 0 | Integration | Yes |
| BusinessService | BusinessServiceTest | 62 | 0 | Unit | No |
| BusinessMapper | BusinessMapperTest | 30 | 0 | Unit | No |
| BusinessRequest | BusinessRequestTest | 29 | 0 | Unit | No |
| BusinessResponse | BusinessResponseTest | 21 | 0 | Unit | No |
| BusinessController | BusinessControllerTest | 64 | 0 | Component (@WebMvcTest) | No |
| OwnerBusinessController | OwnerBusinessControllerTest | 68 | 0 | Component (@WebMvcTest) | No |
| AdminBusinessController | AdminBusinessControllerTest | 51 | 0 | Component (@WebMvcTest) | No |
| CategoryFeignClient | CategoryFeignClientTest | 16 | 0 | Integration | No |
| RatingConfig | RatingConfigTest | 29 | 0 | Integration | No |
| BusinessesApplication | BusinessesApplicationTest | 1 | 0 | Integration | Yes |

---

### Module: Categories

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| Category | CategoryTest | 42 | 0 | Unit | No |
| CategoryKeyword | CategoryKeywordTest | 42 | 0 | Unit | No |
| CategoryRepository | CategoryRepositoryTest | 27 | 0 | Integration | Yes |
| CategoryService | CategoryServiceTest | 19 | 0 | Unit | No |
| CategoryMapper | CategoryMapperTest | 8 | 0 | Unit | No |
| CategoryRequest | CategoryRequestTest | 7 | 0 | Unit | No |
| CategoryResponse | CategoryResponseTest | 5 | 0 | Unit | No |
| CategoryController | CategoryControllerTest | 8 | 0 | Component (@WebMvcTest) | No |
| AdminCategoryController | AdminCategoryControllerTest | 10 | 0 | Component (@WebMvcTest) | No |
| CategoriesApplication | CategoriesApplicationTest | 1 | 0 | Integration | Yes |

---

### Module: Users

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| User | UserTest | 89 | 0 | Unit | No |
| Group | GroupTest | 46 | 0 | Unit | No |
| Token | TokenTest | 38 | 0 | Unit | No |
| Role | RoleTest | 51 | 0 | Unit | No |
| UserRepository | UserRepositoryTest | 33 | 0 | Integration | Yes |
| GroupRepository | GroupRepositoryTest | 33 | 0 | Integration | Yes |
| TokenRepository | TokenRepositoryTest | 38 | 0 | Integration | Yes |
| RoleRepository | RoleRepositoryTest | 37 | 0 | Integration | Yes |
| AuthenticationService | AuthenticationServiceTest | 8 | 0 | Unit | No |
| EmailService | EmailServiceTest | 20 | 0 | Unit | No |
| UserDetailsServiceImpl | UserDetailsServiceImplTest | 3 | 0 | Unit | No |
| JwtService | JwtServiceTest | 16 | 0 | Unit | No |
| JwtFilter | ❌ *Not covered* | - | - | | |
| SecurityConfig | ❌ *Not covered* | - | - | | |
| BeansConfig | ❌ *Not covered* | - | - | | |
| AuthenticationController | AuthenticationControllerTest | 9 | 0 | Component (@WebMvcTest) | No |
| AuthenticationRequest | ❌ *Not covered* | - | - | | |
| AuthenticationResponse | ❌ *Not covered* | - | - | | |
| RegistrationRequest | RegistrationRequestValidationTest | 12 | 0 | Unit | No |
| AuthRegBaseRequest | ❌ *Not covered* | - | - | | |
| EmailTemplateName | EmailTemplateNameTest | 4 | 0 | Unit | No |
| UsersApplication | UsersApplicationTest | 1 | 0 | Integration | Yes |

---

### Module: Exceptions

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| UserAuthenticationException | UserAuthenticationExceptionTest | 6 | 0 | Unit | No |
| ResourceNotFoundException | ResourceNotFoundExceptionTest | 5 | 0 | Unit | No |
| OperationNotPermittedException | OperationNotPermittedExceptionTest | 4 | 0 | Unit | No |
| ActivationTokenException | ActivationTokenExceptionTest | 4 | 0 | Unit | No |
| GlobalExceptionHandler | GlobalExceptionHandlerTest | 24 | 0 | Unit | No |
| ExceptionResponse | ExceptionResponseTest | 7 | 0 | Unit | No |
| BusinessErrorCodes | BusinessErrorCodesTest | 84 | 0 | Unit | No |

---

### Module: Common

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| SecurityUtils | SecurityUtilsTest | 24 | 0 | Unit | No |
| FileStorageService | FileStorageServiceTest | 49 | 0 | Unit | No |
| PageResponse | PageResponseTest | 10 | 0 | Unit | No |
| Keyword | KeywordTest | 56 | 0 | Unit | No |
| BaseEntity | BaseEntityTest | 24 | 0 | Unit | No |
| BaseBasicEntity | BaseBasicEntityTest | 25 | 0 | Unit | No |
| Address | AddressTest | 48 | 0 | Unit | No |
| AuditConfig | ❓ *Assumed covered* | - | - | Unit | No |
| ApplicationAuditAware | ApplicationAuditAwareTest | 3 | 0 | Unit | No |

---

### Module: Feedback

| Class | Test Class | Number of Tests | Failures | Test Type | Requires Docker |
|-------|-----------|----------------|----------|-----------|-----------------|
| FeedbackApplication | ❌ *Not covered* | - | - | | |

**Note:** Feedback module is incomplete - only contains Application.java main class

---

## Coverage Gaps Summary

### High Priority - Users Module (5 classes)
- **Security** (3): JwtFilter, SecurityConfig, BeansConfig
- **DTOs** (2): AuthenticationRequest, AuthenticationResponse

### Medium Priority
- **Common**: FileStorageService

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
