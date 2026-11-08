package pl.kufieta.reserveit_api.users.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.kufieta.reserveit_api.security.JwtService;
import pl.kufieta.reserveit_api.users.dto.LoginRequest;
import pl.kufieta.reserveit_api.users.dto.LoginResponse;
import pl.kufieta.reserveit_api.users.dto.RegisterRequest;
import pl.kufieta.reserveit_api.users.dto.UserResponse;
import pl.kufieta.reserveit_api.users.entity.Role;
import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    @Test
    void register_savesUserWithHashedPassword() {
        RegisterRequest registerRequest = new RegisterRequest("jakub@test.pl","haslo1234");
        when(userRepository.existsByEmail("jakub@test.pl")).thenReturn(false);
        when(passwordEncoder.encode("haslo1234")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponse response = userService.registerUser(registerRequest);

        assertEquals("jakub@test.pl",response.email());
        assertEquals(Role.USER, response.role());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("hashed",captor.getValue().getPasswordHash());
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest registerRequest = new RegisterRequest("jakub@test.pl","haslo1234");
        when(userRepository.existsByEmail("jakub@test.pl")).thenReturn(true);

        assertThrows(IllegalArgumentException.class,()->userService.registerUser(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void login_throwsWjenPasswordIsWrong() {
        User user = new User("jakub@test.pl","hashed", Role.USER);
        when(userRepository.findByEmail("jakub@test.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("zlehaslo","hashed")).thenReturn(false);

        assertThrows(BadCredentialsException.class,
                () -> userService.login(new LoginRequest("jakub@test.pl","zlehaslo")));
    }

    @Test
    void login_throwsWhenUserDoesNotExist() {
        when(userRepository.findByEmail("babajaga@test.pl")).thenReturn(Optional.empty());

        assertThrows(BadCredentialsException.class,
                ()->userService.login(new LoginRequest("babajaga@test.pl","babajaga")));
    }

    @Test
    void login_returnsTokenForValidCredentials() {
        User user = new User("jakub@test.pl", "hashed", Role.USER);
        when(userRepository.findByEmail("jakub@test.pl")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("haslo1234", "hashed")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("token123");

        LoginResponse response = userService.login(new LoginRequest("jakub@test.pl", "haslo1234"));

        assertEquals("token123", response.token());
    }

}

