package pl.kufieta.reserveit_api.payments.provider;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class FakePaymentProvider implements PaymentProvider {

    @Override
    public PaymentSession createPayment(BigDecimal amount, String description) {
        String externalId = "fake_" + UUID.randomUUID();
        return new PaymentSession(externalId, "http://localhost:8080/fake-pay/" + externalId);
    }
}
