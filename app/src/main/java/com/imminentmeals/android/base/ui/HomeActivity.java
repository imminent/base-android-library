package com.imminentmeals.android.base.ui;

import javax.annotation.CheckForNull;
import javax.annotation.OverridingMethodsMustInvokeSuper;

import android.os.Bundle;

import com.imminentmeals.android.base.R;
import com.imminentmeals.android.base.ui.base.BaseActivity;

/**
 * <p>Controller that provides the Home screen.</p>
 * @author Dandré Allison
 */
public class HomeActivity extends BaseActivity {

    @Override
    @OverridingMethodsMustInvokeSuper
    protected void onCreate(@CheckForNull Bundle icicle) {
        super.onCreate(icicle);
        getSupportActionBar().setTitle(R.string.title_home_screen);
    }
}
