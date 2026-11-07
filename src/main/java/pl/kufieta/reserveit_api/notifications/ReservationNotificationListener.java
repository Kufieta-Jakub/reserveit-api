package pl.kufieta.reserveit_api.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ReservationNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(ReservationNotificationListener.class);

    private final JavaMailSender mailSender;
    private final String from;

    public ReservationNotificationListener(JavaMailSender mailSender,
                                           @Value("${app.mail.from}") String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationConfirmed(ReservationConfirmedEvent event) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(event.userEmail());
        message.setSubject("Reservation confirmed: " + event.facilityName());
        message.setText("Your reservation is confirmed.\n\n"
                + "Facility: " + event.facilityName() + "\n"
                + "From: " + event.startTime() + "\n"
                + "To: " + event.endTime() + "\n"
                + "Paid: " + event.amount() + " PLN");
        try {
            mailSender.send(message);
            log.info("Confirmation e-mail sent to {}", event.userEmail());
        } catch (MailException e) {
            log.error("Could not send confirmation e-mail to {}", event.userEmail(), e);
        }
    }
}
