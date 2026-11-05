package pl.kufieta.reserveit_api.users.service;


import jdk.jshell.spi.ExecutionControl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.users.dto.RegisterRequest;
import pl.kufieta.reserveit_api.users.dto.UserResponse;
import pl.kufieta.reserveit_api.users.entity.Role;
import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.repository.UserRepository;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
