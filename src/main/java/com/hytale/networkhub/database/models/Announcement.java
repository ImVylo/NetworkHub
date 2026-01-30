package com.hytale.networkhub.database.models;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Announcement {
    private UUID creatorUuid;
    private String creatorName;
    private String title;
    private String subtitle;
    private String actionBar;
    private DisplayType displayType;
    private List<String> targetServers = new ArrayList<>();
    private String targetPermissions;
    private int durationSeconds;
    private int priority;
    private String sound;

    public enum DisplayType {
        TITLE, SUBTITLE, ACTIONBAR, POPUP, ALL
    }

    // Getters and Setters
    public UUID getCreatorUuid() { return creatorUuid; }
    public void setCreatorUuid(UUID creatorUuid) { this.creatorUuid = creatorUuid; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }

    public String getActionBar() { return actionBar; }
    public void setActionBar(String actionBar) { this.actionBar = actionBar; }

    public DisplayType getDisplayType() { return displayType; }
    public void setDisplayType(DisplayType displayType) { this.displayType = displayType; }

    public List<String> getTargetServers() { return targetServers; }
    public void setTargetServers(List<String> targetServers) { this.targetServers = targetServers; }

    public String getTargetPermissions() { return targetPermissions; }
    public void setTargetPermissions(String targetPermissions) { this.targetPermissions = targetPermissions; }

    public int getDurationSeconds() { return durationSeconds; }
    public void setDurationSeconds(int durationSeconds) { this.durationSeconds = durationSeconds; }

    public int getPriority() { return priority; }
    public void setPriority(int priority) { this.priority = priority; }

    public String getSound() { return sound; }
    public void setSound(String sound) { this.sound = sound; }
}
