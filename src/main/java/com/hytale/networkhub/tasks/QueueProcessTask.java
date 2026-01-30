package com.hytale.networkhub.tasks;

import com.hytale.networkhub.managers.QueueManager;

public class QueueProcessTask implements Runnable {
    private final QueueManager queueManager;

    public QueueProcessTask(QueueManager queueManager) {
        this.queueManager = queueManager;
    }

    @Override
    public void run() {
        try {
            queueManager.processQueues();
        } catch (Exception e) {
            // Silently fail
        }
    }
}
