package com.imminentmeals.android.base.utilities;

import android.app.Activity;
import android.app.Service;
import android.support.v4.app.Fragment;

/**
 * <p>Retrieves the {@link dagger.ObjectGraph} and injects dependencies.</p>
 * @author Dandré Allison
 */
public final class ObjectGraph {

    /**
     * <p>An {@link android.app.Application} that wants to present an {@link dagger.ObjectGraph object graph}
     * must implement {@link ObjectGraphApplication}.</p>
     */
    public interface ObjectGraphApplication {
        /**
         * <p>Retrieves the {@link dagger.ObjectGraph} from the {@link ObjectGraphApplication}.</p>
         * @return The object graph
         */
        dagger.ObjectGraph objectGraph();
    }

    /**
     * <p>Retrieves the {@link dagger.ObjectGraph} on behave of the given {@link android.app.Application}.</p>
     * @param application The given application
     * @return The object graph
     */
    public static dagger.ObjectGraph from(ObjectGraphApplication application) {
        return application.objectGraph();
    }

    /**
     * <p>Retrieves the {@link dagger.ObjectGraph} on behave of the given {@link android.app.Activity}.</p>
     * @param activity The given activity
     * @return The object graph
     */
    public static dagger.ObjectGraph from(Activity activity) {
        return from((ObjectGraphApplication) activity.getApplication());
    }

    /**
     * <p>Injects the dependencies for the given {@link Activity}.</p>
     * @param activity The given activity
     */
    public static void inject(Activity activity) {
        from(activity).inject(activity);
    }

    /**
     * <p>Retrieves the {@link dagger.ObjectGraph} on behave of the given {@link Fragment}.</p>
     * @param fragment The given fragment
     * @return The object graph
     */
    public static dagger.ObjectGraph from(Fragment fragment) {
        final Activity activity = fragment.getActivity();
        if (activity == null)
            throw new IllegalStateException("Attempting to get Activity before it has been attached to "
                    + fragment.getClass().getName());
        return from(activity);
    }

    /**
     * <p>Injects the dependencies for the given {@link Fragment}.</p>
     * @param fragment The given fragment
     */
    public static void inject(Fragment fragment) {
        from(fragment).inject(fragment);
    }

    /**
     * <p>Retrieves the {@link dagger.ObjectGraph} on behave of the given {@link android.app.Service}.</p>
     * @param service The given service
     * @return The object graph
     */
    public static dagger.ObjectGraph from(Service service) {
        return from((ObjectGraphApplication) service.getApplication());
    }

    /**
     * <p>Injects the dependencies for the given {@link Service}.</p>
     * @param service The given service
     */
    public static void inject(Service service) {
        from(service).inject(service);
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link ObjectGraph} class. */
    private ObjectGraph() { }
}
