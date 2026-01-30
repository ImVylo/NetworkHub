package com.hytale.networkhub.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;

public class RedisMessageHandler {
    private final HytaleLogger logger;
    private final Gson gson;

    public RedisMessageHandler(HytaleLogger logger, Gson gson) {
        this.logger = logger;
        this.gson = gson;
    }

    public void handlePlayerJoin(String json) {
        try {
            Map<String, Object> message = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
            String playerName = (String) message.get("playerName");
            String serverId = (String) message.get("serverId");

            logger.at(Level.FINE).log("Player " + playerName + " joined " + serverId + " (via Redis)");

            // TODO: Update local cache, trigger events, etc.

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error handling player join message: " + e.getMessage());
        }
    }

    public void handlePlayerQuit(String json) {
        try {
            Map<String, Object> message = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
            String playerName = (String) message.get("playerName");
            String serverId = (String) message.get("serverId");

            logger.at(Level.FINE).log("Player " + playerName + " quit " + serverId + " (via Redis)");

            // TODO: Update local cache, trigger events, etc.

        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error handling player quit message: " + e.getMessage());
        }
    }
}
