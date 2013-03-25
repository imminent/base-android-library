/*******************************************************************************
 * Copyright 2013 Chris Banes.
 *           2013 Dandre Allison
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.imminentmeals.android.base.ui.widget;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.LinearLayout;

/**
 * <p>An extension of {@link LinearLayout} that manages a {@link Checkable} state based off of a
 * child view that implements {@link Checkable}.
 * @author Chris Banes
 * @author Dandre Allison
 */
public class CheckableLinearLayout extends LinearLayout implements Checkable {

    /**
     * <p>Interface definition for a callback to be invoked when the checked state is changed.</p>
     */
    public interface OnCheckStateChangeListener {

        /**
         * <p>Callback triggered when the checked state changes.</p>
         * @param checkableView The view whose state has changed.
         * @param isChecked     The new checked state of checkableView.
         */
        void onCheckStateChanged(@Nonnull View checkable_view, boolean is_checked);
    }

    /**
     * <p>Constructs a {@link CheckableLinearLayout}.</p>
     * @param context The context in which to create the CheckableLinearLayout
     * @param attrs The set of attributes
     */
    public CheckableLinearLayout(@Nonnull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

/* Checkable Contract */
    @Override
    public boolean isChecked() {
        return _checked;
    }

    @Override
    public void setChecked(boolean is_checked) {
        // Avoids performing actions when the checked state is unchanged
        if (is_checked != _checked) {
            _checked = is_checked;
            // Triggers the background drawable to change state
            refreshDrawableState();

            if (_checked_state_listener != null)
                _checked_state_listener.onCheckStateChanged(this, _checked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!_checked);
    }

/* LinearLayout Callbacks */
    @Override
    public int[] onCreateDrawableState(int extra_space) {
        // Changes the background drawable state to reflect a change in checked state
        final int[] drawable_state = super.onCreateDrawableState(extra_space + 1);
        if (isChecked()) mergeDrawableStates(drawable_state, _CHECKED_STATE_SET);
        return drawable_state;
    }

/* OnCheckedChange*/
    /**
     * <p>Registers callback to trigger when the checked state of this view changes.<p>
     * @param listener The callback to trigger when checked state change
     */
    public void setCheckedStateListener(@Nullable OnCheckStateChangeListener listener) {
        _checked_state_listener = listener;
    }

    /** State identifier for identifying if the view is currentl checked */
    private static final int[] _CHECKED_STATE_SET = { android.R.attr.state_checked };
    /** The current checked state */
    private boolean _checked = false;
    /** Listener on which to invoke callback when the checked state changes */
    private OnCheckStateChangeListener _checked_state_listener;
}
