package com.smthbig.shadow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smthbig.shadow.data.FocusStore;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.launcher.core.LauncherController;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public final class HomeViewModel extends AndroidViewModel {

    private static final int DEFAULT_FOCUS_MINUTES = 25;
    private static final String[] INSIGHTS = {
        "Stay in flow — deep work compounds.",
        "Small wins build momentum. Keep going.",
        "Your focus today shapes tomorrow's results.",
        "One task at a time. Presence over urgency.",
        "You control your attention. Protect it.",
        "Consistency > intensity for long-term growth.",
        "Every session is a brick in your foundation.",
        "The best time to focus was now. Start again."
    };

    private final LauncherController launcherController;
    private final FocusStore focusStore;
    private final Random random = new Random();

    private final MutableLiveData<Boolean> searchVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> intention = new MutableLiveData<>("");
    private final MutableLiveData<String> greeting = new MutableLiveData<>("");
    private final MutableLiveData<String> insightText = new MutableLiveData<>("");

    private final MutableLiveData<Integer> timerRemainingSecs = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> timerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> dateText = new MutableLiveData<>("");

    // Stats
    private final MutableLiveData<Integer> focusMinutes = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> focusSessions = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> blocksToday = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> goalProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> dailyGoal = new MutableLiveData<>(120);

    // Ring data
    private final MutableLiveData<Integer> streakDays = new MutableLiveData<>(0);
    private final MutableLiveData<Float> ringFocusProgress = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> ringStreakProgress = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> ringTasksProgress = new MutableLiveData<>(0f);
    private final MutableLiveData<Float> ringHoursProgress = new MutableLiveData<>(0f);

    // Ring values
    private final MutableLiveData<String> ringFocusValue = new MutableLiveData<>("0");
    private final MutableLiveData<String> ringStreakValue = new MutableLiveData<>("0");
    private final MutableLiveData<String> ringTasksValue = new MutableLiveData<>("0");
    private final MutableLiveData<String> ringHoursValue = new MutableLiveData<>("0");

    private java.util.Timer focusTimer;
    private int baseMinutes = DEFAULT_FOCUS_MINUTES;
    private int remainingSecs = 0;
    private android.os.CountDownTimer countDownTimer;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.launcherController = ServiceLocator.getInstance().getLauncherController();
        this.focusStore = new FocusStore(application);

        loadIntention();
        updateDateText();
        updateGreeting();
        refreshStats();
        generateInsight();
        computeStreak();
    }

    public LauncherController getLauncherController() {
        return launcherController;
    }

    public FocusStore getFocusStore() {
        return focusStore;
    }

    public LiveData<Boolean> getSearchVisible() {
        return searchVisible;
    }

    public void setSearchVisible(boolean visible) {
        searchVisible.setValue(visible);
    }

    public LiveData<String> getIntention() {
        return intention;
    }

    public LiveData<String> getDateText() {
        return dateText;
    }

    public LiveData<String> getGreeting() {
        return greeting;
    }

    public LiveData<String> getInsightText() {
        return insightText;
    }

    public LiveData<Integer> getFocusMinutes() {
        return focusMinutes;
    }

    public LiveData<Integer> getFocusSessions() {
        return focusSessions;
    }

    public LiveData<Integer> getBlocksToday() {
        return blocksToday;
    }

    public LiveData<Integer> getGoalProgress() {
        return goalProgress;
    }

    public LiveData<Integer> getDailyGoal() {
        return dailyGoal;
    }

    public LiveData<Integer> getStreakDays() {
        return streakDays;
    }

    public LiveData<Float> getRingFocusProgress() {
        return ringFocusProgress;
    }

    public LiveData<Float> getRingStreakProgress() {
        return ringStreakProgress;
    }

    public LiveData<Float> getRingTasksProgress() {
        return ringTasksProgress;
    }

    public LiveData<Float> getRingHoursProgress() {
        return ringHoursProgress;
    }

    public LiveData<String> getRingFocusValue() {
        return ringFocusValue;
    }

    public LiveData<String> getRingStreakValue() {
        return ringStreakValue;
    }

    public LiveData<String> getRingTasksValue() {
        return ringTasksValue;
    }

    public LiveData<String> getRingHoursValue() {
        return ringHoursValue;
    }

    public void loadIntention() {
        String saved = focusStore.getIntention();
        intention.setValue(saved);
    }

    public void setIntention(String text) {
        focusStore.setIntention(text);
        intention.setValue(text);
    }

    public void refreshStats() {
        int mins = focusStore.getTodayFocusMinutes();
        int sessions = focusStore.getTodaySessions();
        int blocks = focusStore.getTodayBlocks();
        int goal = focusStore.getDailyGoalMinutes();

        focusMinutes.setValue(mins);
        focusSessions.setValue(sessions);
        blocksToday.setValue(blocks);
        dailyGoal.setValue(goal);
        goalProgress.setValue(focusStore.getDailyGoalProgress());

        ringFocusValue.setValue(String.valueOf(mins));
        ringFocusProgress.setValue(Math.min(1f, mins / 120f));

        float hoursFloat = mins / 60f;
        ringHoursValue.setValue(String.format(Locale.US, "%.1f", hoursFloat));
        ringHoursProgress.setValue(Math.min(1f, hoursFloat / 4f));

        ringTasksValue.setValue(String.valueOf(sessions));
        ringTasksProgress.setValue(Math.min(1f, sessions / 10f));

        computeStreak();
    }

    private void computeStreak() {
        int streak = 0;
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.US);

        for (int i = 0; i < 365; i++) {
            String key = sdf.format(cal.getTime()) + "_minutes";
            int mins = focusStore.getPrefs().getInt(key, 0);
            if (mins > 0) {
                streak++;
                cal.add(Calendar.DAY_OF_YEAR, -1);
            } else {
                break;
            }
        }
        streakDays.setValue(streak);
        ringStreakValue.setValue(String.valueOf(streak));
        ringStreakProgress.setValue(Math.min(1f, streak / 30f));
    }

    private void generateInsight() {
        int mins = focusStore.getTodayFocusMinutes();
        String insight;
        if (mins == 0) {
            insight = "No focus yet today. Start a session to build momentum.";
        } else if (mins < 30) {
            insight = INSIGHTS[random.nextInt(INSIGHTS.length)];
        } else if (mins < 60) {
            insight = "Good progress! " + mins + " minutes of focus logged today.";
        } else {
            insight = "Excellent! " + mins + " minutes deep. You're in the zone.";
        }
        insightText.setValue(insight);
    }

    public void handleIntent(String text) {
        lastQuery.setValue(text);
        launcherController.handleIntentText(text);
    }

    // Timer
    public LiveData<Integer> getTimerRemainingSecs() {
        return timerRemainingSecs;
    }

    public LiveData<Boolean> getTimerRunning() {
        return timerRunning;
    }

    public int getBaseMinutes() {
        return baseMinutes;
    }

    public void setBaseMinutes(int minutes) {
        if (minutes < 1) minutes = 1;
        if (minutes > 180) minutes = 180;
        this.baseMinutes = minutes;
        if (!Boolean.TRUE.equals(timerRunning.getValue())) {
            remainingSecs = minutes * 60;
            timerRemainingSecs.setValue(remainingSecs);
        }
    }

    public void startTimer() {
        if (Boolean.TRUE.equals(timerRunning.getValue())) return;
        if (remainingSecs <= 0) remainingSecs = baseMinutes * 60;

        timerRunning.setValue(true);
        countDownTimer = new android.os.CountDownTimer(remainingSecs * 1000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                remainingSecs = (int) (millisUntilFinished / 1000);
                timerRemainingSecs.setValue(remainingSecs);
            }

            @Override
            public void onFinish() {
                remainingSecs = 0;
                timerRemainingSecs.setValue(0);
                timerRunning.setValue(false);
                focusStore.logFocusSession(baseMinutes);
                refreshStats();
                generateInsight();
            }
        };
        countDownTimer.start();
    }

    public void pauseTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        timerRunning.setValue(false);
    }

    public void resetTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        remainingSecs = baseMinutes * 60;
        timerRemainingSecs.setValue(remainingSecs);
        timerRunning.setValue(false);
    }

    private void updateGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greet;
        if (hour < 12) greet = "Good morning";
        else if (hour < 17) greet = "Good afternoon";
        else greet = "Good evening";
        greeting.postValue(greet);
    }

    private void updateDateText() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        dateText.postValue(sdf.format(cal.getTime()));
    }

    public void showFocusWarning() {
        // handled in activity
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        if (focusTimer != null) {
            focusTimer.cancel();
        }
    }

    public String getLastKnownIntention() {
        String saved = focusStore.getIntention();
        return saved != null && !saved.isEmpty() ? saved : "";
    }
}
