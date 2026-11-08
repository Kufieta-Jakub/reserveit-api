package pl.kufieta.reserveit_api.users.dto;

import pl.kufieta.reserveit_api.users.entity.User;
import pl.kufieta.reserveit_api.users.entity.Role;

public record UserResponse(Long id, String email, Role role) {
    public static UserResponse from(User user) {
        return new UserResponse(user.getId(),user.getEmail(),user.getRole());
    }
}
