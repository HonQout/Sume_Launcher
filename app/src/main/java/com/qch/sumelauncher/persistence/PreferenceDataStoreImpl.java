package com.qch.sumelauncher.persistence;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class PreferenceDataStoreImpl {
    private static final String TAG = "PreferenceDataStoreImpl";
    protected static final String NAME = "settings";
    protected final RxDataStore<Preferences> dataStore;

    public PreferenceDataStoreImpl(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), NAME).build();
    }

    public Completable setBoolean(String key, boolean value) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
            mutablePreferences.set(preferencesKey, value);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putBooleanAsync(String key, boolean value) {
        Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Completable setInteger(String key, int value) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
            mutablePreferences.set(preferencesKey, value);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putIntegerAsync(String key, int value) {
        Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Completable setLong(String key, long value) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
            mutablePreferences.set(preferencesKey, value);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putLongAsync(String key, long value) {
        Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Completable setFloat(String key, float value) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
            mutablePreferences.set(preferencesKey, value);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putFloatAsync(String key, float value) {
        Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Completable setString(String key, String value) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
            mutablePreferences.set(preferencesKey, value);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putStringAsync(String key, String value) {
        Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Completable setStringSet(String key, Set<String> values) {
        return dataStore.updateDataAsync(preferences -> {
            MutablePreferences mutablePreferences = preferences.toMutablePreferences();
            Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
            mutablePreferences.set(preferencesKey, values);
            return Single.just(mutablePreferences);
        }).ignoreElement();
    }

    public void putStringSetAsync(String key, Set<String> values) {
        Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
        dataStore.updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, values);
                    return Single.just(mutablePreferences);
                }).subscribeOn(Schedulers.io())
                .subscribe(
                        success -> {
                        },
                        throwable -> Log.e(TAG, "Failed to set key " + key, throwable)
                );
    }

    public Flowable<Boolean> getBooleanFlowable(String key, boolean defValue) {
        Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
        return dataStore.data()
                .map(preferences -> {
                    Boolean value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get boolean flowable. Return default values.");
                    return defValue;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public boolean getBooleanSync(String key, boolean defValue) {
        try {
            Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get boolean data synchronously. Return default value.");
            return defValue;
        }
    }

    public Flowable<Integer> getIntegerFlowable(String key, int defValue) {
        Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
        return dataStore.data()
                .map(preferences -> {
                    Integer value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get integer flowable. Return default values.");
                    return defValue;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public int getIntegerSync(String key, int defValue) {
        try {
            Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get integer data synchronously. Return default value.");
            return defValue;
        }
    }

    public Flowable<Long> getLongFlowable(String key, long defValue) {
        Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
        return dataStore.data()
                .map(preferences -> {
                    Long value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get long flowable. Return default values.");
                    return defValue;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Long getLongSync(String key, long defValue) {
        try {
            Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get long data synchronously. Return default value.");
            return defValue;
        }
    }

    public Flowable<Float> getFloatFlowable(String key, float defValue) {
        Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
        return dataStore.data()
                .map(preferences -> {
                    Float value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get float flowable. Return default values.");
                    return defValue;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public float getFloatSync(String key, float defValue) {
        try {
            Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get float data synchronously. Return default value.");
            return defValue;
        }
    }

    public Flowable<String> getStringFlowable(String key, String defValue) {
        Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
        return dataStore.data()
                .map(preferences -> {
                    String value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get string flowable. Return default values.");
                    return defValue;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public String getStringSync(String key, String defValue) {
        try {
            Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get string data synchronously. Return default value.");
            return defValue;
        }
    }

    public Flowable<Set<String>> getStringSetFlowable(String key, Set<String> defValues) {
        Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
        return dataStore.data()
                .map(preferences -> {
                    Set<String> values = preferences.get(preferencesKey);
                    return values != null ? values : defValues;
                })
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get string set flowable. Return default values.");
                    return defValues;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    public Set<String> getStringSetSync(String key, Set<String> defValues) {
        try {
            Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
            return dataStore
                    .data()
                    .map(preferences -> preferences.get(preferencesKey))
                    .blockingFirst(defValues);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get string set data synchronously. Return default value.");
            return defValues;
        }
    }
}