package com.imminentmeals.android.base.ui;

import static org.fest.assertions.api.ANDROID.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import android.app.ActionBar;


/**
 * <p>Test suite for Home Activity.</p>
 * @author Dandre Allison
 */
@RunWith(RobolectricTestRunner.class)
public class HomeActivityTest {

    @Test
    public void testActionBarDisplay() {
        // Given
        final HomeActivity activity_under_testing = new HomeActivity();

        // When
        activity_under_testing.onCreate(null);

        // Then
        assertThat(activity_under_testing.getActionBar())
            .hasDisplayOptions(ActionBar.DISPLAY_SHOW_HOME|ActionBar.DISPLAY_SHOW_TITLE|ActionBar.DISPLAY_USE_LOGO);
    }
}
