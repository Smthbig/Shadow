package com.smthbig.shadow.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public final class FocusStore {

    private static final String PREF = "shadow_focus";
    private static final String KEY_INTENTION = "daily_intention";
    private static final String KEY_INTENTION_DATE = "intention_date";
    private static final String KEY_DAILY_GOAL = "daily_goal_minutes";

    private static final int DEFAULT_DAILY_GOAL = 120;
    private static final int[] LEVEL_THRESHOLDS_MINUTES = {0, 5, 15, 30, 60};

    private final SharedPreferences prefs;
    private final SimpleDateFormat dateKeyFormat;

    public FocusStore(Context context) {
        this.prefs = context.getApplicationContext()
                .getSharedPreferences(PREF, Context.MODE_PRIVATE);
        this.dateKeyFormat = new SimpleDateFormat("yyyyMMdd", Locale.US);
    }

    public void logFocusSession(int minutes) {
        String key = dateKey("minutes");
        int total = prefs.getInt(key, 0) + minutes;
        prefs.edit().putInt(key, total).apply();

        String sessionsKey = dateKey("sessions");
        int sessions = prefs.getInt(sessionsKey, 0) + 1;
        prefs.edit().putInt(sessionsKey, sessions).apply();
    }

    public void logDistractionBlocked() {
        String key = dateKey("blocks");
        int blocks = prefs.getInt(key, 0) + 1;
        prefs.edit().putInt(key, blocks).apply();
    }

    public int getFocusMinutes(Date date) {
        return getDateValue(date, "minutes");
    }

    public int getFocusSessions(Date date) {
        return getDateValue(date, "sessions");
    }

    public int getDistractionsBlocked(Date date) {
        return getDateValue(date, "blocks");
    }

    public int getLevelForDate(Date date) {
        int minutes = getFocusMinutes(date);
        for (int i = LEVEL_THRESHOLDS_MINUTES.length - 1; i >= 0; i--) {
            if (minutes >= LEVEL_THRESHOLDS_MINUTES[i]) return i;
        }
        return 0;
    }

    public int getTodayFocusMinutes() {
        return prefs.getInt(dateKey("minutes"), 0);
    }

    public int getTodaySessions() {
        return prefs.getInt(dateKey("sessions"), 0);
    }

    public int getTodayBlocks() {
        return prefs.getInt(dateKey("blocks"), 0);
    }

    public String getIntention() {
        String today = dateKey("");
        String savedDate = prefs.getString(KEY_INTENTION_DATE, "");
        if (!today.equals(savedDate)) return "";
        return prefs.getString(KEY_INTENTION, "");
    }

    public void setIntention(String text) {
        prefs.edit()
                .putString(KEY_INTENTION, text.trim())
                .putString(KEY_INTENTION_DATE, dateKey(""))
                .apply();
    }

    public int getDailyGoalMinutes() {
        return prefs.getInt(KEY_DAILY_GOAL, DEFAULT_DAILY_GOAL);
    }

    public void setDailyGoalMinutes(int minutes) {
        prefs.edit().putInt(KEY_DAILY_GOAL, Math.max(15, Math.min(480, minutes))).apply();
    }

    public int getDailyGoalProgress() {
        int goal = getDailyGoalMinutes();
        int focused = getTodayFocusMinutes();
        return Math.min(100, (int) ((float) focused / goal * 100));
    }

    public int[] getLastWeeksData(int weeks) {
        int[] levels = new int[weeks * 7];
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(weeks * 7 - 1));

        for (int i = 0; i < levels.length; i++) {
            levels[i] = getLevelForDate(cal.getTime());
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
        return levels;
    }

    public static int getLevelForMinutes(int minutes) {
        for (int i = LEVEL_THRESHOLDS_MINUTES.length - 1; i >= 0; i--) {
            if (minutes >= LEVEL_THRESHOLDS_MINUTES[i]) return i;
        }
        return 0;
    }

    private int getDateValue(Date date, String suffix) {
        String key = dateKeyFormat.format(date) + "_" + suffix;
        return prefs.getInt(key, 0);
    }

    private String dateKey(String suffix) {
        return dateKeyFormat.format(new Date()) + "_" + suffix;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }
}
