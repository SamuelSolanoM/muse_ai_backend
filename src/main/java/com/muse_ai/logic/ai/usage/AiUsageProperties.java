package com.muse_ai.logic.ai.usage;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "ai.usage")
public class AiUsageProperties {

    /**
     * Límite mensual de tokens permitidos. Usado para calcular alertas.
     */
    private long tokenLimit = 100_000;

    /**
     * Umbral para disparar alerta (0.8 = 80%).
     */
    private double alertThreshold = 0.8;

    public long getTokenLimit() {
        return tokenLimit;
    }

    public void setTokenLimit(long tokenLimit) {
        this.tokenLimit = tokenLimit;
    }

    public double getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(double alertThreshold) {
        this.alertThreshold = alertThreshold;
    }
}
