package com.imminentmeals.android.base.ui;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.ListFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.imminentmeals.android.base.R;

/**
 * {@link android.app.ListFragment} that hosts a quick return bar. The bar can be found at
 * {@link com.keepandshare.android.R.id#quick_return_bar} and Views can be added to it.
 * @author Dandré Allison
 * @see <a href="https://plus.google.com/u/0/113735310430199015092/posts/1Sb549FvpJt">Quick Return post</a>
 */
public class QuickReturnListFragment extends ListFragment implements OnScrollListener {

    public QuickReturnListFragment() { }

/* Lifecycle */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
        final ViewGroup root_view = (ViewGroup) inflater.inflate(R.layout.fragment_quick_return, container, false);

        // Gathers views
        final View _quick_return_bar = root_view.findViewById(R.id.quick_return_bar);

        // Sets up animation
        _quick_return_bar_return_animator = ObjectAnimator.ofFloat(_quick_return_bar,
                "translationY",
                0);
        _quick_return_bar_return_animator.addListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        _state = QuickReturnState.RETURNING;
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        _state = QuickReturnState.ON_SCREEN;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        _state = QuickReturnState.OFF_SCREEN;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                }
        );
        _quick_return_bar_hide_animator = ObjectAnimator.ofFloat(_quick_return_bar,
                "translationY",
                getResources().getDimension(R.dimen.quick_return_bar_height));
        _quick_return_bar_hide_animator.addListener(
                new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                        _state = QuickReturnState.HIDING;
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        _state = QuickReturnState.OFF_SCREEN;
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                        _state = QuickReturnState.ON_SCREEN;
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) { }
                }
        );

        return root_view;
    }

    @Override
    public void onViewCreated(View view, Bundle icicle) {
        super.onViewCreated(view, icicle);

        final ListView list_view = getListView();
        list_view.setOnScrollListener(this);
        _last_first_child = list_view.getFirstVisiblePosition();
        final View first_child = list_view.getChildAt(_last_first_child);
        _last_y = first_child == null ? 0 : first_child.getTop();
    }

/* AbsListView.OnScrollListener */
    @Override
    public void onScroll(AbsListView list_view, int first_visible_child, int _, int __) {
        // Computes the scroll Y position
        final View first_child = list_view.getChildAt(first_visible_child);
        int y_position = first_child == null? 0 : first_child.getTop();

        switch (_state) {
            case OFF_SCREEN:
                // * Return quick return bar if first visible child is the same AND it's Y position increases
                // * Return quick return bar if first visible child changes to a previous sibling
                // * AND only if the quick return bar isn't already returning
                if (!quickReturnBarIsReturning()
                    && (first_visible_child == _last_first_child && y_position > _last_y)
                    || first_visible_child < _last_first_child)
                        _quick_return_bar_return_animator.start();
                break;

            case ON_SCREEN:
                // * Hide quick return bar if first visible child is the same AND it's Y position decreases
                // * Hide quick return bar if first visible child changes to a later sibling
                // * AND only if the quick return bar isn't already going away
                if (!quickReturnBarIsGoingAway()
                    && (first_visible_child == _last_first_child && y_position < _last_y)
                    || first_visible_child > _last_first_child)
                    _quick_return_bar_hide_animator.start();
                break;

            case RETURNING:
                // * Cancel return of quick return bar if first visible child is the same AND it's Y position decreases
                // * Cancel return of quick return bar if first visible child changes to a later sibling
                if ((first_visible_child == _last_first_child && y_position < _last_y)
                        || first_visible_child > _last_first_child) {
                    _quick_return_bar_return_animator.cancel();
                    _quick_return_bar_hide_animator.start();
                }
                break;

            case HIDING:
                // * Cancel hide of quick return bar if first visible child is the same AND it's Y position increases
                // * Cancel hide of quick return bar if first visible child changes to a previous sibling
                if ((first_visible_child == _last_first_child && y_position > _last_y)
                        || first_visible_child < _last_first_child) {
                    _quick_return_bar_hide_animator.cancel();
                    _quick_return_bar_return_animator.start();
                }
                break;
        }
        _last_first_child = first_visible_child;
        _last_y = y_position;
    }

    @Override
    public void onScrollStateChanged(AbsListView _, int __) { }

/* Transition status checks */
    /**
     * Checks if the quick return bar is transitioning back onto the screen.
     * @return {@code true} indicates that the quick return bar is returning
     */
    private boolean quickReturnBarIsReturning() {
        // This should be equivalent to checking that the quick return bar is RETURNING
        return _quick_return_bar_return_animator.isRunning() || _quick_return_bar_return_animator.isStarted();
    }

    /**
     * Checks if the quick return bar is transitioning off of the screen.
     * @return {@code true} indicates that the quick return bar is going away
     */
    private boolean quickReturnBarIsGoingAway() {
        return _quick_return_bar_hide_animator.isRunning() || _quick_return_bar_hide_animator.isStarted();
    }

    /**
     * Represents the state of the quick return bar.
     */
    private static enum QuickReturnState {
        /** Stable state indicating that the quick return bar is visible on screen */
        ON_SCREEN,
        /** Stable state indicating that the quick return bar is hidden off screen */
        OFF_SCREEN,
        /** Transitive state indicating that the quick return bar is coming onto the screen */
        RETURNING,
        /** Transitive state indicating that the quick return bar is going off of the screen */
        HIDING
    }
    /** The current state of the quick return bar */
    private QuickReturnState _state = QuickReturnState.ON_SCREEN;
    /** Tracks the last seen y-position of the first visible child */
    private int _last_y;
    /** Tracks the last seen first visible child */
    private int _last_first_child;
    /** Animates the quick return bar off of the screen */
    private ObjectAnimator _quick_return_bar_hide_animator;
    /** Animates the quick return bar onto the screen */
    private ObjectAnimator _quick_return_bar_return_animator;
}
