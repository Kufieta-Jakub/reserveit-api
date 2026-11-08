package pl.kufieta.reserveit_api.reservations.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.payments.entity.PaymentStatus;
import pl.kufieta.reserveit_api.payments.repository.PaymentRepository;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;
import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReservationExpiryService {
    private final ReservationRepository reservationRepository;
    private final PaymentRepository paymentRepository;

    public ReservationExpiryService(ReservationRepository reservationRepository,
                                    PaymentRepository paymentRepository) {
        this.reservationRepository = reservationRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public int expireOverdue() {
        List<Reservation> overdue = reservationRepository
                .findByStatusAndExpiresAtBefore(ReservationStatus.PENDING, LocalDateTime.now());

        for (Reservation reservation : overdue) {
            reservation.cancel();
            paymentRepository.findByReservationId(reservation.getId())
                    .filter(p -> p.getStatus() == PaymentStatus.PENDING)
                    .ifPresent(p -> p.markFailed());
        }
        return overdue.size();
    }
}
