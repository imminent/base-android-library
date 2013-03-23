package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;

import javax.annotation.CheckForNull;
import javax.inject.Inject;
import javax.inject.Named;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.imminentmeals.android.base.ui.AccountActivity;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.SimpleActivityLifecycleCallbacks;
import static com.imminentmeals.android.base.BaseAndroidLibraryApplication.ACCOUNT_ACTIVITY;

/**
 * <p>Finishes the {@link Activity} and starts the authentication flow if not authenticated.</p>
 * @author Dandre Allison
 */
public class AccountFlowCallbacks extends SimpleActivityLifecycleCallbacks {
    @SuppressWarnings("rawtypes")
    @Inject @Named(ACCOUNT_ACTIVITY) Class account_activity;

    @Inject
    public AccountFlowCallbacks() { }

    @Override
    public void onActivityCreated(Activity activity, @CheckForNull Bundle icicle) {
        // Starts the account authentication user flow. Allows the user to select from added accounts or add a
        // new account.
        LOGV("Checking for authenticated account...");
        if (!AccountUtilities.isAuthenticated(activity) && !account_activity.isInstance(activity)) {
            LOGV("An authenticated account is required, starting AccountActivity");
            final Intent login_flow_intent = new Intent(activity, account_activity);
            login_flow_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            login_flow_intent.putExtra(AccountActivity.EXTRA_FINISH_INTENT, activity.getIntent());
            activity.startActivity(login_flow_intent);
            activity.finish();
        }
    }
}
