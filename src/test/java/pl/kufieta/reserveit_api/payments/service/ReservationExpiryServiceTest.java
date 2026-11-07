package pl.kufieta.reserveit_api.payments.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.payments.entity.Payment;
import pl.kufieta.reserveit_api.payments.entity.PaymentStatus;
import pl.kufieta.reserveit_api.payments.repository.PaymentRepository;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;
import pl.kufieta.reserveit_api.reservations.repository.ReservationRepository;
import pl.kufieta.reserveit_api.reservations.service.ReservationExpiryService;
import pl.kufieta.reserveit_api.users.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReservationExpiryServiceTest {

    @Mock
    private ReservationRepository reservationRepository;
    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private ReservationExpiryService expiryService;

    private Reservation newReservation(long id) {
        Reservation reservation = new Reservation(mock(Facility.class), mock(User.class),
                LocalDateTime.of(2026, 11, 20, 10, 0),
                LocalDateTime.of(2026, 11, 20, 11, 0));
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    @Test
    void expireOverdue_cancelsReservationAndFailsPendingPayment() {
        Reservation reservation = newReservation(1L);
        Payment payment = new Payment(reservation, new BigDecimal("97.00"), "fake_1");
        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));
        when(paymentRepository.findByReservationId(1L)).thenReturn(Optional.of(payment));

        int expired = expiryService.expireOverdue();

        assertThat(expired).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
    }

    @Test
    void expireOverdue_doesNotTouchAlreadyPaidPayment() {
        Reservation reservation = newReservation(2L);
        Payment payment = new Payment(reservation, new BigDecimal("97.00"), "fake_2");
        payment.markPaid();
        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));
        when(paymentRepository.findByReservationId(2L)).thenReturn(Optional.of(payment));

        expiryService.expireOverdue();

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
    }

    @Test
    void expireOverdue_withoutPayment_stillCancelsReservation() {
        Reservation reservation = newReservation(3L);
        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of(reservation));
        when(paymentRepository.findByReservationId(3L)).thenReturn(Optional.empty());

        int expired = expiryService.expireOverdue();

        assertThat(expired).isEqualTo(1);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void expireOverdue_withNothingOverdue_returnsZero() {
        when(reservationRepository.findByStatusAndExpiresAtBefore(
                eq(ReservationStatus.PENDING), any(LocalDateTime.class)))
                .thenReturn(List.of());

        assertThat(expiryService.expireOverdue()).isZero();
    }
}