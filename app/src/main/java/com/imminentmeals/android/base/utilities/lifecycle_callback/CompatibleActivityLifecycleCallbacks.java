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

import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

import android.app.Activity;
import android.os.Bundle;

/**
 * <p>Backport of {@link android.app.Application.ActivityLifecycleCallbacks} to be used with
 * {@link ApplicationHelper#registerActivityLifecycleCallbacks(android.app.Application, CompatibleActivityLifecycleCallbacks)} and
 * {@link ApplicationHelper#unregisterActivityLifecycleCallbacks(android.app.Application, CompatibleActivityLifecycleCallbacks)}.</p>
 */
@ParametersAreNonnullByDefault
public interface CompatibleActivityLifecycleCallbacks {
    /**
     * <p>Triggered when an {@linkplain Activity#onCreate(Bundle) Activity is created}.</p>
     * @param activity The activity that was create
     * @param icicle The state that was frozen in time
     */
    void onActivityCreated(Activity activity, @CheckForNull Bundle icicle);

    /**
     * <p>Triggered when an {@linkplain Activity#onStart() Activity is started}.</p>
     * @param activity The activity that was started
     */
    void onActivityStarted(Activity activity);

    /**
     * <p>Triggered when an {@linkplain Activity#onResume() Activity is resumed}.</p>
     * @param activity The activity that was resumed
     */
    void onActivityResumed(Activity activity);

    /**
     * <p>Triggered when an {@linkplain Activity#onPause() Activity is paused}.</p>
     * @param activity The activity that was paused
     */
    void onActivityPaused(Activity activity);

    /**
     * <p>Triggered when an {@linkplain Activity#onStop() Activity is stopped}.</p>
     * @param activity The activity that was stopped
     */
    void onActivityStopped(Activity activity);

    /**
     * <p>Triggered when an {@linkplain Activity#onSaveInstanceState(Activity,Bundle) Activity's state is saved}.</p>
     * @param activity The activity that was saved
     * @param icicle Where the activity's state gets frozen
     */
    void onActivitySaveInstanceState(Activity activity, @CheckForNull Bundle icicle);

    /**
     * <p>Triggered when an {@linkplain Activity#onDestroy() Activity is destroyed}.</p>
     * @param activity The activity that was destroyed
     */
    void onActivityDestroyed(Activity activity);
}
