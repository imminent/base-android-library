package com.imminentmeals.android.base.utilities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.analytics.tracking.android.GoogleAnalytics;
import com.imminentmeals.android.base.R.string;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

import static com.imminentmeals.android.base.utilities.GateKeeper.isIcsTablet;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGW;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;

/**
 * <p>Assortment of UI helper methods.</p>
 * @author Dandré Allison
 */
@SuppressWarnings("UnusedDeclaration")
public final class UiUtilities {

    /**
     * <p>Enables and disables {@linkplain android.app.Activity activities} based on their "target device" meta-data
     * and the current device. Add {@literal <meta-data name="target device" value="tablet|phone|universal" />} to
     * an activity to specify its target device.</p>
     * @param context the current context of the device
     * @see GateKeeper#isIcsTablet(android.content.Context)
     */
    public static void configureDeviceSpecificActivities(@Nonnull Context context) {
        final PackageManager package_manager = context.getPackageManager();
        final boolean is_honeycomb_tablet = isIcsTablet(context);
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
                            Crouton.showText((Activity) context, string.did_opt_out, Style.CONFIRM);
                        else
                            Crouton.showText((Activity) context, string.did_opt_in, Style.CONFIRM);

                        if (callback != null)
                            callback.reportAppOptOut(did_opt_out);
                    }
                }
        );
    }

    /**
     * <p>Converts a {@link android.app.Fragment}'s arguments {@link Bundle} into an
     * {@link Intent}.</p>
     * @see #intentToFragmentArguments(android.content.Intent) for inverse of this function
     * @param arguments The fragment's arguments
     * @return The intent converted from the given bundle
     */
    @Nonnull public static Intent fragmentArgumentsToIntent(@Nullable Bundle arguments) {
        final Intent intent = new Intent();
        if (arguments == null) return intent;

        // Set the data URI to be the URI in the arguments, if one exists
        final Uri data = arguments.getParcelable("_uri");
        if (data != null)
            intent.setData(data);

        // Convert the Bundle into an Intent and remove the URI extra, as it was added as the data URI already
        intent.putExtras(arguments);
        intent.removeExtra("_uri");
        return intent;
    }

    /**
     * <p>Converts an {@link Intent} into a {@link Bundle} suitable for use as a
     * {@link android.app.Fragment}'s arguments.</p>
     * @see #fragmentArgumentsToIntent(android.os.Bundle) for inverse of this function
     * @param intent The intent
     * @return The bundle converted from the given intent
     */
    @Nonnull public static Bundle intentToFragmentArguments(@Nullable Intent intent) {
        final Bundle arguments = new Bundle();
        if (intent == null) return arguments;

        // Get the data URI to be the URI in the arguments, if one exists
        final Uri data = intent.getData();
        if (data != null)
            arguments.putParcelable("_uri", data);

        final Bundle extras = intent.getExtras();
        if (extras != null)
            arguments.putAll(extras);
        return arguments;
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link UiUtilities} class. */
    private UiUtilities() { }

    /** Tag to label {@link UiUtilities} log messages */
    private static final String _TAG = makeLogTag("UiUtils");
}
