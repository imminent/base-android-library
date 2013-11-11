package com.imminentmeals.android.base.utilities;

import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

import android.app.Activity;
import android.app.Application.ActivityLifecycleCallbacks;
import android.os.Bundle;

/**
 * <p>A convenience class to extend when only a subset of all callbacks is needed. This implements all methods
 * in the {@link ActivityLifecycleCallbacks} but does nothing.</p>
 */
@ParametersAreNonnullByDefault
public class SimpleActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

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
