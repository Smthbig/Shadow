package com.smthbig.shadow.viewmodel;

import android.app.Application;
import android.os.CountDownTimer;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smthbig.shadow.data.FocusStore;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.launcher.core.LauncherController;

import java.util.Calendar;
import java.util.Locale;

public final class HomeViewModel extends AndroidViewModel {

    private static final int DEFAULT_FOCUS_MINUTES = 25;

    private final LauncherController launcherController;
    private final FocusStore focusStore;

    private final MutableLiveData<Boolean> searchVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> intention = new MutableLiveData<>("");
    private final MutableLiveData<String> greeting = new MutableLiveData<>("");

    private final MutableLiveData<Integer> timerRemainingSecs = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> timerRunning = new MutableLiveData<>(false);
    private final MutableLiveData<String> dateText = new MutableLiveData<>("");

    private final MutableLiveData<Integer> focusMinutes = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> focusSessions = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> blocksToday = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> goalProgress = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> dailyGoal = new MutableLiveData<>(120);

    private CountDownTimer countDownTimer;
    private int baseMinutes = DEFAULT_FOCUS_MINUTES;
    private int remainingSecs = 0;

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.launcherController = ServiceLocator.getInstance().getLauncherController();
        this.focusStore = new FocusStore(application);

        loadIntention();
        updateDateText();
        updateGreeting();
        refreshStats();
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

    public void loadIntention() {
        String saved = focusStore.getIntention();
        intention.setValue(saved);
    }

    public void setIntention(String text) {
        focusStore.setIntention(text);
        intention.setValue(text);
    }

    public void refreshStats() {
        focusMinutes.setValue(focusStore.getTodayFocusMinutes());
        focusSessions.setValue(focusStore.getTodaySessions());
        blocksToday.setValue(focusStore.getTodayBlocks());
        int goal = focusStore.getDailyGoalMinutes();
        dailyGoal.setValue(goal);
        goalProgress.setValue(focusStore.getDailyGoalProgress());
    }

    private void updateGreeting() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        String greet;
        if (hour < 12) greet = "Good morning";
        else if (hour < 17) greet = "Good afternoon";
        else greet = "Good evening";
        greeting.setValue(greet);
    }

    public void handleIntent(String text) {
        lastQuery.setValue(text);
        launcherController.handleIntentText(text);
    }

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
        if (!timerRunning.getValue()) {
            remainingSecs = minutes * 60;
            timerRemainingSecs.setValue(remainingSecs);
        }
    }

    public void startTimer() {
        if (timerRunning.getValue()) return;
        if (remainingSecs <= 0) remainingSecs = baseMinutes * 60;

        timerRunning.setValue(true);
        countDownTimer = new CountDownTimer(remainingSecs * 1000L, 1000) {
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

    private void updateDateText() {
        Calendar cal = Calendar.getInstance();
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("EEEE, MMM d", Locale.getDefault());
        dateText.setValue(sdf.format(cal.getTime()));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}
