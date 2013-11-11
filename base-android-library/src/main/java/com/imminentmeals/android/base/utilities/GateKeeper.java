package com.imminentmeals.android.base.utilities;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Keeper of the {@linkplain Build.VERSION_CODES Android version gates}, used to guarantee non shall pass,
 * unless they are worthy.</p>
 * @author Dandre Allison
 */
@SuppressWarnings("UnusedDeclaration")
@ParametersAreNonnullByDefault
public final class GateKeeper {

    /**
     * <p>Determines if the current device supports Ice Cream Sandwich 4.0 API, also know as API level
     * {@value Build.VERSION_CODES#ICE_CREAM_SANDWICH}.</p>
     * @return {@code true} indicates that the current device supports Ice Cream Sandwich 4.0 APIs
     */
    public static boolean hasIcs() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * <p>Determines if the current device supports Jelly Bean 4.1 API, also know as API level
     * {@value Build.VERSION_CODES#JELLY_BEAN}.</p>
     * @return {@code true} indicates that the current device supports Jelly Bean 4.1 APIs
     */
    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    /**
     * <p>Determines if the current device supports Jelly Bean 4.2 API, also know as API level
     * {@value Build.VERSION_CODES#JELLY_BEAN_MR1}.</p>
     * @return {@code true} indicates that the current device supports Jelly Bean 4.2 APIs
     */
    public static boolean hasJellyBeanMr1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * <p>Determines if the the current device is a tablet. This approach uses the
     * {@link Configuration#screenLayout} to determine this.</p>
     * @param context the context from which to retrieve the screen layout
     * @return {@code true} indicates that the current device is a tablet
     */
    public static boolean isTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * <p>Determines if the current device is a <b>ICS or newer</b> tablet. This approach
     * uses {@link #isTablet(android.content.Context)} to determine if the device is a tablet, and
     * checks that it supports the Honeycomb API.</p>
     * @param context the context to use for {@link #isTablet(android.content.Context)}
     * @return {@code true} indicates that the current device is a tablet that supports the Honeycomb API
     * @see {@link #isTablet(android.content.Context)}
     */
    public static boolean isIcsTablet(Context context) {
        return hasIcs() && isTablet(context);
    }

    /**
     * <p>Determines if the the current device is a Google TV. This approach uses the
     * {@link android.content.pm.PackageManager#hasSystemFeature(String)} to determine this.</p>
     * @param context the context from which to determine if the device has a system feature
     * @return {@code true} indicates that the current device is a Google TV
     */
    public static boolean isGoogleTv(Context context) {
        return context.getPackageManager().hasSystemFeature("com.google.android.tv");
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link GateKeeper} class. */
    private GateKeeper() { }
}
