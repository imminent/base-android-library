package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import android.app.Activity;
import android.os.Bundle;
import butterknife.Views;

import com.imminentmeals.android.base.utilities.ObjectGraph;
import com.imminentmeals.android.base.utilities.SimpleActivityLifecycleCallbacks;

/**
 * <p>Injects the {@link Activity} with its dependencies.</p>
 * @author Dandre Allison
 */
public class InjectionCallbacks extends SimpleActivityLifecycleCallbacks {

    @Inject
    public InjectionCallbacks() { }

    @Override
    public void onActivityCreated(Activity activity, @CheckForNull Bundle icicle) {
        ObjectGraph.inject(activity);
        Views.inject(activity);
    }
}
