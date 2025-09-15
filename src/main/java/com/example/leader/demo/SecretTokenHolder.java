package com.example.leader.demo;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SecretTokenHolder {
    private volatile String token;
    private final Path tokenPath = Paths.get("/etc/token/accessToken");
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretTokenHolder.class);

    @PostConstruct
    public void init() {
        // Load the initial token on startup
        updateToken();
    }

    public String getToken() {
        return this.token;
    }

    // This method will be called by the file watcher
    public void updateToken() {
        try {
            String newToken = new String(Files.readAllBytes(tokenPath));
            this.token = newToken;
            LOGGER.info("✅ Secret token was reloaded successfully.");
        } catch (IOException e) {
            LOGGER.error("🔥 Failed to reload secret token: " + e.getMessage());
            // Decide how to handle this error - maybe keep the old token?
        }
    }
}
