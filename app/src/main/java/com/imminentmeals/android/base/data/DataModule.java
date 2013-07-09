package com.imminentmeals.android.base.data;

import android.content.ContentResolver;
import android.content.Context;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Dandré Allison
 */
@ParametersAreNonnullByDefault
public class DataModule {

    public static Context getApplicationContext() {
        return get()._context;
    }

    public static ContentResolver getContentResolver() {
        return get()._context.getContentResolver();
    }

    public static DataModule get() {
        if(_instance == null)
            throw new IllegalStateException("DataModule not initialized before get().");
        return _instance;
    }

    /**
     * <p>Initializes {@link DataModule}, this should be the very first thing called in onCreate of an
     * Android application implementation.</p>
     *
     * @param context
     */
    public static void init(Context context) {
        if(_instance == null)
            _instance = new DataModule(context);
    }

    private DataModule(Context context){
        _context = context.getApplicationContext();
    }

    private Context _context;
    private static DataModule _instance;
}
