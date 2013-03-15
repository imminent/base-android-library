package com.imminentmeals.android.base.ui;

import android.os.Bundle;

import com.imminentmeals.android.base.R;
import com.imminentmeals.android.base.ui.base.BaseActivity;

/**
 * <p>Controller that provides the Home screen.</p>
 *
 * @author Dandré Allison
 */
public class HomeActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
    }

    @Override
    public CharSequence onCreateDescription() {
        return getString(R.string.title_app);
    }
}
