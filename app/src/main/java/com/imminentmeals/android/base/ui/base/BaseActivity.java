package com.imminentmeals.android.base.ui.base;


import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import javax.inject.Inject;

import org.holoeverywhere.app.Activity;

import com.actionbarsherlock.view.MenuItem;
import com.imminentmeals.android.base.utilities.GateKeeper;
import com.imminentmeals.android.base.utilities.lifecycle_callback.MainLifecycleDispatcher;
import com.squareup.otto.Bus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;

/**
 * <p>Base {@link android.app.Activity} that handles {@link Application#ActivityLifecycleCallbacks},
 * {@link android.app.Fragment} and {@link android.app.ActionBar} backward-compatibility.
 *
 * @author Dandre Allison
 */
public class BaseActivity extends Activity {
    /** The event bus */
    @Inject protected Bus bus;

    /**
     * <p>Flag to indicate the refresh action has been triggered.</p>
     */
    public static final class RefreshEvent { }

/* Lifecycle */
    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCreate(@CheckForNull Bundle icicle) {
        super.onCreate(icicle);
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityCreated(this, icicle);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onStart() {
        super.onStart();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityStarted(this);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onResume() {
        super.onResume();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityResumed(this);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onPause() {
        super.onPause();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityPaused(this);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onStop() {
        super.onStop();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityStopped(this);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onSaveInstanceState(@CheckForNull Bundle icicle) {
        super.onSaveInstanceState(icicle);
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivitySaveInstanceState(this, icicle);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onDestroy() {
        super.onDestroy();
        if (!GateKeeper.hasICS()) MainLifecycleDispatcher.get().onActivityDestroyed(this);
    }

/* Activity Callbacks */
    /**
     * <p>Implements basic up affordance based on the "android.support.PARENT_ACTIVITY" meta-data, should
     * be the class name of the {@link android.app.Activity} that resides one level up from the given Activity.</p>
     * @param action The selected action
     * @return {@code true} signifies that the action was handled by this method
     */
    @Override
    @OverridingMethodsMustInvokeSuper
    public boolean onOptionsItemSelected(@Nonnull MenuItem action) {
        switch (action.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(action);
    }

/* Helpers */
    /**
     * <p>Converts a {@link android.support.v4.app.Fragment}'s arguments {@link Bundle} into an
     * {@link Intent}.</p>
     * @see #intentToFragmentArguments(android.content.Intent) for inverse of this function
     * @param arguments The fragment's arguments
     * @return The intent converted from the given bundle
     */
    @Nonnull public static Intent fragmentArgumentsToIntent(@Nullable Bundle arguments) {
        final Intent intent = new Intent();
        if (arguments == null) return intent;

        // Set the data URI to be the URI in the arguments, if one exists
        final Uri data = arguments.getParcelable("_uri");
        if (data != null)
            intent.setData(data);

        // Convert the Bundle into an Intent and remove the URI extra, as it was added as the data URI already
        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    /**
     * <p>Converts an {@link Intent} into a {@link Bundle} suitable for use as a
     * {@link android.support.v4.app.Fragment}'s arguments.</p>
     * @see #fragmentArgumentsToIntent(android.os.Bundle) for inverse of this function
     * @param intent The intent
     * @return The bundle converted from the given intent
     */
    @Nonnull public static Bundle intentToFragmentArguments(@Nullable Intent intent) {
        final Bundle arguments = new Bundle();
        if (intent == null) return arguments;

        // Get the data URI to be the URI in the arguments, if one exists
        final Uri data = intent.getData();
        if (data != null)
            arguments.putParcelable("_uri", data);

        final Bundle extras = intent.getExtras();
        if (extras != null)
            arguments.putAll(extras);
        return arguments;
    }
}
