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

    @Provides @Singleton ActionBar providesActionBar(@ForActivity Context activity) {
        final ActionBar action_bar = ((Activity) activity).getActionBar();
        if (action_bar == null) AUTOTAGLOGW("Expected an Action Bar for %s, but it was null",
            activity);
        return action_bar;
    }

    @Provides @Singleton Resources providesResources(@ForActivity Context activity) {
        return activity.getResources();
    }

    @Provides @Singleton LayoutInflater providesInflater(@ForActivity Context activity) {
        return ((Activity) activity).getLayoutInflater();
    }
}
