package pl.kufieta.reserveit_api.payments.provider;

import java.math.BigDecimal;

public interface PaymentProvider {
    PaymentSession createPayment(BigDecimal amount, String description);
}
