package com.imminentmeals.android.base.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.imminentmeals.android.base.R.id;
import com.imminentmeals.android.base.R.string;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.SyncCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.SyncCallbacks.Syncable;
import com.imminentmeals.android.base.utilities.ActionUtilities;
import com.imminentmeals.android.base.utilities.HelpUtilities;
import com.squareup.otto.Bus;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.inject.Inject;

/**
 * <p>Controller that provides the Home screen.</p>
 * @author Dandré Allison
 */
public class HomeActivity extends Activity implements Syncable {
    @Inject /* package */Bus bus;

/* Lifecycle */
    @Override
    protected void onCreate(@CheckForNull Bundle icicle) {
        super.onCreate(icicle);
        getActionBar().setTitle(string.title_home_screen);
    }

/* Activity Callbacks */
    @Override
    public boolean onCreateOptionsMenu(Menu actions) {
        super.onCreateOptionsMenu(actions);
        ActionUtilities.generateActionsWithSyncStatus(getMenuInflater(), actions, 0);
        _action_menu = actions;
        bus.post(new SyncCallbacks.ActionMenuCreatedEvent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem action) {
        final int action_id = action.getItemId();
        if (action_id == id.action_help) {
            HelpUtilities.showAbout(this);
            return true;
        }
        return super.onOptionsItemSelected(action);
    }

/* Syncable Contract */
    @Override
    @Nonnull public Menu actionMenu() {
        if (_action_menu == null)
            throw new IllegalStateException("Action menu accessed before being assigned in onCreateOptionsMenu(...)");
        return _action_menu;
    }

    /** The action menu */
    private Menu _action_menu;
}
