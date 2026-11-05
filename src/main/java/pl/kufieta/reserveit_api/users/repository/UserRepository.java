package pl.kufieta.reserveit_api.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kufieta.reserveit_api.users.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
}
