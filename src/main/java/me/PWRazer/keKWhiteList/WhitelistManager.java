package me.PWRazer.keKWhiteList;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class WhitelistManager {
    private final Set<String> whitelistedPlayers = new HashSet<>();
    private final Map<String, Instant> tempWhitelistedPlayers = new HashMap<>();

    public boolean isWhitelisted(String username) {
        return whitelistedPlayers.contains(username.toLowerCase());
    }

    public boolean isTempWhitelisted(String username) {
        Instant expiry = tempWhitelistedPlayers.get(username.toLowerCase());
        return expiry != null && expiry.isAfter(Instant.now());
    }

    public void addPlayer(String username) {
        whitelistedPlayers.add(username.toLowerCase());
    }

    public void addTempPlayer(String username, Duration duration) {
        tempWhitelistedPlayers.put(username.toLowerCase(), Instant.now().plus(duration));
    }

    public boolean removePlayer(String username) {
        return whitelistedPlayers.remove(username.toLowerCase());
    }

    public boolean removeTempPlayer(String username) {
        return tempWhitelistedPlayers.remove(username.toLowerCase()) != null;
    }

    public void cleanupTemporaryWhitelist() {
        Instant now = Instant.now();
        tempWhitelistedPlayers.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }

    public Set<String> getWhitelistedPlayers() {
        return new HashSet<>(whitelistedPlayers);
    }

    public Map<String, Instant> getTempWhitelistedPlayers() {
        return new HashMap<>(tempWhitelistedPlayers);
    }

    public void setTempWhitelistDuration(String username, Duration duration) {
        if (tempWhitelistedPlayers.containsKey(username.toLowerCase())) {
            tempWhitelistedPlayers.put(username.toLowerCase(), Instant.now().plus(duration));
        }
    }
}