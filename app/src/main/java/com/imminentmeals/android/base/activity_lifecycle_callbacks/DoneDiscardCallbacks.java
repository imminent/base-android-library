/*
 * Copyright 2012 Dandre Allison
 *           2012 Roman Nurik
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.imminentmeals.android.base.activity_lifecycle_callbacks;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import butterknife.Views;

import com.imminentmeals.android.base.R;
import com.imminentmeals.android.base.utilities.SimpleActivityLifecycleCallbacks;

/**
 * <p>Defines the callbacks for the Done/Discard Bar pattern and creates the bar for {@link Activity}s that
 * are defined to use it. The Done/Discard Bar was introduced by Roman Nurik in the Google+ post linked below.</p>
 *
 * <p>The pattern is defined to be used in <em>two modes</em>: <b>Done Only</b> mode and Done/Discard mode. The default
 * mode is Done/Discard mode. In Done/Discard mode, the Action Bar is replaced with a custom View that displays two buttons
 * and resembles:
 * <pre>✕ DISCARD   |   ✓ DONE</pre>
 * In Done Only mode the discard button is hidden, it can provided through the Action Overflow menu if desired.
 * <pre>✓ DONE   |</pre>
 *
 * @see <a href="https://plus.google.com/113735310430199015092/posts/R49wVvcDoEW">Roman's Google+ post</a>
 * @see UsesDoneDiscardBar
 * @see UsesDoneDiscardBarDoneOnlyMode
 * @author Dandré Allison
 */
public class DoneDiscardCallbacks extends SimpleActivityLifecycleCallbacks {

    /**
     * <p>Marks the {@link Activity} to use the Done/Discard pattern<p>
     * <pre>✕ DISCARD   |   ✓ DONE</pre>
     * @author Dandré Allison
     */
    public interface UsesDoneDiscardBar extends UsesDoneDiscardBarDoneOnlyMode {
        /**
         * <p>Callback triggered when the "discard" button is clicked. This method doesn't need to call
         * {@link Activity#finish()}, as this is already handled by the Controller.</p>
         */
        void onDiscardClicked();
    }

    /**
     * <p>Marks the {@link Activity} to use the Done/Discard pattern in Done Only Mode.<p>
     * <pre>✓ DONE   |</pre>
     * @author Dandré Allison
     */
    public interface UsesDoneDiscardBarDoneOnlyMode {
        /**
         * <p>Callback triggered when the "done" button is clicked. This method doesn't need to call
         * {@link Activity#finish()}, as this is already handled by the Controller.</p>
         */
        void onDoneClicked();
    }

    /**
     * <p>Marks the {@link Activity} to provide custom icon and/or text for its Done/Discard Bar.</p>
     * @author Dandre Allison
     */
    public interface ProvidesCustomDoneButton {
        /**
         * <p>Provides a custom icon for the done button. If none is provided, the default icon will be used.</p>
         * @return The custom icon for the done button
         */
        @Nullable Drawable doneButtonIcon();
        /**
         * <p>Provides custom text for the done button. If none is provided, the default text will be used.</p>
         * @return The custom text for the done button
         */
        @Nullable CharSequence doneButtonText();
    }

    @Inject
    public DoneDiscardCallbacks() { }

/* Activity Lifecycles */
    @Override
    public void onActivityCreated(final Activity activity, @CheckForNull Bundle icicle) {
        // Defines types that are Activity and the Done/Discard Bar pattern
        /** Type-cast for Done/Discard Done Only mode Activity */
        class DoneDiscardBarDoneOnlyModeActivity extends ActivityWrapper implements UsesDoneDiscardBarDoneOnlyMode {

            DoneDiscardBarDoneOnlyModeActivity() {
                super(activity);
            }

            @Override
            public void onDoneClicked() {
                assert activity instanceof UsesDoneDiscardBarDoneOnlyMode;
                ((UsesDoneDiscardBarDoneOnlyMode) activity).onDoneClicked();
            }
        }
        /** Type-cast for Done/Discard Activity */
        class DoneDiscardBarActivity extends DoneDiscardBarDoneOnlyModeActivity implements UsesDoneDiscardBar {

            @Override
            public void onDiscardClicked() {
                assert activity instanceof UsesDoneDiscardBar;
                ((UsesDoneDiscardBar) activity).onDiscardClicked();
            }
        }

        // NOTE: order matters as anything that implements UsesDoneDiscardBar also implements UsesDoneDiscardBarDoneOnlyMode
        if (activity instanceof UsesDoneDiscardBar)
            createDoneDiscardBar(new DoneDiscardBarActivity(), R.layout.actionbar_custom_view_done_discard);
        else if (activity instanceof UsesDoneDiscardBarDoneOnlyMode)
            createDoneDiscardDoneOnlyBar(new DoneDiscardBarDoneOnlyModeActivity(), R.layout.actionbar_custom_view_done);
    }

/* Helpers */
    /**
     * <p>Defines the {@link Activity} methods that will be successfully delegated to a wrapped Activity, used to type-
     * cast an activity also implement a Done/Discard Bar marker interface.</p>
     * @author Dandre Allison
     */
    private static class ActivityWrapper implements ProvidesCustomDoneButton {

        /**
         * <p>Constructs the {@link ActivityWrapper}.</p>
         */
        public ActivityWrapper(@Nonnull Activity activity) {
            _activity = activity;
        }

        /**
         * <p>Get the Action Bar from the wrapped {@link Activity}.</p>
         * @return The Action Bar
         */
        ActionBar getActionBar() {
            return _activity.getActionBar();
        }

        /**
         * <p>Finishes the wrapped {@link Activity}.</p>
         */
        void finish() {
            _activity.finish();
        }

        /**
         * <p>Indicates when the wrapped {@link Activity} provides a custom done button.</p>
         * @return {@code true} indicates the wrapped activity provides a custom done button
         */
        boolean providesCustomDoneButton() {
            return _activity instanceof ProvidesCustomDoneButton;
        }

        @Override
        public CharSequence doneButtonText() {
            return providesCustomDoneButton()? ((ProvidesCustomDoneButton) _activity).doneButtonText() : null;
        }

        @Override
        public Drawable doneButtonIcon() {
            return providesCustomDoneButton()? ((ProvidesCustomDoneButton) _activity).doneButtonIcon() : null;
        }

        /** The {@link Activity}*/
        private final Activity _activity;
    }

    /**
     * <p>Adds the Done/Discard Bar to the Action Bar.</p>
     * @param activity An activity that is marked to use the Done/Discard Bar
     * @param layout The custom view layout
     * @return The created Done/Discard Bar
     */
    @Nonnull private <T extends ActivityWrapper & UsesDoneDiscardBar> View createDoneDiscardBar(
            @Nonnull final T activity,
            @Nonnegative int layout) {
        final View done_discard_bar = createDoneDiscardDoneOnlyBar(activity, layout);
        done_discard_bar.findViewById(R.id.actionbar_discard).setOnClickListener(
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View _) {
                            // "Discard"
                            activity.onDiscardClicked();
                            activity.finish();
                        }
                    });
        activity.getActionBar().setCustomView(done_discard_bar, new ActionBar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));
        return done_discard_bar;
    }

    /**
     * <p>Adds the Done/Discard in Done Only mode to the Action Bar.</p>
     * @param activity An activity that is marked to use the Done/Discard Bar in Done Only mode
     * @param layout The custom view layout
     * @return The created Done/Discard Bar
     */
    @Nonnull private <T extends ActivityWrapper & UsesDoneDiscardBarDoneOnlyMode> View createDoneDiscardDoneOnlyBar(
            @Nonnull final T activity,
            @Nonnegative int layout) {
        // Inflates a "Done/Discard" custom action bar view.
        final LayoutInflater inflater = (LayoutInflater) activity.getActionBar().getThemedContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        final View done_discard_bar = inflater.inflate(layout, null);
        final ViewGroup done_button = Views.findById(done_discard_bar, R.id.actionbar_done);
        // Allows for customization of the done button
        if (activity.providesCustomDoneButton()) {
            final TextView done_button_display = (TextView) done_button.getChildAt(0);
            final CharSequence done_text = ((ProvidesCustomDoneButton) activity).doneButtonText();
            if (done_text != null) done_button_display.setText(done_text);
            final Drawable done_icon = ((ProvidesCustomDoneButton) activity).doneButtonIcon();
            if (done_icon != null) done_button_display.setCompoundDrawablesWithIntrinsicBounds(done_icon, null, null, null);
        }
        done_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View _) {
                        // "Done"
                        activity.onDoneClicked();
                        activity.finish();
                    }
                });
        activity.getActionBar().setCustomView(done_discard_bar);
        activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                                                  ActionBar.DISPLAY_SHOW_CUSTOM | ActionBar.DISPLAY_SHOW_HOME
                                                  | ActionBar.DISPLAY_SHOW_TITLE);
        return done_discard_bar;
    }
}
