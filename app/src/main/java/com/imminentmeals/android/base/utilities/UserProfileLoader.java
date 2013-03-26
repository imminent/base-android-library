package com.imminentmeals.android.base.utilities;

import android.content.AsyncTaskLoader;
import android.content.Context;

/**
 * <p>A custom {@link android.content.Loader} that retrieves the {@link AccountUtils.UserProfile} asynchronously.</p>
 * @author Dandré Allison
 */
public class UserProfileLoader extends AsyncTaskLoader<AccountUtilities.UserProfile> {

    /**
     * <p>Constructs a {@link UserProfileLoader}.</p>
     * @param context The context in which the loader operates
     */
    public UserProfileLoader(Context context) {
        super(context);
    }

/* AsyncTaskLoader Callbacks */
    @Override
    public AccountUtilities.UserProfile loadInBackground() {
        return AccountUtilities.getUserProfile(getContext());
    }

    @Override
    public void deliverResult(AccountUtilities.UserProfile user_profile) {
        _user_profile = user_profile;

        if (isStarted())
            // If the Loader is currently started, we can immediately
            // deliver its results.
            super.deliverResult(user_profile);
    }

    @Override
    protected void onStartLoading() {
        if (_user_profile != null)
            // Delivers the result immediately when it's already available
            deliverResult(_user_profile);

        if (takeContentChanged() || _user_profile == null) {
            // If the data has changed since the last time it was loaded
            // or is not currently available, start a load.
            forceLoad();
        }
    }

    @Override
    protected void onStopLoading() {
        // Attempts to cancel the current load task if possible.
        cancelLoad();
    }

    /**
     * Handles a request to completely reset the Loader.
     */
    @Override protected void onReset() {
        super.onReset();

        // Ensures the loader is stopped
        onStopLoading();

        // Clears the stored list
        if (_user_profile != null)
            _user_profile = null;
    }

    /** The list of the user's possible email address and name */
    private AccountUtilities.UserProfile _user_profile;
}
