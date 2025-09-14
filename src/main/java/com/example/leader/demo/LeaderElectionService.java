package com.example.leader.demo;

import java.util.Base64;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import io.fabric8.kubernetes.api.model.ObjectMetaBuilder;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.stereotype.Service;

@Service
public class LeaderElectionService {
    private static final Logger LOGGER = LoggerFactory.getLogger(LeaderElectionService.class);
    private static final String SECRET_NAME = "shared-access-token";
    private static final String SECRET_KEY = "token";

    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private final ApplicationEventPublisher eventPublisher;

    @Autowired
    private KubernetesClient kubernetesClient; // Autowire the K8s client

    public LeaderElectionService(ApplicationEventPublisher publisher) {
        this.eventPublisher = publisher;
    }

    public boolean isLeader() {
        return isLeader.get();
    }


    public void onGranted(OnGrantedEvent event) {
        isLeader.set(true);
        // You can add any logic here that should be executed when this instance becomes the leader
        LOGGER.info("👑 I am the leader! Fetching and sharing the token.");
        try {
            // 1. Fetch the token from your auth service
            String accessToken = fetchTokenFromAuthService();

            // 2. Write the token to a Kubernetes Secret
            // The data in a secret must be Base64 encoded.
            String encodedToken = Base64.getEncoder().encodeToString(accessToken.getBytes());

            kubernetesClient.secrets()
                    .inNamespace(kubernetesClient.getNamespace()) // Use the pod's current namespace
                    .resource(new SecretBuilder()
                            .withMetadata(new ObjectMetaBuilder()
                                    .withName(SECRET_NAME)
                                    .build())
                            .withData(Map.of(SECRET_KEY, encodedToken))
                            .build())
                    .serverSideApply(); // Creates if not exists, updates if it does

            LOGGER.info("✅ Successfully shared token in Secret '{}'", SECRET_NAME);

        } catch (Exception e) {
            LOGGER.error("Failed to fetch or share token", e);
            // Consider revoking leadership or retrying
        }
    }

    public void onRevoked(OnRevokedEvent event) {
        isLeader.set(false);
        // You can add any logic here that should be executed when this instance is no longer the leader
    }

    private String fetchTokenFromAuthService() {
        // Replace with your actual token fetching logic
        LOGGER.info("Calling authentication service to get a new token...");
        return "fake-token-" + System.currentTimeMillis();
    }
}
