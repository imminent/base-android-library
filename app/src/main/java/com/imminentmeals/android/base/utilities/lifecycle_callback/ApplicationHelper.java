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

import com.imminentmeals.android.base.utilities.GateKeeper;

import android.annotation.TargetApi;
import android.app.Application;
import android.os.Build;

/**
 * <p>Helper for accessing
 * {@link Application#registerActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)} and
 * {@link Application#unregisterActivityLifecycleCallbacks(android.app.Application.ActivityLifecycleCallbacks)}
 * introduced in API level 14 in a backward-compatible manner.</p>
 */
public class ApplicationHelper {

/* Callback registration */
    /**
     * <p>Registers the callback to the given {@link Application}'s {@link android.app.Activity} lifecycles.</p>
     *
     * @param application The application with which to register the callback.
     * @param callback The callback to register.
     */
    public static void registerActivityLifecycleCallbacks(Application application,
                                                          CompatibleActivityLifecycleCallbacks callback) {
        if (!GateKeeper.hasICS())
            registerCompatibleActivityLifecycleCallbacks(callback);
        else
            registerFrameworkActivityLifecycleCallbacks(application, callback);
    }

    /**
     * <p>Backward-compatible registration of callback.</p>
     * @param callback The callback to register
     */
    private static void registerCompatibleActivityLifecycleCallbacks(CompatibleActivityLifecycleCallbacks callback) {
        MainLifecycleDispatcher.get().registerActivityLifecycleCallbacks(callback);
    }

    /**
     * <p>API level 14+ compatible registration of callback.</p>
     * @param application The application from which to register the callback
     * @param callback The callback to register
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void registerFrameworkActivityLifecycleCallbacks(Application application,
                                                                    CompatibleActivityLifecycleCallbacks callback) {
        application.registerActivityLifecycleCallbacks(new FrameworkActivityLifecycleCallbacks(callback));
    }


/* Callback unregistration */
    /**
     * <p>Unregisters the callback from the given {@link Application}'s {@link android.app.Activity} lifecycles.</p>
     *
     * @param application The application from which to unregister the callback
     * @param callback The callback to unregister
     */
    public void unregisterActivityLifecycleCallbacks(Application application, CompatibleActivityLifecycleCallbacks callback) {
        if (!GateKeeper.hasICS())
            unregisterCompatibleActivityLifecycleCallbacks(callback);
        else
            unregisterFrameworkActivityLifecycleCallbacks(application, callback);
    }

    /**
     * <p>Backward-compatible unregistration of callback.</p>
     * @param callback The callback to unregister
     */
    private static void unregisterCompatibleActivityLifecycleCallbacks(CompatibleActivityLifecycleCallbacks callback) {
        MainLifecycleDispatcher.get().unregisterActivityLifecycleCallbacks(callback);
    }

    /**
     * <p>API level 14+ compatible unregistration of callback.</p>
     * @param application The application from which to unregister the callback
     * @param callback The callback to unregister
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void unregisterFrameworkActivityLifecycleCallbacks(Application application,
                                                                      CompatibleActivityLifecycleCallbacks callback) {
        application.unregisterActivityLifecycleCallbacks(new FrameworkActivityLifecycleCallbacks(callback));
    }

}
