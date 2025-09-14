package com.example.leader.demo;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class SecretTokenHolder {
    private volatile String token;
    private final Path tokenPath = Paths.get("/etc/token");

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
            System.out.println("✅ Secret token was reloaded successfully.");
        } catch (IOException e) {
            System.err.println("🔥 Failed to reload secret token: " + e.getMessage());
            // Decide how to handle this error - maybe keep the old token?
        }
    }
}
