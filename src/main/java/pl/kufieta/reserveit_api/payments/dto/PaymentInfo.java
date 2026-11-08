package pl.kufieta.reserveit_api.payments.dto;

import java.math.BigDecimal;

public record PaymentInfo(String externalId, String paymentUrl, BigDecimal amount) {}