package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;
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
        LOGV(_TAG, "Tracking activity (%s)...", activity.getClass().getSimpleName());
        EasyTracker.getInstance().activityStart(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {
        LOGV(_TAG, "Stopped tracking activity (%s).", activity.getClass().getSimpleName());
        EasyTracker.getInstance().activityStop(activity);
    }

    /** Tag to label {@link GoogleAnalyticsCallbacks} log messages */
    private static final String _TAG = makeLogTag("Analytics");
}
