package com.imminentmeals.android.base;

import static android.util.Base64.DEFAULT;
import static android.util.Base64.decode;
import static android.util.Base64.encodeToString;
import static com.google.common.collect.Lists.newArrayList;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGE;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;

import java.io.File;
import java.io.IOException;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.AccountFlowCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.DoneDiscardCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.GoogleAnalyticsCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.InjectionCallbacks;
import com.imminentmeals.android.base.activity_lifecycle_callbacks.SyncCallbacks;
import com.imminentmeals.android.base.data.sync.SyncService;
import com.imminentmeals.android.base.ui.AccountActivity;
import com.imminentmeals.android.base.ui.HomeActivity;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.AndroidCookieStore;
import com.imminentmeals.android.base.utilities.CryptographyUtilities;
import com.imminentmeals.android.base.utilities.ObjectGraph.ObjectGraphApplication;
import com.imminentmeals.android.base.utilities.StringUtilities;
import com.squareup.otto.Bus;

import dagger.Lazy;
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
    @Inject /* package */Lazy<CookieStore> cookie_jar;
    /** Name to associate with the account {@link android.app.Activity} */
    public static final String ACCOUNT_ACTIVITY = "account";
    /** Name to associate with the name of the auth token stored in a cookie */
    public static final String COOKIE_AUTH_TOKEN = "cookie auth token";
    @Inject @Named(COOKIE_AUTH_TOKEN)/* package */String cookie_auth_token;

/* Lifecycle */
    @Override
    public void onCreate() {
        if (BuildConfig.DEBUG)
            StrictMode.enableDefaults();
        super.onCreate();

        // Creates the dependency injection object graph
        _object_graph = ObjectGraph.create(modules().toArray());
        _object_graph.inject(this);
        if (BuildConfig.DEBUG)
            LOGV(getClass().getSimpleName(), StringUtilities.joinAnd(", ", " and ",
                Lists.transform(modules(), new Function<Object, String>() {

                    @Override
                    @Nullable public String apply(@Nullable Object module) {
                        return module.getClass().getSimpleName() + ": "
                                + StringUtilities.joinAnd(",  ", " and ",
                                        module.getClass().getAnnotation(Module.class).entryPoints()) + "\n";
                    }
                })));

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

        // Enables auth token cookie persistence when an auth token key is provided
        if (!StringUtilities.isEmpty(cookie_auth_token)) {
            final CookieManager cookie_manager = new CookieManager(cookie_jar.get(), CookiePolicy.ACCEPT_ORIGINAL_SERVER);
            CookieHandler.setDefault(cookie_manager);
        }
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

                    // Activities
                    AccountActivity.class,
                    HomeActivity.class,

                    // Fragments
                    AccountActivity.ChooseAccountFragment.class,

                    // Services
                    AccountAuthenticatorService.class,
                    SyncService.class
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

        @Provides @Named(COOKIE_AUTH_TOKEN) String providesCookieAuthToken() {
            return "";
        }

        @Provides @Singleton CookieStore provideCookieStore(AndroidCookieStore cookie_jar) {
            return cookie_jar;
        }

        /** The context in which the app is running */
        private final Context _context;
    }

    /**
     * <p>Callback to include an extra Module in the {@link ObjectGraph}.</p>
     * @return
     */
    @OverridingMethodsMustInvokeSuper
    @Nonnull protected List<Object> modules() {
        return newArrayList((Object) new BaseAndroidLibraryModule(this));
    }

    /**
     * <p>Enables the HTTP response cache.</p>
     */
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

    /**
     * <p>Stores all buffered HTTP response caches.</p>
     */
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
