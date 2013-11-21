package com.imminentmeals.android.base.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import dagger.Module;
import dagger.Provides;
import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Singleton;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGW;

@Module(
      library = true
    , complete = false
)
@ParametersAreNonnullByDefault
public class ActivityModule {

    public ActivityModule(Activity activity) {
        _activity = activity;
    }

    /**
     * Allow the activity context to be injected but require that it be annotated with
     * {@link ForActivity @ForActivity} to explicitly differentiate it from application context.
     */
    @Provides @Singleton @ForActivity Context provideActivityContext() {
        return _activity;
    }

    @Provides @Singleton ActionBar providesActionBar() {
        final ActionBar action_bar = _activity.getActionBar();
        if (action_bar == null) AUTOTAGLOGW("Expected an Action Bar for %s, but it was null",
            _activity);
        return action_bar;
    }

    @Provides @Singleton Resources providesResources() {
        return _activity.getResources();
    }

    @Provides @Singleton LayoutInflater providesInflater() {
        return _activity.getLayoutInflater();
    }

    protected final Activity _activity;
}
