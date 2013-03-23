package com.imminentmeals.android.base.ui;

import javax.annotation.CheckForNull;

import com.imminentmeals.android.base.R;

import android.app.Activity;
import android.os.Bundle;

/**
 * <p>Controller that provides the Home screen.</p>
 * @author Dandré Allison
 */
public class HomeActivity extends Activity {

    @Override
    protected void onCreate(@CheckForNull Bundle icicle) {
        super.onCreate(icicle);
        getActionBar().setTitle(R.string.title_home_screen);
    }
}
