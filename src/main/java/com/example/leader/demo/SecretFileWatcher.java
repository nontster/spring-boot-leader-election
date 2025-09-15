package com.example.leader.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SecretFileWatcher {
    private static final Logger LOGGER = LoggerFactory.getLogger(SecretFileWatcher.class);
    private final SecretTokenHolder tokenHolder;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private WatchService watchService;

    // Inject the token holder
    public SecretFileWatcher(SecretTokenHolder tokenHolder) {
        this.tokenHolder = tokenHolder;
    }

    @PostConstruct
    public void startWatching() throws IOException {
        watchService = FileSystems.getDefault().newWatchService();
        // Watch the DIRECTORY where the token is located
        Path watchPath = Paths.get("/etc/token");

        watchPath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE
        );

        // Start the watching process in a separate thread
        executorService.submit(() -> {
            LOGGER.info("🔍 Starting file watcher for secret token...");
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        // The event context is the filename, e.g., "token"
                        // Kubernetes updates ..data, which is a symlink. This triggers an event.
                        LOGGER.info("Detected event for: " + event.context());
                        // We can just reload the token regardless of the specific file event
                        tokenHolder.updateToken();
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error("File watcher interrupted.");
            }
        });
    }

    @PreDestroy
    public void stopWatching() {
        executorService.shutdownNow();
        if (watchService != null) {
            try {
                watchService.close();
            } catch (IOException e) {
                LOGGER.error("Error closing watch service: " + e.getMessage());
            }
        }
    }
}
