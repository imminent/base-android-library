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

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.annotation.ParametersAreNonnullByDefault;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

/**
 * <p>Wraps a {@link CompatibleActivityLifecycleCallbacks} into an {@link ActivityLifecycleCallbacks}.</p>
 */
@ParametersAreNonnullByDefault
/* package */class FrameworkActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

    /**
     * <p>Wraps a {@link CompatibleActivityLifecycleCallbacks} into an {@link ActivityLifecycleCallbacks}.</p>
     * @param callback The callback to delegate callbacks to
     */
    public FrameworkActivityLifecycleCallbacks(CompatibleActivityLifecycleCallbacks callback) {
        _callback = callback;
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityCreated(Activity activity, @Nullable Bundle icicle) {
        _callback.onActivityCreated(activity, icicle);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityStarted(Activity activity) {
        _callback.onActivityStarted(activity);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityResumed(Activity activity) {
        _callback.onActivityResumed(activity);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityPaused(Activity activity) {
        _callback.onActivityPaused(activity);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityStopped(Activity activity) {
        _callback.onActivityStopped(activity);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivitySaveInstanceState(Activity activity, @Nullable Bundle icicle) {
        _callback.onActivitySaveInstanceState(activity, icicle);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void onActivityDestroyed(Activity activity) {
        _callback.onActivityDestroyed(activity);
    }

    /** The callback */
    private CompatibleActivityLifecycleCallbacks _callback;
}
