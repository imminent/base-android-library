package com.imminentmeals.android.base;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import com.actionbarsherlock.ActionBarSherlock;
import com.actionbarsherlock.internal.ActionBarSherlockCompat;
import com.actionbarsherlock.internal.ActionBarSherlockNative;

/**
 * <p>{@link RobolectricTestRunner} that setups to not fail with {@link ActionBarSherlock}.</p>
 * @author Dandre Allison
 */
public class ActionBarSherlockTestRunner extends RobolectricTestRunner {
    public ActionBarSherlockTestRunner(Class<?> test_class) throws InitializationError {
      super(test_class);

      // Setups ActionBarSherlock for testing
      ActionBarSherlock.registerImplementation(ActionBarSherlockRobolectric.class);
      ActionBarSherlock.unregisterImplementation(ActionBarSherlockNative.class);
      ActionBarSherlock.unregisterImplementation(ActionBarSherlockCompat.class);
    }
}
