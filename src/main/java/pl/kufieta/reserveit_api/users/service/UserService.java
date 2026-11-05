package pl.kufieta.reserveit_api.users.service;


import jdk.jshell.spi.ExecutionControl;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.security.JwtService;
import pl.kufieta.reserveit_api.users.dto.LoginRequest;
import pl.kufieta.reserveit_api.users.dto.LoginResponse;
import pl.kufieta.reserveit_api.users.dto.RegisterRequest;
import pl.kufieta.reserveit_api.users.dto.UserResponse;
import pl.kufieta.reserveit_api.users.entity.Role;
import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,  JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse registerUser(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.email())){
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User(
                registerRequest.email(),
                passwordEncoder.encode(registerRequest.password()),
                Role.USER);
        return UserResponse.from(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));
        if(!passwordEncoder.matches(loginRequest.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid password");
        }
        return new LoginResponse(jwtService.generateToken(user));
    }
}
