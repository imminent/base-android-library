/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2013 Benoit 'BoD' Lubek (BoD@JRAF.org)
 * Copyright (C) 2006 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imminentmeals.android.base.utilities.lifecycle_callback;

import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.Lists;

import android.app.Activity;
import android.os.Bundle;

/**
 * <p>Maintains the list of {@link CompatibleActivityLifecycleCallbacks}s registered with
 * {@link android.app.Application}s' {@link Activity} lifecycles for backward-compatible
 * {@link android.app.Application#registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)} and
 * {@link android.app.Application#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)}.</p>
 */
@Singleton
public final class MainLifecycleDispatcher implements CompatibleActivityLifecycleCallbacks {

    /**
     * <p>Retrieves the {@linkplain MainLifecycleDispatcher lifecycle callback dispatcher}.</p>
     * @return The lifecycle callback dispatcher
     */
    public static MainLifecycleDispatcher get() {
        return _LIFECYCLE_DISPATCHER;
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle icicle) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityCreated(activity, icicle);
    }

    @Override
    public void onActivityStarted(Activity activity) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityStarted(activity);
    }

    @Override
    public void onActivityResumed(Activity activity) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityResumed(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityPaused(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityStopped(activity);
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle icicle) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivitySaveInstanceState(activity, icicle);
    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        for (CompatibleActivityLifecycleCallbacks callback : _activity_lifecycle_callbacks)
            callback.onActivityDestroyed(activity);
    }

    /**
     * <p>Registers the callback.</p>
     * @param callback The callback to register
     */
    /* package */void registerActivityLifecycleCallbacks(CompatibleActivityLifecycleCallbacks callback) {
        _activity_lifecycle_callbacks.add(callback);

    }

    /**
     * <p>Unregisters the callback.</p>
     * @param callback The callback to unregister
     */
    /* package */void unregisterActivityLifecycleCallbacks(CompatibleActivityLifecycleCallbacks callback) {
        _activity_lifecycle_callbacks.remove(callback);
    }

    /**
     * <p>Singleton</p>
     */
    private MainLifecycleDispatcher() {}

    /** The {@linkplain CompatibleActivityLifecycleCallbacks Activity lifecycle callback} dispatcher */
    private static final MainLifecycleDispatcher _LIFECYCLE_DISPATCHER = new MainLifecycleDispatcher();
    /** List of {@linkplain CompatibleActivityLifecycleCallbacks Activity lifecycle callbacks} to dispatch */
    private List<CompatibleActivityLifecycleCallbacks> _activity_lifecycle_callbacks = Lists.newCopyOnWriteArrayList();
}
