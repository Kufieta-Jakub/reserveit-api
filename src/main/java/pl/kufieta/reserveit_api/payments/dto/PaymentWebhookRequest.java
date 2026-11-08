package pl.kufieta.reserveit_api.payments.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.kufieta.reserveit_api.payments.entity.PaymentStatus;

public record PaymentWebhookRequest(
        @NotBlank String externalId,
        @NotNull PaymentStatus status
) {
}
