package com.imminentmeals.android.base.utilities;

import android.util.Log;

import com.imminentmeals.android.base.BuildConfig;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Helper functions that make logging more consistent throughout the app. Be sure to set {@code _LOG_PREFIX} to
 * something meaningful for your app, usually the app name. Debug and Verbose logging is enabled for applications that
 * have "android:debuggable=true" in the AndroidManifest.xml file. For apps built using SDK Tools r8 or later,
 * this means any debug build. Release builds built with r8 or later will have verbose and debug log messages
 * turned off.</p>
 *
 * <p>
 * <ul>
 *     <li>Make sure to put the {@link Throwable} before the message and formatting objects in calls. A common
 *     mistake is to place it last as is the {@link android.util.Log} convention.</li>
 *     <li>Vararg parameters (i.e. {@code Object...}) are not appended to the log message directly. You must
 *     insert them into the log message using the formatter like those used in
 *     {@link String#format(String, Object...)}</li>
 * </ul>
 * </p>
 *
 * <p>
 * Examples:
 * <ul>
 *  <li>{@code LOGD("tag", "hello world");}</li>
 *  <li>{@code LOGV("tag", cause);}</li>
 *  <li>{@code LOGI("tag", "%s %s", "hello", "world");}</li>
 *  <li>{@code LOGW("tag", cause, "%s %s", "hello", "world");}</li>
 *  <li>{@code LOGD("hello world");}</li>
 *  <li>{@code LOGV(cause);}</li>
 *  <li>{@code LOGI("%s %s", "hello", "world");}</li>
 *  <li>{@code LOGW(cause, "%s %s", "hello", "world");}</li>
 * </ul>
 * </p>
 */
@SuppressWarnings({"JavaDoc", "UnusedDeclaration"})
@ParametersAreNonnullByDefault
public final class LogUtilities {

    /**
     * <p>Constructs an appropriate tag string based on the given tag. Prefixes a log prefix to the tag, while
     * guaranteeing that the tag doesn't get too long.</p>
     * @param tag the given tag
     * @return the constructed tag
     */
    @Nonnull public static String makeLogTag(String tag) {
        return tag.length() > _MAX_LOG_TAG_LENGTH - _LOG_PREFIX_LENGTH
                ? _LOG_PREFIX + tag.substring(0, _MAX_LOG_TAG_LENGTH - _LOG_PREFIX_LENGTH - 1)
                : _LOG_PREFIX + tag;
    }

    /**
     * <p>Constructs an appropriate tag string based on the given class. Prefixes a log prefix to the tag, while
     * guaranteeing that the tag doesn't get too long.</p>
     *
     * <p>WARNING: Don't use this when obfuscating class names with ProGuard!</p>
     * @param type the given class
     * @return the constructed tag
     */
    @Nonnull public static String makeLogTag(Class<?> type) {
        return makeLogTag(type.getSimpleName());
    }

/* Debug */
    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for the given tag. Nothing will be
     * logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param message the message to log
     */
    public static void LOGD(final String tag, String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for the given tag.
     * Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void LOGD(final String tag, Throwable cause, String message) {
        if (BuildConfig.DEBUG) Log.d(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for the given tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     */
    public static void LOGD(final String tag, Throwable cause) {
        if (BuildConfig.DEBUG) Log.d(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for the
     * given tag. Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void LOGD(final String tag, Throwable cause, String format, Object... arguments) {
        if (BuildConfig.DEBUG) {
            final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                    + '\n'
                    + Log.getStackTraceString(cause);
            Log.d(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for the given tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void LOGD(final String tag, String format, Object... arguments) {
        if (BuildConfig.DEBUG) {
            final String message = arguments.length > 0 ? String.format(format, arguments) : format;
            Log.d(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for a default tag. Nothing will be
     * logged if not running a {@link BuildConfig#DEBUG debug build}. This is like {@link #LOGD(String,String)}, but
     * will use a default tag.</p>
     * @param message the message to log
     */
    public static void AUTOTAGLOGD(String message) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.d(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for a default tag.
     * Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGD(String, Throwable, String)}, but will use a default tag.</p>
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void AUTOTAGLOGD(Throwable cause, String message) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.d(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like {@link #LOGD(String, Throwable)},
     * but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGD(Throwable cause) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.d(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for a
     * default tag. Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGD(String, Throwable, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGD(Throwable cause, String format, Object... arguments) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) {
            final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                    + '\n'
                    + Log.getStackTraceString(cause);
            Log.d(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for a default tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGD(String, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void AUTOTAGLOGD(String format, Object... arguments) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) {
            final String message = arguments.length > 0 ? String.format(format, arguments) : format;
            Log.d(tag, message);
        }
    }

/* Verbose */
    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for the given tag. Nothing will be
     * logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param message the message to log
     */
    public static void LOGV(final String tag, String message) {
        if (BuildConfig.DEBUG) Log.v(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for the given tag.
     * Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void LOGV(final String tag, Throwable cause, String message) {
        if (BuildConfig.DEBUG) Log.v(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for the given tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     */
    public static void LOGV(final String tag, Throwable cause) {
        if (BuildConfig.DEBUG) Log.v(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for the
     * given tag. Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void LOGV(final String tag, Throwable cause, String format, Object... arguments) {
        if (BuildConfig.DEBUG) {
                        final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                    + '\n'
                    + Log.getStackTraceString(cause);
            Log.v(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for the given tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void LOGV(final String tag, String format, Object... arguments) {
        if (BuildConfig.DEBUG) {
                        final String message = arguments.length > 0 ? String.format(format, arguments) : format;
            Log.v(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for a default tag. Nothing will be
     * logged if not running a {@link BuildConfig#DEBUG debug build}. This is like {@link #LOGV(String,String)}, but
     * will use a default tag.</p>
     * @param message the message to log
     */
    public static void AUTOTAGLOGV(String message) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.v(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for a default tag.
     * Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGV(String, Throwable, String)}, but will use a default tag.</p>
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void AUTOTAGLOGV(Throwable cause, String message) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.v(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like {@link #LOGV(String, Throwable)},
     * but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGV(Throwable cause) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) Log.v(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for a
     * default tag. Nothing will be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGV(String, Throwable, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGV(Throwable cause, String format, Object... arguments) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) {
                        final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                    + '\n'
                    + Log.getStackTraceString(cause);
            Log.v(tag, message);
        }
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for a default tag. Nothing will
     * be logged if not running a {@link BuildConfig#DEBUG debug build}. This is like
     * {@link #LOGV(String, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void AUTOTAGLOGV(String format, Object... arguments) {
        final String tag = getAutomaticTag();
        if (BuildConfig.DEBUG) {
                        final String message = arguments.length > 0 ? String.format(format, arguments) : format;
            Log.v(tag, message);
        }
    }

/* Inform */
    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param message the message to log
     */
    public static void LOGI(final String tag, String message) {
        Log.i(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for the given tag.
     * </p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void LOGI(final String tag, Throwable cause, String message) {
        Log.i(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     */
    public static void LOGI(final String tag, Throwable cause) {
        Log.i(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for the
     * given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void LOGI(final String tag, Throwable cause, String format, Object... arguments) {
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.i(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void LOGI(final String tag, String format, Object... arguments) {
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.i(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for a default tag. This is like
     * {@link #LOGI(String,String)}, but
     * will use a default tag.</p>
     * @param message the message to log
     */
    public static void AUTOTAGLOGI(String message) {
        final String tag = getAutomaticTag();
        Log.i(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for a default tag.
     * This is like {@link #LOGI(String, Throwable, String)}, but will use a default tag.</p>
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void AUTOTAGLOGI(Throwable cause, String message) {
        final String tag = getAutomaticTag();
        Log.i(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGI(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGI(Throwable cause) {
        final String tag = getAutomaticTag();
        Log.i(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for a
     * default tag. This is like {@link #LOGI(String, Throwable, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGI(Throwable cause, String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.i(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for a default tag. This is like
     * {@link #LOGI(String, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void AUTOTAGLOGI(String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.i(tag, message);
    }

/* Warning */
    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param message the message to log
     */
    public static void LOGW(final String tag, String message) {
        Log.w(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for the given tag.
     * </p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void LOGW(final String tag, Throwable cause, String message) {
        Log.w(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     */
    public static void LOGW(final String tag, Throwable cause) {
        Log.w(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for the
     * given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void LOGW(final String tag, Throwable cause, String format, Object... arguments) {
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.w(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void LOGW(final String tag, String format, Object... arguments) {
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.w(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for a default tag. This is like
     * {@link #LOGW(String,String)}, but
     * will use a default tag.</p>
     * @param message the message to log
     */
    public static void AUTOTAGLOGW(String message) {
        final String tag = getAutomaticTag();
        Log.w(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGW(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGW(Throwable cause, String message) {
        final String tag = getAutomaticTag();
        Log.w(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGW(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGW(Throwable cause) {
        final String tag = getAutomaticTag();
        Log.w(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for a
     * default tag. This is like {@link #LOGW(String, Throwable, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGW(Throwable cause, String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.w(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for a default tag. This is like
     * {@link #LOGW(String, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void AUTOTAGLOGW(String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.w(tag, message);
    }

/* Error */
    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param message the message to log
     */
    public static void LOGE(final String tag, String message) {
        Log.e(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and message, for the given tag.
     * </p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     * @param message the message to log
     */
    public static void LOGE(final String tag, Throwable cause, String message) {
        Log.e(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param cause the given error cause
     */
    public static void LOGE(final String tag, Throwable cause) {
        Log.e(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause and formatted message, for the
     * given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     * @param cause the given error cause
     */
    public static void LOGE(final String tag, Throwable cause, String format, Object... arguments) {
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.e(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for the given tag.</p>
     * @param tag the given tag (assumed to not exceed the maximum tag length, use {@link #makeLogTag(String)} or
     *            {@link #makeLogTag(Class)} to guarantee this precondition)
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void LOGE(final String tag, String format, Object... arguments) {
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.e(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given message, for a default tag. This is like
     * {@link #LOGE(String,String)}, but
     * will use a default tag.</p>
     * @param message the message to log
     */
    public static void AUTOTAGLOGE(String message) {
        final String tag = getAutomaticTag();
        Log.e(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGE(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGE(Throwable cause, String message) {
        final String tag = getAutomaticTag();
        Log.e(tag, message, cause);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGE(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGE(Throwable cause) {
        final String tag = getAutomaticTag();
        Log.e(tag, Log.getStackTraceString(cause));
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the given error cause, for a default tag. This is
     * like {@link #LOGE(String, Throwable)}, but will use a default tag.</p>
     * @param cause the given error cause
     */
    public static void AUTOTAGLOGE(Throwable cause, String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = (arguments.length > 0 ? String.format(format, arguments) : format)
                + '\n'
                + Log.getStackTraceString(cause);
        Log.e(tag, message);
    }

    /**
     * <p>Logs a {@linkplain Log#DEBUG debug-level} message with the formatted message, for a default tag. This is like
     * {@link #LOGE(String, String, Object...)}, but will use a default tag.</p>
     * @param format the object (usually a format string, like those used in {@link String#format(String, Object...)})
     *               used to format the arguments
     * @param arguments a list (Varargs) of arguments to format; once formatted, these produce the formatted message
     */
    public static void AUTOTAGLOGE(String format, Object... arguments) {
        final String tag = getAutomaticTag();
                final String message = arguments.length > 0 ? String.format(format, arguments) : format;
        Log.e(tag, message);
    }

/* Helper code */
    /**
     * <p>Automatically generates a tag that is guaranteed to be within the maximum log tag length for use with log
     * messages. Particularly useful for the log methods that don't require a given tag.</p>
     * @return
     */
    private static String getAutomaticTag() {
        return makeLogTag(Thread.currentThread().getName());
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link LogUtilities} class. */
    private LogUtilities() { }

    /** Prefix to add to all log messages. Assumes that this prefix is shorter (by a good deal) than the max log tag
     * length */
    private static final String _LOG_PREFIX = "base_";
    /** Precomputed length of the log prefix */
    private static final int _LOG_PREFIX_LENGTH = _LOG_PREFIX.length();
    /** Length at which to begin truncating log tags */
    private static final int _MAX_LOG_TAG_LENGTH = 23;
}
