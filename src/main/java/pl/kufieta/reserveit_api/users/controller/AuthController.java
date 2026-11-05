package pl.kufieta.reserveit_api.users.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import pl.kufieta.reserveit_api.users.dto.RegisterRequest;
import pl.kufieta.reserveit_api.users.dto.UserResponse;
import pl.kufieta.reserveit_api.users.service.UserService;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        return userService.registerUser(registerRequest);
    }
}
