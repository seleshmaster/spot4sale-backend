package com.spot4sale.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "stripe")
public class StripeProperties {
    /** sk_test_... */
    private String secretKey;
    /** whsec_... from your Stripe webhook endpoint (test mode is fine) */
    private String webhookSecret;
    /** true if you want to use Connect (application_fee + transfer_data) */
    private boolean connectEnabled = true;
    /** Platform fee percent (e.g., 10 = 10%) */
    private int appFeePercent = 10;

}
