package pl.kufieta.reserveit_api.payments.controller;


import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import pl.kufieta.reserveit_api.payments.dto.PaymentWebhookRequest;
import pl.kufieta.reserveit_api.payments.service.PaymentService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final String webhookSecret;
    public PaymentController(PaymentService paymentService, @Value("${app.payments.webhook-secret}")String webhookSecret) {
        this.paymentService = paymentService;
        this.webhookSecret = webhookSecret;
    }

    @PostMapping("/webhook")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void webhook(@RequestHeader("X-Webhook-Secret") String secret,
                        @Valid @RequestBody PaymentWebhookRequest request) {
        boolean valid = MessageDigest.isEqual(
                secret.getBytes(StandardCharsets.UTF_8),
                webhookSecret.getBytes(StandardCharsets.UTF_8));
        if (!valid) {
            throw new BadCredentialsException("Invalid webhook secret");
        }
        paymentService.handleWebhook(request);
    }
}
