package pl.kufieta.reserveit_api.reservations.dto;

import pl.kufieta.reserveit_api.payments.dto.PaymentInfo;

public record ReservationCreatedResponse(ReservationResponse reservation, PaymentInfo payment) {}