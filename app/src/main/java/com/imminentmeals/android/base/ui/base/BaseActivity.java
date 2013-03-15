package com.imminentmeals.android.base.ui.base;


import org.holoeverywhere.app.Activity;
import com.imminentmeals.android.base.utilities.GateKeeper;
import com.imminentmeals.android.base.utilities.lifecycle_callback.MainLifecycleDispatcher;

import android.os.Bundle;

/**
 * <p>Base {@link android.app.Activity} that handles {@link Application#ActivityLifecycleCallbacks},
 * {@link android.app.Fragment} and {@link android.app.ActionBar} backward-compatibility.
 *
 * @author Dandre Allison
 */
public class BaseActivity extends Activity {

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityCreated(this, icicle);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityStarted(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityResumed(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityPaused(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityStopped(this);
    }

    @Override
    protected void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivitySaveInstanceState(this, icicle);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityDestroyed(this);
    }
}
