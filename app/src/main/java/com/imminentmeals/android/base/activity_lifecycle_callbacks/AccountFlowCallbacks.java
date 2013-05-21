package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.CheckForNull;
import javax.inject.Inject;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.imminentmeals.android.base.ui.AccountActivity;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.SimpleActivityLifecycleCallbacks;
import com.imminentmeals.android.base.utilities.StringUtilities;

/**
 * <p>Finishes the {@link Activity} and starts the authentication flow if not authenticated.</p>
 * @author Dandre Allison
 */
public class AccountFlowCallbacks extends SimpleActivityLifecycleCallbacks {

    @Inject
    public AccountFlowCallbacks(AccountUtilities account_utilities) {
        _account_utilities = account_utilities;
        @CheckForNull final String connect_account_action = _account_utilities.connectAccountActionName();
        @CheckForNull final String add_account_action = _account_utilities.addAccountActionName();
        _ACCOUNT_ACTION = Pattern.compile("(" + StringUtilities.emptyIfNull(connect_account_action) +
                (add_account_action == null? "" : "|" + add_account_action) + ")").matcher("");
        LOGV("Account action matcher = " + _ACCOUNT_ACTION.pattern());
    }

    @Override
    public void onActivityCreated(Activity activity, @CheckForNull Bundle icicle) {
        // Starts the account authentication user flow. Allows the user to select from added accounts or add a
        // new account.
        LOGV("Checking for authenticated account... (" + activity.getLocalClassName() + ")");
        @CheckForNull final String action = activity.getIntent().getAction();
        if (!AccountUtilities.isAuthenticated(activity) && !(action == null || _ACCOUNT_ACTION.reset(action).matches())) {
            LOGV("An authenticated account is required, starting connect account Activity");
            final Intent login_flow_intent = new Intent(_account_utilities.connectAccountActionName());
            login_flow_intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            login_flow_intent.putExtra(AccountActivity.EXTRA_FINISH_INTENT, activity.getIntent());
            activity.startActivity(login_flow_intent);
            activity.finish();
        }
    }

    private final AccountUtilities _account_utilities;
    private final Matcher _ACCOUNT_ACTION;
}
