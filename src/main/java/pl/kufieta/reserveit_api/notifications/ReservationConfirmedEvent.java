package pl.kufieta.reserveit_api.notifications;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReservationConfirmedEvent(
        String userEmail,
        String facilityName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal amount
) {}