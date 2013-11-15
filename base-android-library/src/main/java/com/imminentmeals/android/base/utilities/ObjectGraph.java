package com.imminentmeals.android.base.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;

import javax.annotation.Nonnull;

/**
 * <p>Retrieves the {@link dagger.ObjectGraph} and injects dependencies.</p>
 * @author Dandré Allison
 */
public final class ObjectGraph {

    /**
     * <p>An {@link android.app.Application} that wants to inject dependencies from an
     * {@link dagger.ObjectGraph object graph} must implement {@link ObjectGraphApplication}.</p>
     */
    public interface ObjectGraphApplication {
        /**
         * <p>Injects dependencies into the given Object.</p>
         */
        void inject(@Nonnull Object dependent);
    }

    /**
     * <p>Injects the dependencies for the given {@link Activity}.</p>
     * @param activity The given activity
     */
    public static void inject(@Nonnull Activity activity) {
        ((ObjectGraphApplication) activity.getApplication()).inject(activity);
    }

    /**
     * <p>Injects the dependencies for the given {@link Fragment}.</p>
     * @param fragment The given fragment
     */
    public static void inject(@Nonnull Fragment fragment) {
        final Activity activity = fragment.getActivity();
        if (activity == null)
            throw new IllegalStateException("Attempting to get Activity before it has been attached to "
                    + fragment.getClass().getName());
        ((ObjectGraphApplication) activity.getApplication()).inject(fragment);
    }

    /**
     * <p>Injects the dependencies for the given {@link Object} from the given {@link Context}.</p>
     * @param context The given context
     * @param object The given object
     */
    public static void inject(@Nonnull Context context, @Nonnull Object object) {
        ((ObjectGraphApplication) context.getApplicationContext()).inject(object);
    }

    /**
     * <p>Injects the dependencies for the given {@link Service}.</p>
     * @param service The given service
     */
    public static void inject(@Nonnull Service service) {
        ((ObjectGraphApplication) service.getApplication()).inject(service);
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link ObjectGraph} class. */
    private ObjectGraph() { }
}