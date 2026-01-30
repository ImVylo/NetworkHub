package com.hytale.networkhub.redis;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.Map;
import java.util.logging.Logger;

public class RedisMessageHandler {
    private final Logger logger;
    private final Gson gson;

    public RedisMessageHandler(Logger logger, Gson gson) {
        this.logger = logger;
        this.gson = gson;
    }

    public void handlePlayerJoin(String json) {
        try {
            Map<String, Object> message = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
            String playerName = (String) message.get("playerName");
            String serverId = (String) message.get("serverId");

            logger.fine("Player " + playerName + " joined " + serverId + " (via Redis)");

            // TODO: Update local cache, trigger events, etc.

        } catch (Exception e) {
            logger.warning("Error handling player join message: " + e.getMessage());
        }
    }

    public void handlePlayerQuit(String json) {
        try {
            Map<String, Object> message = gson.fromJson(json, new TypeToken<Map<String, Object>>(){}.getType());
            String playerName = (String) message.get("playerName");
            String serverId = (String) message.get("serverId");

            logger.fine("Player " + playerName + " quit " + serverId + " (via Redis)");

            // TODO: Update local cache, trigger events, etc.

        } catch (Exception e) {
            logger.warning("Error handling player quit message: " + e.getMessage());
        }
    }
}
