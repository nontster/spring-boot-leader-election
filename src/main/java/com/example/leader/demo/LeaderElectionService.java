package com.example.leader.demo;

import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.stereotype.Service;

@Service
public class LeaderElectionService {

    private final AtomicBoolean isLeader = new AtomicBoolean(false);
    private final ApplicationEventPublisher publisher;

    public LeaderElectionService(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public boolean isLeader() {
        return isLeader.get();
    }

    public void onGranted(OnGrantedEvent event) {
        isLeader.set(true);
        // You can add any logic here that should be executed when this instance becomes the leader
    }

    public void onRevoked(OnRevokedEvent event) {
        isLeader.set(false);
        // You can add any logic here that should be executed when this instance is no longer the leader
    }
}
