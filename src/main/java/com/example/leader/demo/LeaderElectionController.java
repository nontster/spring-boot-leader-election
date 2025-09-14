package com.example.leader.demo;

import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderElectionController {

    private final LeaderElectionService leaderElectionService;
    private final SecretTokenHolder tokenHolder;

    public LeaderElectionController(LeaderElectionService leaderElectionService, SecretTokenHolder tokenHolder) {
        this.leaderElectionService = leaderElectionService;
        this.tokenHolder = tokenHolder;
    }

    @GetMapping("/leader")
    public String getLeader() {
        if (leaderElectionService.isLeader()) {
            return "The current leader is... me!" + " token:" + tokenHolder.getToken();
        } else {
            return "I am not the leader." + " token:" + tokenHolder.getToken();
        }
    }

    @EventListener
    public void onGranted(OnGrantedEvent event) {
        leaderElectionService.onGranted(event);
    }

    @EventListener
    public void onRevoked(OnRevokedEvent event) {
        leaderElectionService.onRevoked(event);
    }
}
