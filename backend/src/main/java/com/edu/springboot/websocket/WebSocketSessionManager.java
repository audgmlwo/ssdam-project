package com.edu.springboot.websocket;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebSocketSessionManager {
    private final Set<String> connectedUsers = ConcurrentHashMap.newKeySet();

    public void addUser(String email) {
        connectedUsers.add(email);
    }

    public void removeUser(String email) {
        connectedUsers.remove(email);
    }

    public Set<String> getConnectedUsers() {
        return connectedUsers;
    }
}