package com.smthbig.shadow.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.smthbig.shadow.data.FeatureStore;
import com.smthbig.shadow.data.limits.AppLimitStore;
import com.smthbig.shadow.di.ServiceLocator;
import com.smthbig.shadow.repository.AppRepository;
import com.smthbig.shadow.tracking.UsageTracker;

import java.util.List;
import java.util.concurrent.Executors;

public final class SettingsViewModel extends AndroidViewModel {

    private final UsageTracker usageTracker;
    private final FeatureStore featureStore;
    private final AppRepository appRepository;

    private final MutableLiveData<Integer> totalInterventions = new MutableLiveData<>(0);
    private final MutableLiveData<Boolean> deepFocusEnabled = new MutableLiveData<>(false);
    private final MutableLiveData<List<AppInfo>> whitelistApps = new MutableLiveData<>();

    public SettingsViewModel(@NonNull Application application) {
        super(application);
        ServiceLocator locator = ServiceLocator.getInstance();
        this.usageTracker = locator.getUsageTracker();
        this.featureStore = locator.getFeatureStore();
        this.appRepository = locator.getAppRepository();

        refreshStats();
    }

    public void refreshStats() {
        totalInterventions.setValue(
                usageTracker.getTotalDelays() + usageTracker.getTotalBlocks());
        deepFocusEnabled.setValue(featureStore.isDeepFocusEnabled());
    }

    public LiveData<Integer> getTotalInterventions() {
        return totalInterventions;
    }

    public LiveData<Boolean> getDeepFocusEnabled() {
        return deepFocusEnabled;
    }

    public void toggleDeepFocus() {
        boolean enabled = !featureStore.isDeepFocusEnabled();
        featureStore.setDeepFocusEnabled(enabled);
        deepFocusEnabled.setValue(enabled);
    }

    public LiveData<List<AppInfo>> getWhitelistApps() {
        return whitelistApps;
    }

    public void loadWhitelistAppsAsync() {
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                List<android.content.pm.ApplicationInfo> apps = appRepository.getLaunchableApps();
                android.content.pm.PackageManager pm = getApplication().getPackageManager();
                java.util.ArrayList<AppInfo> result = new java.util.ArrayList<>();

                for (android.content.pm.ApplicationInfo app : apps) {
                    String label = pm.getApplicationLabel(app).toString();
                    boolean whitelisted = featureStore.isWhitelisted(app.packageName);
                    result.add(new AppInfo(label, app.packageName, whitelisted));
                }

                whitelistApps.postValue(result);
            } catch (Exception e) {
                whitelistApps.postValue(new java.util.ArrayList<>());
            }
        });
    }
}
