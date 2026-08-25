package com.qch.sumelauncher.persistence;

import android.content.Context;
import android.util.Log;

import androidx.datastore.preferences.core.MutablePreferences;
import androidx.datastore.preferences.core.Preferences;
import androidx.datastore.preferences.core.PreferencesFactory;
import androidx.datastore.preferences.core.PreferencesKeys;
import androidx.datastore.preferences.rxjava3.RxPreferenceDataStoreBuilder;
import androidx.datastore.rxjava3.RxDataStore;

import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.disposables.Disposable;

public class PreferenceDataStoreImpl {
    private static final String TAG = "PreferenceDataStoreImpl";
    protected static final String NAME = "settings";
    protected final RxDataStore<Preferences> dataStore;

    public PreferenceDataStoreImpl(Context context) {
        dataStore = new RxPreferenceDataStoreBuilder(context.getApplicationContext(), NAME).build();
    }

    /**
     * Put a boolean value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putBoolean(String key, boolean value) {
        Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                })
                .subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put boolean value with key " + key + ".", throwable)
                );
    }

    /**
     * Get the boolean value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        Boolean value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValue;
                    })
                    .blockingFirst(defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get boolean value by key " + key + ". Return default value.");
            return defaultValue;
        }
    }

    /**
     * Get a Flowable of the boolean value corresponding to the key from the preference data store.
     */
    public Flowable<Boolean> getBooleanFlowable(String key, boolean defValue) {
        Preferences.Key<Boolean> preferencesKey = PreferencesKeys.booleanKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of boolean value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    Boolean value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Put an integer value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putInteger(String key, int value) {
        Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                })
                .subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put integer with key " + key + ".", throwable)
                );
    }

    /**
     * Get the integer value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public int getInteger(String key, int defaultValue) {
        Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        Integer value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValue;
                    })
                    .blockingFirst(defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get integer value by key " + key + ". Return default value.");
            return defaultValue;
        }
    }

    /**
     * Get a Flowable of the integer value corresponding to the key from the preference data store.
     */
    public Flowable<Integer> getIntegerFlowable(String key, int defValue) {
        Preferences.Key<Integer> preferencesKey = PreferencesKeys.intKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of integer value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    Integer value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Put a long value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putLong(String key, long value) {
        Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                })
                .subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put long value with key " + key + ".", throwable)
                );
    }

    /**
     * Get the long value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public Long getLong(String key, long defaultValue) {
        Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        Long value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValue;
                    })
                    .blockingFirst(defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get long value by key " + key + ". Return default value.");
            return defaultValue;
        }
    }

    /**
     * Get a Flowable of the long value corresponding to the key from the preference data store.
     */
    public Flowable<Long> getLongFlowable(String key, long defValue) {
        Preferences.Key<Long> preferencesKey = PreferencesKeys.longKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of long value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    Long value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Put a float value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putFloat(String key, float value) {
        Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                })
                .subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put float value with key " + key + ".", throwable)
                );
    }

    /**
     * Get the float value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public float getFloat(String key, float defaultValue) {
        Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        Float value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValue;
                    })
                    .blockingFirst(defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get float value by key " + key + ". Return default value.");
            return defaultValue;
        }
    }

    /**
     * Get a Flowable of the float value corresponding to the key from the preference data store.
     */
    public Flowable<Float> getFloatFlowable(String key, float defValue) {
        Preferences.Key<Float> preferencesKey = PreferencesKeys.floatKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of float value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    Float value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Put a string value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putString(String key, String value) {
        Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, value);
                    return Single.just(mutablePreferences);
                }).subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put string value with key " + key + ".", throwable)
                );
    }

    /**
     * Get the string value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public String getString(String key, String defaultValue) {
        Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        String value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValue;
                    })
                    .blockingFirst(defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get string value by key " + key + ". Return default value.");
            return defaultValue;
        }
    }

    /**
     * Get a Flowable of the string value corresponding to the key from the preference data store.
     */
    public Flowable<String> getStringFlowable(String key, String defValue) {
        Preferences.Key<String> preferencesKey = PreferencesKeys.stringKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of string value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    String value = preferences.get(preferencesKey);
                    return value != null ? value : defValue;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * Put a string set value into the preference data store.
     * <p>This operation is asynchronous.
     */
    public @NonNull Disposable putStringSet(String key, Set<String> values) {
        Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
        return dataStore
                .updateDataAsync(preferences -> {
                    MutablePreferences mutablePreferences = preferences.toMutablePreferences();
                    mutablePreferences.set(preferencesKey, values);
                    return Single.just(mutablePreferences);
                })
                .subscribe(
                        success -> {
                        },
                        throwable ->
                                Log.e(TAG, "Failed to put string set value with key " + key + ".", throwable)
                );
    }

    /**
     * Get the string set value corresponding to the key from the preference data store.
     * <p>This operation is synchronous.
     */
    public Set<String> getStringSet(String key, Set<String> defaultValues) {
        Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
        try {
            return dataStore
                    .data()
                    .map(preferences -> {
                        Set<String> value = preferences.get(preferencesKey);
                        return value != null ? value : defaultValues;
                    })
                    .blockingFirst(defaultValues);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get string set value by key " + key + ". Return default values.");
            return defaultValues;
        }
    }

    /**
     * Get a Flowable of the string set value corresponding to the key from the preference data store.
     */
    public Flowable<Set<String>> getStringSetFlowable(String key, Set<String> defValues) {
        Preferences.Key<Set<String>> preferencesKey = PreferencesKeys.stringSetKey(key);
        return dataStore
                .data()
                .onErrorReturn(throwable -> {
                    Log.e(TAG, "Failed to get Flowable of string set value by key " + key + ".", throwable);
                    return PreferencesFactory.createEmpty();
                })
                .map(preferences -> {
                    Set<String> values = preferences.get(preferencesKey);
                    return values != null ? values : defValues;
                })
                .distinctUntilChanged()
                .observeOn(AndroidSchedulers.mainThread());
    }
}