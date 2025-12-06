package com.qch.sumelauncher.viewmodel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.qch.sumelauncher.bean.ActivityBean;
import com.qch.sumelauncher.utils.ApplicationUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppViewModel extends AndroidViewModel {
    private static final String TAG = "AppViewModel";
    // constant
    private static final int defNumRow = 5;
    private static final int defNumColumn = 5;

    // multithread
    //private final ExecutorService executorService = Executors.newFixedThreadPool(4);
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    // data
    private final MutableLiveData<Boolean> mDisplayTopBar = new MutableLiveData<>();
    private final MutableLiveData<Boolean> mAllowScrollPage = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumRow = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumColumn = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumItemsPerPage = new MutableLiveData<>();
    private final MutableLiveData<Integer> mNumPage = new MutableLiveData<>();
    private final MutableLiveData<Integer> mCurrentPage = new MutableLiveData<>();
    private final MutableLiveData<List<ActivityBean>> mActivityBeanList = new MutableLiveData<>();
    private final MutableLiveData<Map<Integer, List<ActivityBean>>> mActivityBeanMap = new MutableLiveData<>();

    // shared preferences
    public SharedPreferences.OnSharedPreferenceChangeListener spListener = (sharedPreferences, key) -> {
        if (Objects.equals(key, "grid_count")) {
            getStoredGridCount(sharedPreferences);
        } else if (Objects.equals(key, "display_top_bar")) {
            getStoredDisplayTopBar(sharedPreferences);
        }
    };

    // broadcast receiver
    private BroadcastReceiver localeBroadcastReceiver = null;
    private BroadcastReceiver packageBroadcastReceiver = null;


    public AppViewModel(@NonNull Application application) {
        super(application);
        registerLocaleBR();
        registerPackageBR();
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        unregisterLocaleBR();
        unregisterPackageBR();
        executorService.shutdown();
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
                init();
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
                                    addActivityBeans(packageName);
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
                                    removeActivityBeans(packageName);
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
                                replaceActivityBeans(packageName);
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

    public void getStoredPreferences(SharedPreferences sharedPreferences) {
        getStoredDisplayTopBar(sharedPreferences);
        getStoredAllowScrollPage(sharedPreferences);
        getStoredGridCount(sharedPreferences);
    }

    public void getStoredDisplayTopBar(SharedPreferences sharedPreferences) {
        boolean displayTopBar = sharedPreferences.getBoolean("display_top_bar", true);
        mDisplayTopBar.postValue(displayTopBar);
    }

    public void getStoredAllowScrollPage(SharedPreferences sharedPreferences) {
        boolean allowScrollPage = sharedPreferences.getBoolean("allow_scroll_page", true);
        mAllowScrollPage.postValue(allowScrollPage);
    }

    public void getStoredGridCount(SharedPreferences sharedPreferences) {
        String gridCount = sharedPreferences.getString("grid_count", defNumRow + "," + defNumColumn);
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
        if (actualNumRow != getNumRowInt() || actualNumColumn != getNumColumnInt()) {
            mNumRow.postValue(actualNumRow);
            mNumColumn.postValue(actualNumColumn);
            mNumItemsPerPage.postValue(numItemsPerPage);
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list == null) {
                list = new ArrayList<>();
            }
            mNumPage.postValue(calcNumPage(list, numItemsPerPage));
            mActivityBeanMap.postValue(initMap(list, numItemsPerPage));
        }
    }

    private void init() {
        Context context = getApplication();
        executorService.execute(() -> {
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list == null) {
                list = new ArrayList<>();
            } else {
                list.clear();
            }
            list.addAll(ApplicationUtils.getActivityBeanList(context, null));
            sortActivityBeanList(list);
            Log.i(TAG, "Size of ActivityBean list is " + list.size());
            mActivityBeanList.postValue(list);
            // Initialize shared preferences
            SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
            getStoredDisplayTopBar(sharedPreferences);
            getStoredAllowScrollPage(sharedPreferences);
            String gridCount = sharedPreferences.getString("grid_count", defNumRow + "," + defNumColumn);
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
            mNumPage.postValue(calcNumPage(list, numItemsPerPage));
            mCurrentPage.postValue(1);
            mActivityBeanMap.postValue(initMap(list, numItemsPerPage));
        });
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
            list.sort((o1, o2) -> {
                String label1 = o1.getLabel();
                String label2 = o2.getLabel();
                return collator.compare(label1, label2);
            });
        }
    }

    private void addActivityBeans(String packageName) {
        Context context = getApplication();
        executorService.execute(() -> {
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list != null) {
                try {
                    list.addAll(ApplicationUtils.getActivityBeanList(context, packageName));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to add activity beans of " + packageName);
                }
                sortActivityBeanList(list);
                mActivityBeanList.postValue(list);
                int numItemPerPage = getNumItemsPerPageInt();
                mNumPage.postValue(calcNumPage(list, numItemPerPage));
                mActivityBeanMap.postValue(initMap(list, numItemPerPage));
            }
        });
    }

    private void removeActivityBeans(String packageName) {
        executorService.execute(() -> {
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list != null) {
                try {
                    list.removeIf(activityBean -> Objects.equals(packageName, activityBean.getPackageName()));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to remove activity beans of " + packageName);
                }
                mActivityBeanList.postValue(list);
                int numItemPerPage = getNumItemsPerPageInt();
                mNumPage.postValue(calcNumPage(list, numItemPerPage));
                mActivityBeanMap.postValue(initMap(list, numItemPerPage));
            }
        });
    }

    private void replaceActivityBeans(String packageName) {
        Log.i(TAG, "Prepare to replace activity beans of " + packageName);
        Context context = getApplication();
        executorService.execute(() -> {
            List<ActivityBean> list = mActivityBeanList.getValue();
            if (list != null) {
                try {
                    list.removeIf(activityBean -> Objects.equals(packageName, activityBean.getPackageName()));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to remove activity beans of " + packageName);
                }
                try {
                    list.addAll(ApplicationUtils.getActivityBeanList(context, packageName));
                } catch (Exception e) {
                    Log.e(TAG, "Failed to add activity beans of " + packageName);
                }
                sortActivityBeanList(list);
                mActivityBeanList.postValue(list);
                int numItemPerPage = getNumItemsPerPageInt();
                mNumPage.postValue(calcNumPage(list, numItemPerPage));
                mActivityBeanMap.postValue(initMap(list, numItemPerPage));
            }
        });
    }

    public LiveData<Boolean> getDisplayTopBar() {
        return mDisplayTopBar;
    }

    public LiveData<Boolean> getAllowScrollPage() {
        return mAllowScrollPage;
    }

    public LiveData<Integer> getNumRow() {
        return mNumRow;
    }

    public int getNumRowInt() {
        return mNumRow.getValue() == null ? defNumRow : mNumRow.getValue();
    }

    public LiveData<Integer> getNumColumn() {
        return mNumColumn;
    }

    public int getNumColumnInt() {
        return mNumColumn.getValue() == null ? defNumColumn : mNumColumn.getValue();
    }

    public LiveData<Integer> getNumItemsPerPage() {
        return mNumItemsPerPage;
    }

    public int getNumItemsPerPageInt() {
        return mNumItemsPerPage.getValue() == null ? defNumRow * defNumColumn : mNumItemsPerPage.getValue();
    }

    public LiveData<Integer> getNumPage() {
        return mNumPage;
    }

    public int getNumPageInt() {
        if (mNumPage.getValue() == null) {
            List<ActivityBean> list = mActivityBeanList.getValue();
            int numItemsPerPageInt = getNumItemsPerPageInt();
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

    public int getCurrentPageInt() {
        return mCurrentPage.getValue() == null ? 0 : mCurrentPage.getValue();
    }

    public LiveData<List<ActivityBean>> getActivityBeanList() {
        return mActivityBeanList;
    }

    public LiveData<Map<Integer, List<ActivityBean>>> getActivityBeanMap() {
        return mActivityBeanMap;
    }
}