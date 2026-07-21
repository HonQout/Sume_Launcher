package com.qch.sumelauncher.settings.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.qch.sumelauncher.application.MyApplication;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.PermissionBean;
import com.qch.sumelauncher.bean.SortedPermissions;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.DialogUtils;
import com.qch.sumelauncher.utils.PackageUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

public class PermissionViewModel extends AndroidViewModel {
    private final String TAG = "PermissionViewModel";
    // data
    private final MutableLiveData<Boolean> mAnimation = new MutableLiveData<>();
    private final MutableLiveData<PackageInfo> mPackageInfo = new MutableLiveData<>();
    private final MutableLiveData<List<PermissionBean>> mRequestedPermissionBeanList = new MutableLiveData<>();
    // multi-thread
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    // persistence
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();

    public PermissionViewModel(@NonNull Application application) {
        super(application);
        initDisposable();
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
        compositeDisposable.clear();
    }

    private void initDisposable() {
        // animation
        Disposable disposable = MyApplication.getPreferenceDataStore()
                .getBooleanFlowable("animation", true)
                .subscribe(
                        mAnimation::postValue,
                        error -> Log.e(TAG, "Failed to get value of key animation.")
                );
        compositeDisposable.add(disposable);
    }

    public void init() {
        executorService.execute(() -> {
            Context context = getApplication();
            PackageInfo packageInfo = ApplicationUtils.getPackageInfo(context, context.getPackageName());
            mPackageInfo.postValue(packageInfo);
            SortedPermissions sortedPermissions = PackageUtils.getPermissionSorted(context, context.getPackageName());
            List<PermissionBean> requestedPermissionBeanList = new ArrayList<>();
            for (PermissionInfo permissionInfo : sortedPermissions.permissionRequestedList) {
                requestedPermissionBeanList.add(new PermissionBean(context, permissionInfo));
            }
            sortPermissionBeanList(requestedPermissionBeanList);
            mRequestedPermissionBeanList.postValue(requestedPermissionBeanList);
        });
    }

    public void sortPermissionBeanList(List<PermissionBean> permissionBeanList) {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (permissionBeanList != null) {
            Collections.sort(permissionBeanList, (o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return collator.compare(name1, name2);
            });
        }
    }

    public void showPermissionDetailDialog(@NonNull Activity activity, PermissionBean permissionBean) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(permissionBean.getLabel())
                .setMessage(permissionBean.getDescription())
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss());
        DialogUtils.show(builder, getAnimationValue());
    }

    public boolean getAnimationValue() {
        return mAnimation.getValue() == null || mAnimation.getValue();
    }

    public LiveData<List<PermissionBean>> getRequestedPermissionBeanList() {
        return mRequestedPermissionBeanList;
    }
}