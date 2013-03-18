package com.imminentmeals.android.base.utilities;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.annotation.Nonnull;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * <p>Provides ability to call {@link android.content.SharedPreferences.Editor#apply()} that is compatible with
 * all versions of Android, by using it only when possible and falling back to
 * {@link android.content.SharedPreferences.Editor#commit()} when it isn't available.</p>
 *  * @author Dandré Allison
 */
public final class SharedPreferencesUtilities {

    /**
     * <p>Apply the changes currently buffered in the {@link SharedPreferences.Editor} to the stored file.</p>
     * @param editor The buffered changes to apply
     */
    public static void apply(@Nonnull SharedPreferences.Editor editor) {
        if (_apply_method != null) {
            try {
                _apply_method.invoke(editor);
                return;
            } catch (InvocationTargetException _) {
                // fall through
            } catch (IllegalAccessException _) {
                // fall through
            }
        }
        editor.commit();
    }

    /**
     * <p>Uses reflection to find the implementation of {@link SharedPreferences} on the device
     * has an {@linkplain android.content.SharedPreferences.Editor#apply() apply method}. If it isn't
     * found then {@code null} is returned.</p>
     * @return {@code null} if the apply method doesn't exist in this implementation
     */
    private static Method findApplyMethod() {
        try {
            final Class<Editor> cls = SharedPreferences.Editor.class;
            return cls.getMethod("apply");
        } catch (NoSuchMethodException _) {
            // fall through
        }
        return null;
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link SharedPreferencesUtilities} class. */
    private SharedPreferencesUtilities() { }

    /** The {@linkplain android.content.SharedPreferences.Editor#apply() apply method} if this implementation
     * has one */
    private static final Method _apply_method = findApplyMethod();
}
