package com.example.leader.demo;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SecretFileWatcher {
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
        Path watchPath = Paths.get("/etc/");

        watchPath.register(
                watchService,
                StandardWatchEventKinds.ENTRY_MODIFY,
                StandardWatchEventKinds.ENTRY_CREATE
        );

        // Start the watching process in a separate thread
        executorService.submit(() -> {
            System.out.println("🔍 Starting file watcher for secret token...");
            try {
                WatchKey key;
                while ((key = watchService.take()) != null) {
                    for (WatchEvent<?> event : key.pollEvents()) {
                        // The event context is the filename, e.g., "token"
                        // Kubernetes updates ..data, which is a symlink. This triggers an event.
                        System.out.println("Detected event for: " + event.context());
                        // We can just reload the token regardless of the specific file event
                        tokenHolder.updateToken();
                    }
                    key.reset();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                System.out.println("File watcher interrupted.");
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
                System.err.println("Error closing watch service: " + e.getMessage());
            }
        }
    }
}
