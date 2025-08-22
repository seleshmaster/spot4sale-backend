
package com.spot4sale.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.PaymentIntent;
import com.stripe.net.RequestOptions;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import com.stripe.param.PaymentIntentCreateParams;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StripeService {

  @Value("${stripe.secret-key}") private String secretKey;
  @Value("${stripe.platformFeePercent}") private Integer platformFeePercent;

  private void init(){ Stripe.apiKey = secretKey; }

  public Account createExpressAccount(String email) throws StripeException {
    init();
    AccountCreateParams params = AccountCreateParams.builder()
      .setType(AccountCreateParams.Type.EXPRESS)
      .setEmail(email)
      .build();
    return Account.create(params);
  }

  public AccountLink createOnboardingLink(String accountId, String refreshUrl, String returnUrl) throws StripeException {
    init();
    AccountLinkCreateParams params = AccountLinkCreateParams.builder()
      .setAccount(accountId)
      .setRefreshUrl(refreshUrl)
      .setReturnUrl(returnUrl)
      .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
      .build();
    return AccountLink.create(params);
  }

  public PaymentIntent createDirectCharge(String connectedAccountId, long amountCents, String currency) throws StripeException {
    init();
    long fee = (amountCents * platformFeePercent) / 100;
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
      .setAmount(amountCents)
      .setCurrency(currency)
      .setAutomaticPaymentMethods(
        PaymentIntentCreateParams.AutomaticPaymentMethods.builder().setEnabled(true).build())
      .setApplicationFeeAmount(fee)
      .build();
    RequestOptions req = RequestOptions.builder().setStripeAccount(connectedAccountId).build();
    return PaymentIntent.create(params, req);
  }
}
