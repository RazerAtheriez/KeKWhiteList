package me.PWRazer.keKWhiteList;

import java.util.HashSet;
import java.util.Set;

public class WhitelistManager {
    private final Set<String> whitelistedPlayers = new HashSet<>();

    public boolean isWhitelisted(String username) {
        return whitelistedPlayers.contains(username.toLowerCase());
    }

    public void addPlayer(String username) {
        whitelistedPlayers.add(username.toLowerCase());
    }

    public boolean removePlayer(String username) {
        return whitelistedPlayers.remove(username.toLowerCase());
    }

    public Set<String> getWhitelistedPlayers() {
        return new HashSet<>(whitelistedPlayers);
    }
}