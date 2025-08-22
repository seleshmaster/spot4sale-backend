package com.spot4sale.controller;

import com.spot4sale.dto.InitPaymentRequest;
import com.spot4sale.service.PaymentService;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
public class PaymentsController {

    private final PaymentService payments;

    public PaymentsController(PaymentService payments) {
        this.payments = payments;
    }

    /** POST /api/payments/intent → { clientSecret } */
    @PostMapping("/intent")
    public Map<String, String> createIntent(@RequestBody InitPaymentRequest req, Authentication auth) {
        return payments.createIntentForBooking(req, auth);
    }
}
