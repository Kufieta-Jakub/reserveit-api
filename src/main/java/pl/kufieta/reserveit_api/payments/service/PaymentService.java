package pl.kufieta.reserveit_api.payments.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.kufieta.reserveit_api.common.ConflictException;
import pl.kufieta.reserveit_api.common.NotFoundException;
import pl.kufieta.reserveit_api.payments.dto.PaymentInfo;
import pl.kufieta.reserveit_api.payments.dto.PaymentWebhookRequest;
import pl.kufieta.reserveit_api.payments.entity.Payment;
import pl.kufieta.reserveit_api.payments.entity.PaymentStatus;
import pl.kufieta.reserveit_api.payments.provider.PaymentProvider;
import pl.kufieta.reserveit_api.payments.provider.PaymentSession;
import pl.kufieta.reserveit_api.payments.repository.PaymentRepository;
import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;

import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentProvider paymentProvider;

    public PaymentService(PaymentRepository paymentRepository, PaymentProvider paymentProvider) {
        this.paymentRepository = paymentRepository;
        this.paymentProvider = paymentProvider;
    }

    @Transactional
    public PaymentInfo createForReservation(Reservation reservation, BigDecimal amount) {
        PaymentSession session = paymentProvider.createPayment(
                amount, "Reservation #" + reservation.getId());
        paymentRepository.save(new Payment(reservation, amount, session.externalId()));
        return new PaymentInfo(session.externalId(), session.paymentUrl(), amount);
    }

    @Transactional
    public void handleWebhook(PaymentWebhookRequest request) {
        Payment payment = paymentRepository.findByExternalId(request.externalId())
                .orElseThrow(() -> new NotFoundException("Payment not found"));

        if (payment.getStatus() != PaymentStatus.PENDING) {
            return; // webhook dwa razy moze sie pojawic wiec ignorujemy drugiego
        }

        Reservation reservation = payment.getReservation();
        switch (request.status()) {
            case PAID -> {
                if (reservation.getStatus() != ReservationStatus.PENDING) {
                    throw new ConflictException("Reservation is no longer awaiting payment");
                }
                payment.markPaid();
                reservation.confirm();
            }
            case FAILED -> {
                payment.markFailed();
                reservation.cancel();
            }
            default -> throw new IllegalArgumentException(
                    "Unsupported payment status: " + request.status());
        }
    }
}
