package com.example.leader.demo;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Component("token")
public class TokenReadinessProbe implements HealthIndicator {

    private final SecretTokenHolder secretTokenHolder;

    public TokenReadinessProbe(SecretTokenHolder secretTokenHolder) {
        this.secretTokenHolder = secretTokenHolder;
    }

    @Override
    public Health health() {
        if (secretTokenHolder.getToken() != null) {
            return Health.up().withDetail("token", "available").build();
        }
        return Health.down().withDetail("token", "not available").build();
    }
}
