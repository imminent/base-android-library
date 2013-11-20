package com.imminentmeals.android.base.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGW;

@Module(
      library = true
    , complete = false
)
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

    protected final Activity _activity;
}
