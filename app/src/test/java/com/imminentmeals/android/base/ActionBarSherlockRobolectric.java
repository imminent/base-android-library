// Copyright 2012 Square, Inc.
package com.imminentmeals.android.base;

import com.actionbarsherlock.internal.ActionBarSherlockCompat;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.internal.ActionBarSherlockNative;
import com.actionbarsherlock.view.MenuInflater;

import static org.robolectric.Robolectric.shadowOf;

/**
 * <p>During initialization, {@link ActionBarSherlock} figures out which {@link
 * com.actionbarsherlock.app.ActionBar} to use based on the API level. It does this by checking the
 * Build.Version.SDK_INT value which depends on the hidden <i>SystemProperties</i> class.</p>
 *
 * <p>Because Roboelectric does not have this, it always returns <code>0</code> for its API level
 * causing {@link ActionBarSherlock} to crash. This class helps resolve this issue by providing
 * an {@link ActionBarSherlockNative} implementation for API level 0.</p>
 * @see ActionBarSherlock#registerImplementation(Class)
 */
@ActionBarSherlock.Implementation(api = 0)
public class ActionBarSherlockRobolectric extends ActionBarSherlockCompat {
  final private ActionBar actionBar;

  public ActionBarSherlockRobolectric(Activity activity, int flags) {
    super(activity, flags);
    actionBar = new MockActionBar(activity);
  }

  @Override public void setContentView(int layoutResId) {
    LayoutInflater layoutInflater = LayoutInflater.from(mActivity);
    View contentView = layoutInflater.inflate(layoutResId, null);

    shadowOf(mActivity).setContentView(contentView);
  }

  @Override public void setContentView(View view) {
    shadowOf(mActivity).setContentView(view);
  }

  @Override public ActionBar getActionBar() {
    return actionBar;
  }

  @Override protected Context getThemedContext() {
    return mActivity;
  }

  @Override public MenuInflater getMenuInflater() {
    if (mMenuInflater == null) {
      mMenuInflater = new SherlockMenuInflater(mActivity);
    }
    return mMenuInflater;
  }
}
