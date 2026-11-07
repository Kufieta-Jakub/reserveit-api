package pl.kufieta.reserveit_api.reservations.dto;

import pl.kufieta.reserveit_api.reservations.entity.Reservation;
import pl.kufieta.reserveit_api.reservations.entity.ReservationStatus;

import java.time.LocalDateTime;

public record ReservationResponse (
        Long id,
        Long facilityId,
        String facilityName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        ReservationStatus status,
        LocalDateTime expiresAt
) {
    public static ReservationResponse from (Reservation reservation) {
        return new ReservationResponse(
            reservation.getId(),
            reservation.getFacility().getId(),
            reservation.getFacility().getName(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            reservation.getStatus(),
            reservation.getExpiresAt());
    }
}