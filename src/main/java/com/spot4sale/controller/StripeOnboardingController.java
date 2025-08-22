
package com.spot4sale.controller;

import com.spot4sale.service.StripeService;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments/connect")
@RequiredArgsConstructor
public class StripeOnboardingController {
  private final StripeService stripeService;
  @Value("${app.baseUrl:http://localhost:8080}") private String baseUrl;

  @PostMapping("/accounts")
  public String createAccount(@RequestParam String email) throws StripeException {
    Account acct = stripeService.createExpressAccount(email);
    return acct.getId();
  }

  @PostMapping("/accounts/{accountId}/link")
  public String createOnboarding(@PathVariable String accountId) throws StripeException {
    String refresh = baseUrl + "/connect/refresh";
    String ret = baseUrl + "/connect/return";
    AccountLink link = stripeService.createOnboardingLink(accountId, refresh, ret);
    return link.getUrl();
  }
}
