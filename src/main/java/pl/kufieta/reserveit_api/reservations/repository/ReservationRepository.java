package pl.kufieta.reserveit_api.reservations.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
