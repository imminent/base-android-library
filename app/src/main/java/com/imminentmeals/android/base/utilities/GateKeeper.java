package com.imminentmeals.android.base.utilities;

import javax.annotation.Nonnull;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

/**
 * <p>Keeper of the {@linkplain Build#VERSION_CODES Android version gates}, used to guarantee non shall pass,
 * unless they are worthy.</p>
 * @author Dandre Allison
 */
public final class GateKeeper {

    /**
     * <p>Determines if the current device supports Gingerbread 2.3 API, also know as API level
     * {@value Build.VERSION_CODES#GINGERBREAD}.</p>
     * @return {@code true} indicates that the current device supports Gingerbread 2.3 APIs
     */
    public static boolean hasGingerbread() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed behavior. Froyo is skipped
        // since Froyo 2.2 API is API level 8, which is the minimum supported API level, so any device running
        // this app will support Froyo 2.2 API
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    /**
     * <p>Determines if the current device supports Honeycomb 3.0 API, also know as API level
     * {@value Build.VERSION_CODES#HONEYCOMB}.</p>
     * @return {@code true} indicates that the current device supports Honeycomb 3.0 APIs
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * <p>Determines if the current device supports Honeycomb 3.1 API, also know as API level
     * {@value Build.VERSION_CODES#HONEYCOMB_MR1}.</p>
     * @return {@code true} indicates that the current device supports Honeycomb 3.1 APIs
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * <p>Determines if the current device supports Honeycomb 3.2 API, also know as API level
     * {@value Build.VERSION_CODES#HONEYCOMB_MR2}.</p>
     * @return {@code true} indicates that the current device supports Honeycomb 3.2 APIs
     */
    public static boolean hasHoneycombMR2() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2;
    }

    /**
     * <p>Determines if the current device supports Ice Cream Sandwich 4.0 API, also know as API level
     * {@value Build.VERSION_CODES#ICE_CREAM_SANDWICH}.</p>
     * @return {@code true} indicates that the current device supports Ice Cream Sandwich 4.0 APIs
     */
    public static boolean hasICS() {
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
    public static boolean hasJellyBeanMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * <p>Determines if the the current device is a tablet. This approach uses the
     * {@link Configuration#screenLayout} to determine this.</p>
     * @param context the context from which to retrieve the screen layout
     * @return {@code true} indicates that the current device is a tablet
     */
    public static boolean isTablet(@Nonnull Context context) {
        return (context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    /**
     * <p>Determines if the current device is a <b>Honeycomb or newer</b> tablet. This approach
     * uses {@link #isTablet(android.content.Context)} to determine if the device is a tablet, and
     * checks that it supports the Honeycomb API.</p>
     * @param context the context to use for {@link #isTablet(android.content.Context)}
     * @return {@code true} indicates that the current device is a tablet that supports the Honeycomb API
     * @see {@link #isTablet(android.content.Context)}
     */
    public static boolean isHoneycombTablet(@Nonnull Context context) {
        return hasHoneycomb() && isTablet(context);
    }

    /**
     * <p>Determines if the the current device is a Google TV. This approach uses the
     * {@link PackageManager#hasSystemFeature(String)} to determine this.</p>
     * @param context the context from which to determine if the device has a system feature
     * @return {@code true} indicates that the current device is a Google TV
     */
    public static boolean isGoogleTV(@Nonnull Context context) {
        return context.getPackageManager().hasSystemFeature("com.google.android.tv");
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link GateKeeper} class. */
    private GateKeeper() { }
}
