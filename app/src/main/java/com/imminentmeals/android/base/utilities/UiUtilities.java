package com.imminentmeals.android.base.utilities;

import static com.imminentmeals.android.base.utilities.GateKeeper.isHoneycombTablet;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGW;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.imminentmeals.android.base.R;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;

/**
 * <p>Assortment of UI helper methods.</p>
 * @author Dandré Allison
 */
public final class UiUtilities {

    /**
     * <p>Enables and disables {@linkplain android.app.Activity activities} based on their "target device" meta-data
     * and the current device. Add {@literal <meta-data name="target device" value="tablet|phone|universal" />} to
     * an activity to specify its target device.</p>
     * @param context the current context of the device
     * @see #isHoneycombTablet(android.content.Context)
     */
    public static void configureDeviceSpecificActivities(@Nonnull Context context) {
        final PackageManager package_manager = context.getPackageManager();
        final boolean is_honeycomb_tablet = isHoneycombTablet(context);
        try {
            final ActivityInfo[] activity_info = package_manager.getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES | PackageManager.GET_META_DATA).activities;
            for (ActivityInfo info : activity_info) {
                String target_device = info.metaData.getString("target device");
                if (target_device == null) break;
                target_device = target_device.toLowerCase();
                final boolean is_for_tablet = target_device.equals("tablet");
                final boolean is_for_phone = target_device.equals("phone");
                final String class_name = info.name;
                final boolean should_disable = (is_honeycomb_tablet && is_for_phone)
                        || (!is_honeycomb_tablet && is_for_tablet);
                package_manager.setComponentEnabledSetting(new ComponentName(context, Class.forName(class_name)),
                        should_disable
                            ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                            : PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);
            }
        } catch (PackageManager.NameNotFoundException error) {
            LOGW(_TAG, error.getCause());
        } catch (ClassNotFoundException error) {
            LOGW(_TAG, error.getCause());
        }
    }

    /**
     * <p>Opts out of Analytics tracking.</p>
     * @param context the context from which to retrieve {@link GoogleAnalytics}
     * @param callback the callback that after the opt out request is served
     */
    public static void requestOptOut(@Nonnull final Context context,
                                     @Nullable final GoogleAnalytics.AppOptOutCallback callback) {
        GoogleAnalytics.getInstance(context).requestAppOptOut(
                new GoogleAnalytics.AppOptOutCallback() {
                    @Override
                    public void reportAppOptOut(boolean did_opt_out) {
                        if (!(context instanceof Activity))
                            return;

                        if (did_opt_out)
                            Crouton.showText((Activity) context, R.string.did_opt_out, Style.CONFIRM);
                        else
                            Crouton.showText((Activity) context, R.string.did_opt_in, Style.CONFIRM);

                        if (callback != null)
                            callback.reportAppOptOut(did_opt_out);
                    }
                }
        );
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link UiUtilies} class. */
    private UiUtilities() { }

    /** Tag to label {@link UiUtilities} log messages */
    private static final String _TAG = makeLogTag("UiUtils");
}
