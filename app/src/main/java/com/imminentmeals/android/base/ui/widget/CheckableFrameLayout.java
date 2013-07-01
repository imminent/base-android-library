package com.imminentmeals.android.base.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * <p>An extension of {@link android.widget.LinearLayout} that manages a {@link android.widget.Checkable} state based off of a
 * child view that implements {@link android.widget.Checkable}.
 * @author Chris Banes
 * @author Dandre Allison
 */
public class CheckableFrameLayout extends FrameLayout implements Checkable {

    /**
     * <p>Interface definition for a callback to be invoked when the checked state is changed.</p>
     */
    public interface OnCheckStateChangeListener {

        /**
         * <p>Callback triggered when the checked state changes.</p>
         * @param checkable_view The view whose state has changed.
         * @param is_checked     The new checked state of checkableView.
         */
        void onCheckStateChanged(@Nonnull View checkable_view, boolean is_checked);
    }

    /**
     * <p>Constructs a {@link CheckableLinearLayout}.</p>
     * @param context The context in which to create the CheckableLinearLayout
     * @param attrs The set of attributes
     */
    public CheckableFrameLayout(@Nonnull Context context, @Nullable AttributeSet attrs) {
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

/* FrameLayout Callbacks */
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
