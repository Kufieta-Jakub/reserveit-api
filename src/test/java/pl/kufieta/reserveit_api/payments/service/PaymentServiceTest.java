package pl.kufieta.reserveit_api.payments.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.kufieta.reserveit_api.common.ConflictException;
import pl.kufieta.reserveit_api.common.NotFoundException;
import pl.kufieta.reserveit_api.facilities.entity.Facility;
import pl.kufieta.reserveit_api.notifications.ReservationConfirmedEvent;
import pl.kufieta.reserveit_api.payments.dto.PaymentWebhookRequest;
import pl.kufieta.reserveit_api.payments.entity.Payment;
import pl.kufieta.reserveit_api.payments.entity.PaymentStatus;
import pl.kufieta.reserveit_api.payments.provider.PaymentProvider;
import pl.kufieta.reserveit_api.payments.repository.PaymentRepository;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;
import pl.kufieta.reserveit_api.users.entity.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {
    private static final String EXTERNAL_ID = "fake_123";

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private PaymentProvider paymentProvider;
    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private PaymentService paymentService;

    private Reservation reservation;
    private Payment payment;

    @BeforeEach
    void setUp() {
        Facility facility = mock(Facility.class);
        User user = mock(User.class);
        lenient().when(facility.getName()).thenReturn("Kort 1");
        lenient().when(user.getEmail()).thenReturn("jakub@test.pl");

        reservation = new Reservation(facility, user,
                LocalDateTime.of(2026, 11, 20, 10, 0),
                LocalDateTime.of(2026, 11, 20, 11, 0));
        payment = new Payment(reservation, new BigDecimal("97.00"), EXTERNAL_ID);

        lenient().when(paymentRepository.findByExternalId(EXTERNAL_ID))
                .thenReturn(Optional.of(payment));
    }

    @Test
    void paidWebhook_confirmsReservationAndPublishesEvent() {
        paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.PAID));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);

        ArgumentCaptor<ReservationConfirmedEvent> captor =
                ArgumentCaptor.forClass(ReservationConfirmedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().userEmail()).isEqualTo("jakub@test.pl");
        assertThat(captor.getValue().facilityName()).isEqualTo("Kort 1");
        assertThat(captor.getValue().amount()).isEqualByComparingTo("97.00");
    }

    @Test
    void failedWebhook_cancelsReservationAndSendsNoEmail() {
        paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.FAILED));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void repeatedWebhook_isIgnored() {
        paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.PAID));
        paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.PAID));

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        verify(eventPublisher, times(1)).publishEvent(any(ReservationConfirmedEvent.class));
    }

    @Test
    void unknownExternalId_throwsNotFound() {
        when(paymentRepository.findByExternalId("nope")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                paymentService.handleWebhook(new PaymentWebhookRequest("nope", PaymentStatus.PAID)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void paidWebhook_forCancelledReservation_throwsConflict() {
        reservation.cancel(); // np. wygasla, zanim przyszedł webhook

        assertThatThrownBy(() ->
                paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.PAID)))
                .isInstanceOf(ConflictException.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.PENDING);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void unsupportedStatus_throwsIllegalArgument() {
        assertThatThrownBy(() ->
                paymentService.handleWebhook(new PaymentWebhookRequest(EXTERNAL_ID, PaymentStatus.PENDING)))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
