package pl.kufieta.reserveit_api.payments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kufieta.reserveit_api.payments.entity.Payment;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByExternalId(String externalId);
    Optional<Payment> findByReservationId(Long reservationId);

}
