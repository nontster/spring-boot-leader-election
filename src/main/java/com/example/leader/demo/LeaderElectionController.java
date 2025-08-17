package com.example.leader.demo;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class LeaderElectionController {

    @GetMapping("/leader")
    public String getLeader() {
        return "The current leader is... me!";
    }
}
