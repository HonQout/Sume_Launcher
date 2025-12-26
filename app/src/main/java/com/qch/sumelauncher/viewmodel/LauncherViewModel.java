package com.qch.sumelauncher.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qch.sumelauncher.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.activity.SettingsActivity;
import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.bean.ActivityBeanStub;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.CollectionUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.IntentUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class LauncherViewModel extends AndroidViewModel {
    private static final String TAG = "LauncherViewModel";
    // constant
    private static final int defNumRow = 5;
    private static final int defNumColumn = 5;

    public enum LauncherState {
        NORMAL, EDIT
    }

    public enum AppListOp {
        INIT, ADD, REMOVE, REPLACE
    }

    // data
    private final MutableLiveData<LauncherState> mLauncherState = new MutableLiveData<>(LauncherState.NORMAL);
    private final MutableLiveData<Boolean> mDisplayStatusBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mDisplayTopBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mScrollToSwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mVolumeKeySwitchPage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAskForPermFineLocation = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumRow = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumColumn = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumItemsPerPage = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumPage = new MutableLiveData<>();
    private final MutableLiveData<Integer> mCurrentPage = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityBean>> mActivityBeanList = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityBeanStub>> mHiddenActivityList = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, List<ActivityBean>>> mActivityBeanMap = new MutableLiveData<>();

    // multi-thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    // broadcast receiver
    private BroadcastReceiver localeBroadcastReceiver = null;
    private BroadcastReceiver packageBroadcastReceiver = null;


    public LauncherViewModel(@NonNull Application application) {
        super(application);
        registerLocaleBR();
        registerPackageBR();
        initDisposable();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterLocaleBR();
        unregisterPackageBR();
        executorService.shutdown();
        compositeDisposable.clear();
    }


    private void registerLocaleBR() {
        if (localeBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_LOCALE_CHANGED);

        localeBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                updateActivityBeanList(AppListOp.INIT, null);
            }
        };

        ContextCompat.registerReceiver(getApplication(), localeBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_NOT_EXPORTED);
    }

    private void unregisterLocaleBR() {
        if (localeBroadcastReceiver != null) {
            getApplication().unregisterReceiver(localeBroadcastReceiver);
            localeBroadcastReceiver = null;
        }
    }

    private void registerPackageBR() {
        if (packageBroadcastReceiver != null) {
            return;
        }

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        intentFilter.addDataScheme("package");

        packageBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null) {
                    Log.e(TAG, "Received intent is null.");
                    return;
                }
                String action = intent.getAction();
                if (action == null) {
                    Log.e(TAG, "Action of intent is null.");
                    return;
                }
                Log.i(TAG, "Received intent action: " + action);
                switch (action) {
                    case Intent.ACTION_PACKAGE_ADDED: {
                        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                        Log.i(TAG, "Extra: replacing = " + replacing);
                        if (!replacing) {
                            String packageName;
                            Uri data = intent.getData();
                            if (data != null) {
                                packageName = data.getSchemeSpecificPart();
                                if (packageName != null) {
                                    updateActivityBeanList(AppListOp.ADD, packageName);
                                }
                            }
                        }
                        break;
                    }

                    case Intent.ACTION_PACKAGE_REMOVED: {
                        boolean replacing = intent.getBooleanExtra(Intent.EXTRA_REPLACING, false);
                        boolean dataRemoved = intent.getBooleanExtra(Intent.EXTRA_DATA_REMOVED, false);
                        Log.i(TAG, "Extra: replacing = " + replacing + ", data_removed = " + dataRemoved);
                        if (!replacing) {
                            String packageName;
                            Uri data = intent.getData();
                            if (data != null) {
                                packageName = data.getSchemeSpecificPart();
                                if (packageName != null) {
                                    updateActivityBeanList(AppListOp.REMOVE, packageName);
                                }
                            }
                        }
                        break;
                    }

                    case Intent.ACTION_PACKAGE_REPLACED: {
                        String packageName;
                        Uri data = intent.getData();
                        if (data != null) {
                            packageName = data.getSchemeSpecificPart();
                            if (packageName != null) {
                                updateActivityBeanList(AppListOp.REPLACE, packageName);
                            }
                        }
                        break;
                    }
                }
            }
        };

        ContextCompat.registerReceiver(getApplication(), packageBroadcastReceiver, intentFilter, ContextCompat.RECEIVER_EXPORTED);
    }

    private void unregisterPackageBR() {
        if (packageBroadcastReceiver != null) {
            getApplication().unregisterReceiver(packageBroadcastReceiver);
            packageBroadcastReceiver = null;
        }
    }

    private void initDisposable() {
        // display_status_bar
        Disposable disposable1 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("display_status_bar", true)
                .subscribe(
                        mDisplayStatusBar::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key display_status_bar", throwable)
                );
        compositeDisposable.add(disposable1);
        // display_top_bar
        Disposable disposable2 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("display_top_bar", true)
                .subscribe(
                        mDisplayTopBar::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key display_top_bar", throwable)
                );
        compositeDisposable.add(disposable2);
        // animation
        Disposable disposable3 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("animation", true)
                .subscribe(
                        mAnimation::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key animation", throwable)
                );
        compositeDisposable.add(disposable3);
        // scroll_switch_page
        Disposable disposable4 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("scroll_switch_page", true)
                .subscribe(
                        mScrollToSwitchPage::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key scroll_switch_page", throwable)
                );
        compositeDisposable.add(disposable4);
        // volume_key_switch_page
        Disposable disposable5 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("volume_key_switch_page", true)
                .subscribe(
                        mVolumeKeySwitchPage::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key volume_key_switch_page", throwable)
                );
        compositeDisposable.add(disposable5);
        // ask_for_perm_fine_location
        Disposable disposable6 = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("ask_for_perm_fine_location", true)
                .subscribe(
                        mAskForPermFineLocation::postValue,
                        throwable -> Log.e(TAG, "Failed to get value of key ask_for_perm_fine_location", throwable)
                );
        compositeDisposable.add(disposable6);
        // grid_count
        Disposable disposable7 = MyApplication.getPreferenceDataStore()
                .getStringFlowable("grid_count", "5,5")
                .subscribe(gridCount -> {
                            String[] split = gridCount.split(",");
                            int actualNumRow = defNumRow;
                            int actualNumColumn = defNumColumn;
                            try {
                                actualNumRow = Integer.parseInt(split[0]);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to get stored numRow.", e);
                            }
                            try {
                                actualNumColumn = Integer.parseInt(split[1]);
                            } catch (Exception e) {
                                Log.e(TAG, "Failed to get stored numRow.", e);
                            }
                            int numItemsPerPage = actualNumRow * actualNumColumn;
                            mNumRow.postValue(actualNumRow);
                            mNumColumn.postValue(actualNumColumn);
                            mNumItemsPerPage.postValue(numItemsPerPage);
                            updateActivityBeanList(AppListOp.INIT, null);
                        },
                        throwable -> Log.e(TAG, "Failed to get value of key grid_count", throwable)
                );
        compositeDisposable.add(disposable7);
    }

    private int calcNumPage(List<ActivityBean> list, int numItemPerPage) {
        return (list.size() - 1) / numItemPerPage + 1;
    }

    @NonNull
    private Map<Integer, List<ActivityBean>> initMap(List<ActivityBean> list, int numItemPerPage) {
        int numPage = calcNumPage(list, numItemPerPage);
        Map<Integer, List<ActivityBean>> map = new HashMap<>();
        for (int i = 0; i < numPage; i++) {
            int begin = i * numItemPerPage;
            if (begin >= list.size()) {
                break;
            }
            int end = Math.min(begin + numItemPerPage, list.size());
            List<ActivityBean> subList = list.subList(begin, end);
            Log.i(TAG, "Put sublist from #" + begin + " to #" + (end - 1) + " to map.");
            map.put(i, subList);
        }
        return map;
    }

    private void sortActivityBeanList(List<ActivityBean> list) {
        Collator collator = Collator.getInstance();
        if (list != null) {
            Collections.sort(list, (o1, o2) -> {
                String label1 = o1.getLabel();
                String label2 = o2.getLabel();
                return collator.compare(label1, label2);
            });
        }
    }

    private void updateActivityBeanList(AppListOp op, @Nullable String packageName) {
        executorService.execute(() -> {
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list == null || op == AppListOp.INIT) {
                // initialize
                list = new ArrayList<>(ApplicationUtils.getActivityBeanList(getApplication(), null));
                sortActivityBeanList(list);
                mActivityBeanList.postValue(list);
                mCurrentPage.postValue(1);
            }
            if (packageName != null) {
                if (op == AppListOp.REMOVE || op == AppListOp.REPLACE) {
                    try {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            list.removeIf(item ->
                                    Objects.equals(packageName, item.getPackageName()));
                        } else {
                            CollectionUtils.removeConditionally(list, item ->
                                    Objects.equals(packageName, item.getPackageName()));
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to remove activity beans of " + packageName);
                    }
                }
                if (op == AppListOp.ADD || op == AppListOp.REPLACE) {
                    try {
                        list.addAll(ApplicationUtils.getActivityBeanList(getApplication(), packageName));
                    } catch (Exception e) {
                        Log.e(TAG, "Failed to add activity beans of " + packageName);
                    }
                    sortActivityBeanList(list);
                }
            } else if (op != AppListOp.INIT) {
                Log.e(TAG, "Package name is null. Specified operation has not been executed.");
            }
            int numItemPerPage = getNumItemsPerPageValue();
            mNumPage.postValue(calcNumPage(list, numItemPerPage));
            mActivityBeanMap.postValue(initMap(list, numItemPerPage));
        });
    }

    public void setLauncherState(LauncherState state) {
        mLauncherState.postValue(state);
    }

    public LiveData<LauncherState> getLauncherState() {
        return mLauncherState;
    }

    public LauncherState getLauncherStateValue() {
        return mLauncherState.getValue() == null ? LauncherState.NORMAL : mLauncherState.getValue();
    }

    public LiveData<Boolean> getDisplayStatusBar() {
        return mDisplayStatusBar;
    }

    public LiveData<Boolean> getDisplayTopBar() {
        return mDisplayTopBar;
    }

    public LiveData<Boolean> getAnimation() {
        return mAnimation;
    }

    public boolean getAnimationValue() {
        return mAnimation.getValue() == null || mAnimation.getValue();
    }

    public LiveData<Boolean> getScrollToSwitchPage() {
        return mScrollToSwitchPage;
    }

    public boolean getVolumeKeySwitchPageValue() {
        return mVolumeKeySwitchPage.getValue() == null || mVolumeKeySwitchPage.getValue();
    }

    public boolean getAskForPermFineLocationValue() {
        return mAskForPermFineLocation.getValue() == null || mAskForPermFineLocation.getValue();
    }

    public int getNumRowValue() {
        return mNumRow.getValue() == null ? defNumRow : mNumRow.getValue();
    }

    public int getNumColumnValue() {
        return mNumColumn.getValue() == null ? defNumColumn : mNumColumn.getValue();
    }

    public int getNumItemsPerPageValue() {
        return mNumItemsPerPage.getValue() == null ? defNumRow * defNumColumn : mNumItemsPerPage.getValue();
    }

    public LiveData<Integer> getNumPage() {
        return mNumPage;
    }

    public int getNumPageValue() {
        if (mNumPage.getValue() == null) {
            List<ActivityBean> list = mActivityBeanList.getValue();
            int numItemsPerPageInt = getNumItemsPerPageValue();
            if (list == null) {
                return 0;
            } else {
                return (list.size() - 1) / numItemsPerPageInt + 1;
            }
        } else {
            return mNumPage.getValue();
        }
    }

    public void setCurrentPage(int newValue) {
        mCurrentPage.postValue(newValue);
    }

    public LiveData<Integer> getCurrentPage() {
        return mCurrentPage;
    }

    public int getCurrentPageValue() {
        return mCurrentPage.getValue() == null ? 0 : mCurrentPage.getValue();
    }

    public LiveData<List<ActivityBean>> getActivityBeanList() {
        return mActivityBeanList;
    }

    public List<ActivityBean> getActivityBeanListValue() {
        return mActivityBeanList.getValue() == null ? new ArrayList<>() : mActivityBeanList.getValue();
    }

    public LiveData<Map<Integer, List<ActivityBean>>> getActivityBeanMap() {
        return mActivityBeanMap;
    }

    public void showPermFineLocationDialog(@NonNull Activity activity) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.request_permission)
                .setMessage(R.string.perm_fine_location_reason)
                .setPositiveButton(R.string.app_info, (dialog, which) ->
                        IntentUtils.openAppDetailsPage(activity, activity.getPackageName()))
                .setNeutralButton(R.string.deny, (dialog, which) ->
                        MyApplication.getPreferenceDataStore().setBoolean("ask_for_perm_fine_location", false))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, getAnimationValue());
    }

    public void showUninstallSystemAppDialog(@NonNull Activity activity, String packageName) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(activity)
                .setTitle(R.string.hint)
                .setMessage(R.string.insist_uninstall_system_app)
                .setPositiveButton(R.string.uninstall, (dialog, which) ->
                        IntentUtils.handleLaunchIntentResult(
                                activity,
                                IntentUtils.requireUninstallApp(activity, packageName)
                        ))
                .setNegativeButton(R.string.cancel, null);
        DialogUtils.show(builder, getAnimationValue());
    }

    public void startSettingsActivity(@NonNull Activity activity) {
        Intent intent = new Intent(activity, SettingsActivity.class);
        if (!getAnimationValue()) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        }
        activity.startActivity(intent);
    }
}