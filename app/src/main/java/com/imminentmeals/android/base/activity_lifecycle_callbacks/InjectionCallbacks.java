package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import javax.annotation.CheckForNull;

import android.app.Activity;
import android.os.Bundle;
import butterknife.Views;

import com.imminentmeals.android.base.utilities.ObjectGraph;
import com.imminentmeals.android.base.utilities.lifecycle_callback.SimpleCompatibleActivityLifecycleCallbacks;

/**
 * <p>Injects the {@link Activity} with its dependencies.</p>
 * @author Dandre Allison
 */
public class InjectionCallbacks extends SimpleCompatibleActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, @CheckForNull Bundle icicle) {
        ObjectGraph.inject(activity);
        try {
            Views.inject(activity);
        } catch (Exception _) {
            // Fall through
        }
    }
}
