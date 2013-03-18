package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import android.app.Activity;

import com.google.analytics.tracking.android.EasyTracker;
import com.imminentmeals.android.base.utilities.lifecycle_callback.SimpleCompatibleActivityLifecycleCallbacks;

/**
 * <p>{@linkplain com.imminentmeals.android.base.utilities.lifecycle_callback.CompatibleActivityLifecycleCallbacks Activity lifecycle callbacks}
 * for tracking the starting and stopping of an {@link Activity}.</p>
 * @author Dandre Allison
 */
public class GoogleAnalyticsCallbacks extends SimpleCompatibleActivityLifecycleCallbacks {

    @Override
    public void onActivityStarted(Activity activity) {
        EasyTracker.getInstance().activityStart(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        EasyTracker.getInstance().activityStop(activity);
    }
}
