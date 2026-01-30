package com.hytale.networkhub.tasks;

import com.hytale.networkhub.managers.QueueManager;
import com.hypixel.hytale.logger.HytaleLogger;

import java.util.logging.Level;

/**
 * Periodic task that processes server queues
 * Runs every 2 seconds to check for available slots and transfer queued players
 */
public class QueueProcessTask implements Runnable {
    private final HytaleLogger logger;
    private final QueueManager queueManager;

    public QueueProcessTask(HytaleLogger logger, QueueManager queueManager) {
        this.logger = logger;
        this.queueManager = queueManager;
    }

    @Override
    public void run() {
        try {
            queueManager.processQueues();
        } catch (Exception e) {
            logger.at(Level.WARNING).log("Error processing queues: %s", e.getMessage());
        }
    }
}
