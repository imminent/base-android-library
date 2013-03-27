package com.imminentmeals.android.base;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.decode;
import static android.util.Base64.encodeToString;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGE;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.SecretKey;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.StrictMode;
import android.preference.PreferenceManager;

import com.imminentmeals.android.base.activity_lifecycle_callbacks.AccountFlowCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.DoneDiscardCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.GoogleAnalyticsCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.InjectionCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.SyncCallbacks;
import com.imminentmeals.android.base.ui.AccountActivity;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.CryptographyUtilities;
import com.imminentmeals.android.base.utilities.ObjectGraph.ObjectGraphApplication;
import com.squareup.otto.Bus;

import dagger.Module;
import dagger.ObjectGraph;
import dagger.Provides;

/**
 * <p>Enables {@link StrictMode} during debugging, creates the {@linkplain ObjectGraph object graph} for dependency
 * injection, and registers {@linkplain Application#ActivityLifecycleCallbacks Activity lifecycle callbacks}.
 * @author Dandré Allison
 */
public class BaseAndroidLibraryApplication extends Application implements ObjectGraphApplication {
    @Inject /* package */SharedPreferences settings;
    @Inject /* package */AccountFlowCallbacks account_flow_callbacks;
    @Inject /* package */GoogleAnalyticsCallbacks google_analytics_callbakcs;
    @Inject /* package */InjectionCallbacks injection_callbacks;
    @Inject /* package */SyncCallbacks sync_callbacks;
    @Inject /* package */DoneDiscardCallbacks done_discard_callbacks;
    /** Name to associate with the account {@link android.app.Activity} */
    public static final String ACCOUNT_ACTIVITY = "account";

/* Lifecycle */
    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults();
        super.onCreate();

        // Creates the dependency injection object graph
        _object_graph = ObjectGraph.create(new BaseAndroidLibraryModule(this));
        _object_graph.inject(this);

        // TODO: setDefaultPreferences here

        // Registers the Activity lifecycle callbacks
        registerActivityLifecycleCallbacks(account_flow_callbacks);
        registerActivityLifecycleCallbacks(google_analytics_callbakcs);
        registerActivityLifecycleCallbacks(injection_callbacks);
        registerActivityLifecycleCallbacks(sync_callbacks);
        registerActivityLifecycleCallbacks(done_discard_callbacks);

        // Establishes a secret key on initial launch
        if (!settings.contains(CryptographyUtilities.KEY_SECRET_KEY))
            try {
                settings.edit()
                    .putString(CryptographyUtilities.KEY_SECRET_KEY,
                                   encodeToString(CryptographyUtilities.generateKey().getEncoded(), DEFAULT))
                    .apply();
            } catch (NoSuchAlgorithmException error) {
                LOGE(error);
            }

        // Enables HTTP response caching
        enableHttpResponseCache();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        flushHttpCache();
    }

/* Application Callbacks */
    @Override
    public void onTrimMemory(int level) {
        // TODO: look into onTrimMemory and onLowMemory
        super.onTrimMemory(level);
        switch (level) {
            case Application.TRIM_MEMORY_COMPLETE:
                flushHttpCache();
                break;
            default: break;
        }
    }

/* ObjectGraphApplication Contract */
    @Override
    public void inject(Object dependent) {
        _object_graph.inject(dependent);
    }

/* Dependency Injection Module */
    /**
     * <p>Module class that defines injection entry points that aren't defined in the
     * AndroidManifest (like Fragment and non-Android classes). This allows Dagger to inject
     * their dependencies. It also {@link Provides} dependencies that can be injected at entry
     * points.</p>
     *
     * @author Dandré Allison
     */
    @Module(
            entryPoints = {
                    // Application
                    BaseAndroidLibraryApplication.class,

                    // Fragments
                    AccountActivity.ChooseAccountFragment.class
            },
            includes = {
                    // Module generated by dagger-androidmanifest-plugin
                    ManifestModule.class
            }
    )
    protected static class BaseAndroidLibraryModule {

        /**
         * <p>Constructs the module using the {@link Context} to retrieve the application context
         * from which to retrieve context-dependent dependencies.</p>
         * @param context The context from which to retrieve the application context
         */
        public BaseAndroidLibraryModule(Context context) {
            _context = context.getApplicationContext();
        }

        @Provides @Singleton Bus provideBus() {
            return new Bus();
        }

        @Provides @Singleton AbstractAccountAuthenticator provideAccountAuthenticator(Context context) {
            return new AccountAuthenticatorService.FakeAccountAuthenticator(context);
        }

        @Provides SharedPreferences provideSharedPreferences() {
            return PreferenceManager.getDefaultSharedPreferences(_context);
        }

        @Provides AccountManager provideAccountManager() {
            return AccountManager.get(_context);
        }

        @Provides Context provideContext() {
            return _context;
        }

        @Provides Account provideTestAccount() {
            return new Account("test47", "fake:" + AccountUtilities.ACCOUNT_TYPE);
        }

        @Provides SecretKey provideSecretKey(final SharedPreferences settings) {
            return new SecretKey() {

                @Override
                public String getAlgorithm() {
                    return CryptographyUtilities.AES;
                }

                @Override
                public String getFormat() {
                    return null;
                }

                @Override
                public byte[] getEncoded() {
                    return decode(settings.getString(CryptographyUtilities.KEY_SECRET_KEY, ""), DEFAULT);
                }

                private static final long serialVersionUID = 4664651834175207772L;
            };
        }

        @SuppressWarnings("rawtypes")
        @Provides @Named(ACCOUNT_ACTIVITY) Class provideAccountActivity() {
            return AccountActivity.class;
        }

        /** The context in which the app is running */
        private final Context _context;
    }

    protected void enableHttpResponseCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    final File cache_directory = new File(getCacheDir(), "http");
                    HttpResponseCache.install(cache_directory, _CACHE_SIZE);
                } catch (IOException error) {
                    LOGE(error.getCause(), "HTTP cache installation failed.");
                }
            }
        }).start();
    }

    protected void flushHttpCache() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final HttpResponseCache cache = HttpResponseCache.getInstalled();
                if (cache != null)
                    cache.flush();
            }
        }).start();
    }

    /** Application's object graph for handling dependency injection */
    private ObjectGraph _object_graph;
    /** One Mebibyte, is 2^20 = 1024 * 1024 = 1,048,576 bytes (MiB) */
    private static final long _MEBIBYTE = 1024 * 1024;
    /** Sets cache size to 10 MiB */
    private static final long _CACHE_SIZE = 10 * _MEBIBYTE;
}
