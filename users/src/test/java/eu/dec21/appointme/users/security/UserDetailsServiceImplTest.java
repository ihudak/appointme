package eu.dec21.appointme.users.security;

import eu.dec21.appointme.users.users.entity.User;
import eu.dec21.appointme.users.users.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_existingUser_returnsUserDetails() {
        User user = User.builder()
                .email("test@example.com")
                .password("encoded")
                .firstName("Test")
                .lastName("User")
                .build();
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("test@example.com");

        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("test@example.com");
    }

    @Test
    void loadUserByUsername_nonExistentUser_throwsUsernameNotFoundException() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("unknown@example.com"))
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("unknown@example.com");
    }

    @Test
    void loadUserByUsername_returnsUserEntity() {
        User user = User.builder()
                .email("admin@test.com")
                .password("pass")
                .build();
        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("admin@test.com");

        assertThat(result).isInstanceOf(User.class);
    }
}
