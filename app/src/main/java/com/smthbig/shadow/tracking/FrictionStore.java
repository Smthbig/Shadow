package com.smthbig.shadow.tracking;

import java.util.HashMap;
import java.util.Map;

/**
 * Maintenance singleton to store volatile friction state.
 * This ensures that "Launch Heat" and "Cooldowns" survive HomeActivity recreation
 * (e.g. on configuration changes or theme switches).
 */
public final class FrictionStore {

    private static FrictionStore instance;

    private final Map<String, Long> lastLaunchMap = new HashMap<>();
    private final Map<String, Long> heatMap = new HashMap<>();
    
    // Safety check for UsageMonitorService to prevent overlapping overlays
    private String activeDelayPackage = null;

    private FrictionStore() {}

    public static synchronized FrictionStore getInstance() {
        if (instance == null) {
            instance = new FrictionStore();
        }
        return instance;
    }

    public Map<String, Long> getLastLaunchMap() { return lastLaunchMap; }
    public Map<String, Long> getHeatMap() { return heatMap; }

    public synchronized void setActiveDelay(String pkg) { this.activeDelayPackage = pkg; }
    public synchronized String getActiveDelay() { return activeDelayPackage; }
    public synchronized void clearActiveDelay() { this.activeDelayPackage = null; }
}
