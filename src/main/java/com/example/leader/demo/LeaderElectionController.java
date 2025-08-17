package com.example.leader.demo;

import org.springframework.context.event.EventListener;
import org.springframework.integration.leader.event.OnGrantedEvent;
import org.springframework.integration.leader.event.OnRevokedEvent;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderElectionController {

    private final LeaderElectionService leaderElectionService;

    public LeaderElectionController(LeaderElectionService leaderElectionService) {
        this.leaderElectionService = leaderElectionService;
    }

    @GetMapping("/leader")
    public String getLeader() {
        if (leaderElectionService.isLeader()) {
            return "The current leader is... me!";
        } else {
            return "I am not the leader.";
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
