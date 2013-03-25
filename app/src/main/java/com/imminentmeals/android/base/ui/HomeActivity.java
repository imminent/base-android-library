package com.imminentmeals.android.base.ui;

import javax.annotation.CheckForNull;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.imminentmeals.android.base.R;
import com.imminentmeals.android.base.utilities.ActionUtilities;
import com.imminentmeals.android.base.utilities.HelpUtilities;

/**
 * <p>Controller that provides the Home screen.</p>
 * @author Dandré Allison
 */
public class HomeActivity extends Activity {

/* Lifecycle */
    @Override
    protected void onCreate(@CheckForNull Bundle icicle) {
        super.onCreate(icicle);
        getActionBar().setTitle(R.string.title_home_screen);
    }

/* Activity Callbacks */
    @Override
    public boolean onCreateOptionsMenu(Menu actions) {
        super.onCreateOptionsMenu(actions);
        ActionUtilities.generateActions(getMenuInflater(), actions, 0);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem action) {
        final int action_id = action.getItemId();
        if (action_id == R.id.menu_help) {
            HelpUtilities.showAbout(this);
            return true;
        }
        return super.onOptionsItemSelected(action);
    }
}
