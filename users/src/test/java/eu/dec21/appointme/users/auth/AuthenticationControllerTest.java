package eu.dec21.appointme.users.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import eu.dec21.appointme.users.security.JwtService;
import eu.dec21.appointme.users.security.UserDetailsServiceImpl;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthenticationController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private AuthenticationService authService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;
    @MockitoBean
    private AuthenticationProvider authenticationProvider;
    @MockitoBean
    private eu.dec21.appointme.users.roles.repository.RoleRepository roleRepository;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    // === POST /auth/register ===

    @Test
    void register_validRequest_returns202() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted());

        verify(authService).register(any(RegistrationRequest.class));
    }

    @Test
    void register_missingEmail_returns400() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .password("Pass123!@")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_blankFirstName_returns400() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("").lastName("Doe")
                .email("john@example.com").password("Pass123!@")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_weakPassword_returns400() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("john@example.com").password("weak")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegistrationRequest request = RegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .email("not-an-email").password("Pass123!@")
                .build();

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // === POST /auth/authenticate ===

    @Test
    void authenticate_validRequest_returns200WithToken() throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("john@example.com").password("Pass123!@")
                .build();
        AuthenticationResponse response = AuthenticationResponse.builder().token("jwt-token").build();
        when(authService.authenticate(any())).thenReturn(response);

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-token"));
    }

    @Test
    void authenticate_missingPassword_returns400() throws Exception {
        String json = "{\"email\":\"john@example.com\"}";

        mockMvc.perform(post("/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest());
    }

    // === GET /auth/verify-account ===

    @Test
    void verifyAccount_validToken_returns200() throws Exception {
        mockMvc.perform(get("/auth/verify-account").param("token", "abc123"))
                .andExpect(status().isOk());

        verify(authService).activateAccount("abc123");
    }

    @Test
    void verifyAccount_missingToken_returns400() throws Exception {
        mockMvc.perform(get("/auth/verify-account"))
                .andExpect(status().isBadRequest());
    }
}
