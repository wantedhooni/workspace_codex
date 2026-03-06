package com.example.dynamicjob.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service("userService")
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public void syncUsers(String source, Boolean fullSync) {
        log.info("syncUsers called. source={}, fullSync={}", source, fullSync);
    }
}
