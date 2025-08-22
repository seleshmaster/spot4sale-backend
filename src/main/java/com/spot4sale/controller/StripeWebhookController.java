package com.spot4sale.controller;

import com.spot4sale.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
public class StripeWebhookController {

    private final PaymentService payments;

    public StripeWebhookController(PaymentService payments) {
        this.payments = payments;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handle(@RequestHeader("Stripe-Signature") String sig,
                                         @RequestBody String payload) {
        // Delegate all logic (signature verify + markPaid) to the service
        payments.handleWebhook(payload, sig);
        // Stripe expects 2xx to stop retries; returning OK if processed or already handled
        return ResponseEntity.ok("ok");
    }
}
