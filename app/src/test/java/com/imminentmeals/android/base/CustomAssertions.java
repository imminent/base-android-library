package com.imminentmeals.android.base;

import org.fest.assertions.api.ANDROID;

import com.actionbarsherlock.app.ActionBar;


/**
 * <p>Adds custom assertions to {@linkplain ANDROID Android assertions}.</p>
 * @author Dandre Allison
 */
public final class CustomAssertions extends ANDROID {

    /**
     * <p>Provides a backward-compatible {@link ActionBar} assertion test utility.</p>
     * @param actual The Action Bar under testing
     * @return The assertion utility for chaining assertions
     */
    public static ActionBarAssert assertThat(ActionBar actual) {
        return new ActionBarAssert(actual);
    }

}
