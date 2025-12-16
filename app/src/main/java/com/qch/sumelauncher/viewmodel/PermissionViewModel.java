package com.qch.sumelauncher.viewmodel;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PermissionInfo;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.qch.sumelauncher.R;
import com.qch.sumelauncher.bean.PermissionBean;
import com.qch.sumelauncher.bean.permission.PermissionSorted;
import com.qch.sumelauncher.utils.ApplicationUtils;
import com.qch.sumelauncher.utils.PackageUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PermissionViewModel extends AndroidViewModel {
    private final String TAG = "PermissionViewModel";

    private final MutableLiveData<PackageInfo> mPackageInfo = new MutableLiveData<>();
    private final MutableLiveData<List<PermissionBean>> mRequestedPermissionBeanList = new MutableLiveData<>();

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public PermissionViewModel(@NonNull Application application) {
        super(application);
        init();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public void init() {
        executorService.execute(() -> {
            Context context = getApplication();
            final String packageName = "com.qch.sumelauncher";
            PackageInfo packageInfo = ApplicationUtils.getPackageInfo(context, packageName);
            mPackageInfo.postValue(packageInfo);
            PermissionSorted permissionSorted = PackageUtils.getPermissionSorted(context, packageName);
            List<PermissionBean> requestedPermissionBeanList = new ArrayList<>();
            for (PermissionInfo permissionInfo : permissionSorted.permissionRequestedList) {
                requestedPermissionBeanList.add(new PermissionBean(context, permissionInfo));
            }
            sortPermissionBeanList(requestedPermissionBeanList);
            mRequestedPermissionBeanList.postValue(requestedPermissionBeanList);
        });
    }

    public void sortPermissionBeanList(List<PermissionBean> permissionBeanList) {
        Collator collator = Collator.getInstance(Locale.getDefault());
        if (permissionBeanList != null) {
            permissionBeanList.sort((o1, o2) -> {
                String name1 = o1.getName();
                String name2 = o2.getName();
                return collator.compare(name1, name2);
            });
        }
    }

    public void showPermissionDetailDialog(@NonNull Activity activity, PermissionBean permissionBean) {
        new MaterialAlertDialogBuilder(activity)
                .setTitle(permissionBean.getLabel())
                .setMessage(permissionBean.getDescription())
                .setPositiveButton(R.string.confirm, (dialog, which) -> dialog.dismiss())
                .show();
    }

    public LiveData<List<PermissionBean>> getRequestedPermissionBeanList() {
        return mRequestedPermissionBeanList;
    }
}