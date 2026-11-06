package pl.kufieta.reserveit_api.reservations.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record CreateReservationRequest(
    @NotNull Long facilityId,
    @NotNull @Future LocalDateTime startTime,
    @NotNull LocalDateTime endTime
){}
