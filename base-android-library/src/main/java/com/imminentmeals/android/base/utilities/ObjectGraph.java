package com.imminentmeals.android.base.utilities;

import android.app.Activity;
import android.app.Fragment;
import android.app.Service;
import android.content.Context;
import android.support.v4.util.ArrayMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGD;

/**
 * <p>Retrieves the {@link dagger.ObjectGraph} and injects dependencies.</p>
 * @author Dandré Allison
 */
@ParametersAreNonnullByDefault
public final class ObjectGraph {

    /**
     * <p>An {@link android.app.Application} that wants to inject dependencies from an
     * {@linkplain dagger.ObjectGraph object graph} must implement {@link ObjectGraphApplication}.</p>
     */
    public interface ObjectGraphApplication {
        /**
         * <p>Injects dependencies into the given Object.</p>
         */
        void inject(Object dependent);

       /**
        * <p>Pluses the given module onto to {@link ObjectGraphApplication}'s
        * {@linkplain dagger.ObjectGraph object graph}.</p>
        * @param module The given module to plus onto the object graph Application's object graph
        * @return The resulting object graph (note, this doesn't modify the object graph
        *         Application's object graph
        */
        @Nonnull dagger.ObjectGraph plus(@Nullable Object... module);
    }

    public interface ObjectGraphActivity {
       /**
        * <p>The Activity modules to plus.</p>
        */
        @Nonnull List<Object> modules();
    }

    /**
     * <p>Injects the dependencies for the given {@link Activity}.</p>
     * @param activity The given activity
     */
    public static void inject(Activity activity) {
        inject(activity, activity);
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
        inject(activity, fragment);
    }

    /**
     * <p>Injects the dependencies for the given {@link Object} from the given {@link Context}.</p>
     * @param context The given context
     * @param target The given object
     */
    public static void inject(@Nonnull Context context, @Nonnull Object target) {
        if (context instanceof Activity)
            inject((Activity) context, target);
        else if (context.getApplicationContext() != null)
            ((ObjectGraphApplication) context.getApplicationContext()).inject(target);
        else AUTOTAGLOGD("Application context was null in %s, unable to inject %s", target, context);
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

   /**
    * <p>Injects the dependencies for the given {@link Object} from the given {@link Activity}.</p>
    * @param activity The given activity
    * @param target The given object
    */
    private static void inject(Activity activity, Object target) {
        if (activity instanceof ObjectGraphActivity) {
            final dagger.ObjectGraph activity_object_graph = _activity_object_graphs.get(activity.getClass());
            final dagger.ObjectGraph object_graph =
                activity_object_graph != null
                    ? activity_object_graph
                    : ((ObjectGraphApplication) activity.getApplication())
                        .plus(((ObjectGraphActivity) activity).modules().toArray());
            _activity_object_graphs.put(activity.getClass(), object_graph);
            object_graph.inject(target);
        } else ((ObjectGraphApplication) activity.getApplication()).inject(target);
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link ObjectGraph} class. */
    private ObjectGraph() { }

    /** Map of Activity -> Activity object graph */
    private static final Map<Class<? extends Activity>, dagger.ObjectGraph> _activity_object_graphs =
        new ArrayMap<>(2);
}
