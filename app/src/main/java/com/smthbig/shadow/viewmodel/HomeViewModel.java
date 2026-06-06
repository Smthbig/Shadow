package com.smthbig.shadow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.launcher.core.LauncherController;
import com.smthbig.shadow.launcher.home.DoomsdayStore;

public final class HomeViewModel extends AndroidViewModel {

    private final LauncherController launcherController;
    private final DoomsdayStore doomsdayStore;

    private final MutableLiveData<Boolean> searchVisible = new MutableLiveData<>(false);
    private final MutableLiveData<String> lastQuery = new MutableLiveData<>("");

    public HomeViewModel(@NonNull Application application) {
        super(application);
        this.launcherController = ServiceLocator.getInstance().getLauncherController();
        this.doomsdayStore = new DoomsdayStore(application);
    }

    public LauncherController getLauncherController() {
        return launcherController;
    }

    public DoomsdayStore getDoomsdayStore() {
        return doomsdayStore;
    }

    public LiveData<Boolean> getSearchVisible() {
        return searchVisible;
    }

    public void setSearchVisible(boolean visible) {
        searchVisible.setValue(visible);
    }

    public void handleIntent(String text) {
        lastQuery.setValue(text);
        launcherController.handleIntentText(text);
    }
}
