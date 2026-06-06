package com.smthbig.shadow.tracking;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class FrictionStore {

    private static FrictionStore instance;

    private final Map<String, Long> lastLaunchMap = new ConcurrentHashMap<>();
    private final Map<String, Long> heatMap = new ConcurrentHashMap<>();
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
