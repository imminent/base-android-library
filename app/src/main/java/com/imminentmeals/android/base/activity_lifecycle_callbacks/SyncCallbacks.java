package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;

import android.accounts.Account;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.SyncStatusObserver;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.imminentmeals.android.base.R;
import com.imminentmeals.android.base.data.provider.BaseContract;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.SimpleActivityLifecycleCallbacks;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

/**
 * <p>Handles display of sync status in the {@link android.app.ActionBar}.</p>
 * @author Dandre Allison
 */
public class SyncCallbacks extends SimpleActivityLifecycleCallbacks {

    /**
     * <p>Interface to implement when sync status should be displayed. Though this can't be required through
     * an interface contract, this contract only works if the class also {@code bus.post(ActionMenuCreatedEvent);}
     * when the action menu is created, so that {@link SyncCallbacks} knows when it can use the action menu.</p>
     * @author Dandre Allison
     */
    public interface Syncable {
        /**
         * <p>Retrieves the current action menu.</p>
         * @return The current action menu
         */
        @Nonnull Menu actionMenu();
    }

    /**
     * <p>Notifies of the current {@linkplain Menu action menu}.</p>
     * @author Dandre Allison
     */
    public static class ActionMenuCreatedEvent { }

    /**
     * <p>Constructs a {@link SyncCallbacks}.</p>
     * @param bus The EventBus
     */
    @Inject
    public SyncCallbacks(Bus bus) {
        bus.register(this);
    }

/* Activity Lifecycle */
    @Override
    public void onActivityResumed(@Nonnull final Activity activity) {
        if (activity instanceof Syncable) {
            _sync_status_observer = new SyncStatusObserver() {
                @Override
                public void onStatusChanged(int _) {
                        activity.runOnUiThread(
                                new Runnable() {
                                    @Override
                                    public void run() {
                                        final String account_name = AccountUtilities.getChosenAccount(activity);
                                        if (TextUtils.isEmpty(account_name)) {
                                            setRefreshActionButtonState((Syncable) activity, false);
                                            return;
                                        }

                                        final Account account = new Account(account_name, AccountUtilities.ACCOUNT_TYPE);
                                        final boolean sync_active = ContentResolver.isSyncActive(account,
                                                BaseContract.CONTENT_AUTHORITY);
                                        final boolean sync_pending = ContentResolver.isSyncPending(account,
                                                BaseContract.CONTENT_AUTHORITY);
                                        setRefreshActionButtonState((Syncable) activity, sync_active || sync_pending);
                                    }
                                }
                        );
                }};
        }
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (activity instanceof Syncable && _sync_observer_handle != null) {
            // Removes the sync observer
            ContentResolver.removeStatusChangeListener(_sync_observer_handle);
            _sync_observer_handle = null;
        }
    }

/* ActionMenuCreatedEvent */
    /**
     * <p>Registers a sync status observer to monitor changes to the sync state once the action menu is created.</p>
     * @param _ The unused event
     */
    @Subscribe
    public void onActionMenuCreated(@Nonnull ActionMenuCreatedEvent _) {
        if (_sync_status_observer == null) return;

        // Defers observing sync status until after the action menu is created
        // Checks the current sync status
        _sync_status_observer.onStatusChanged(0);

        // Watches for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING | ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        _sync_observer_handle = ContentResolver.addStatusChangeListener(mask, _sync_status_observer);
    }

/* Helpers */
    /**
     * <p>Sets the Refresh action button to indicate progress while refreshing, and the Refresh action while at rest.</p>
     * @param is_refreshing whether the app is refreshing data
     */
    private void setRefreshActionButtonState(@Nonnull Syncable activity, boolean is_refreshing) {
        final MenuItem refresh_action = activity.actionMenu().findItem(R.id.menu_refresh);
        if (refresh_action != null) {
            if (is_refreshing)
                refresh_action.setActionView(R.layout.actionbar_indeterminate_progress);
            else
                refresh_action.setActionView(null);
        }
    }

    /** Used to remove {@link #_sync_status_observer} from the {@link ContentResolver} */
    @CheckForNull private Object _sync_observer_handle;
    /** Receives callback when {@link ContentResolver} sync status changes */
    @CheckForNull private SyncStatusObserver _sync_status_observer;
}
