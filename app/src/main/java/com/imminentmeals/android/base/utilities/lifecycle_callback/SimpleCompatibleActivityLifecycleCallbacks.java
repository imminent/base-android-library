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
 * <p>A convenience class to extend when only a subset of all callbacks is needed. This implements all methods
 * in the {@link CompatibleActivityLifecycleCallbacks} but does nothing.</p>
 */
@ParametersAreNonnullByDefault
public class SimpleCompatibleActivityLifecycleCallbacks implements CompatibleActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, @CheckForNull Bundle icicle) { }

    @Override
    public void onActivityStarted(Activity activity) { }

    @Override
    public void onActivityResumed(Activity activity) { }

    @Override
    public void onActivityPaused(Activity activity) { }

    @Override
    public void onActivityStopped(Activity activity) { }

    @Override
    public void onActivitySaveInstanceState(Activity activity, @CheckForNull Bundle icicle) { }

    @Override
    public void onActivityDestroyed(Activity activity) { }
}
