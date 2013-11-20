package com.imminentmeals.android.base.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;

import javax.annotation.Nonnull;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGD;

/**
 * <p>Retrieves the {@link dagger.ObjectGraph} and injects dependencies.</p>
 * @author Dandré Allison
 */
public final class ObjectGraph {

    /**
     * <p>An {@link android.app.Application} that wants to inject dependencies from an
     * {@linkplain dagger.ObjectGraph object graph} must implement {@link ObjectGraphApplication}.</p>
     */
    public interface ObjectGraphApplication {
        /**
         * <p>Injects dependencies into the given Object.</p>
         */
        void inject(@Nonnull Object dependent);

       /**
        * <p>Pluses the given module onto to {@link ObjectGraphApplication}'s
        * {@linkplain dagger.ObjectGraph object graph}.</p>
        * @param module The given module to plus onto the object graph Application's object graph
        * @return The resulting object graph (note, this doesn't modify the object graph
        *         Application's object graph
        */
        @Nonnull dagger.ObjectGraph plus(Object... module);
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
        if (context.getApplicationContext() != null)
            ((ObjectGraphApplication) context.getApplicationContext()).inject(object);
        else AUTOTAGLOGD("Application context was null in %s, unable to inject %s", object, context);
    }

    /**
     * <p>Injects the dependencies for the given {@link Service}.</p>
     * @param service The given service
     */
    public static void inject(@Nonnull Service service) {
        if (service.getApplication() != null)
            ((ObjectGraphApplication) service.getApplication()).inject(service);
        else AUTOTAGLOGD("Application context was null, unable to inject %s", service);
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link ObjectGraph} class. */
    private ObjectGraph() { }
}
