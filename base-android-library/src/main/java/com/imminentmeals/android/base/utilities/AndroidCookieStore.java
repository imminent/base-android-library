package com.imminentmeals.android.base.utilities;

import android.content.SharedPreferences;
import android.os.PatternMatcher;

import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

import javax.annotation.ParametersAreNonnullByDefault;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import static com.google.common.collect.Lists.newArrayList;
import static com.imminentmeals.android.base.BaseAndroidLibraryApplication.COOKIE_AUTH_TOKEN;
import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGE;

/**
 * A simple {@linkplain CookieStore cookie jar} that only cares to store the {@link #_COOKIE_AUTH_TOKEN}.
 * It persists the cookie through app launches and maintains it securely. Designed to allow null {@link URI}
 * in {@link #add(java.net.URI, java.net.HttpCookie)} to trigger one-time-use cookie setting, that restores
 * to the previous value after use. Utilized to support a connected account while also allowing other accounts
 * to make sync requests.
 * @author Dandré Allison
 */
@Singleton
@ParametersAreNonnullByDefault
public class AndroidCookieStore implements CookieStore {
    /** Name of the authentication token stored in a {@link SharedPreferences} */
    public static final String KEY_AUTH_TOKEN = "com.keepandshare.android.key.AndroidCookieStore.AUTH_TOKEN";

/* Constructor */
    @Inject
    public AndroidCookieStore(SharedPreferences settings, CryptographyUtilities crypto,
                              @Named(COOKIE_AUTH_TOKEN) String cookie_auth_token) {
        _settings = settings;
        _crypto = crypto;
        _COOKIE_AUTH_TOKEN = cookie_auth_token;
        _auth_token_pattern = new PatternMatcher(_COOKIE_AUTH_TOKEN, PatternMatcher.PATTERN_LITERAL);
    }

/* Cookie Store */
    @Override
    public void add(URI null_check, HttpCookie cookie) {
        // Stores the cookie in shared preferences
        if (_auth_token_pattern.match(cookie.getName())) {
            final SharedPreferences.Editor editor = _settings.edit();
            if (null_check == null)
                _previous_auth_token = _settings.getString(_COOKIE_AUTH_TOKEN, null);
            try {
                editor.putString(KEY_AUTH_TOKEN, _crypto.encrypt(cookie.getValue())).apply();
            } catch (Exception error) {
                AUTOTAGLOGE(error.getCause());
            }
        }
    }

    @Override
    public List<HttpCookie> getCookies() {
        return get(null);
    }

    @Override
    public List<HttpCookie> get(URI _) {
        // Loads in cookies from shared preferences
        final String auth_token = _settings.getString(KEY_AUTH_TOKEN, null);
        if (auth_token != null)
            try {
                final HttpCookie cookie = new HttpCookie(_COOKIE_AUTH_TOKEN, _crypto.decipher(auth_token));
                cookie.setPath("/");
                cookie.setVersion(0);

                // Reset to previous auth token if set
                if (_previous_auth_token != null) {
                    final SharedPreferences.Editor editor = _settings.edit();
                    try {
                        editor.putString(KEY_AUTH_TOKEN, _previous_auth_token).apply();
                    } catch (Exception error) {
                        AUTOTAGLOGE(error.getCause());
                    }
                    _previous_auth_token = null;
                }
                return newArrayList(cookie);
            } catch (Exception error) {
                AUTOTAGLOGE(error.getCause());
            }
        return newArrayList();
    }

    @Override
    public List<URI> getURIs() {
        return newArrayList();
    }

    @Override
    public boolean remove(URI _, HttpCookie __) {
        final boolean had_auth_token = _settings.getString(KEY_AUTH_TOKEN, null) != null;
        final SharedPreferences.Editor editor = _settings.edit();
        editor.remove(KEY_AUTH_TOKEN).apply();
        return had_auth_token;
    }

    @Override
    public boolean removeAll() {
        return remove(null,null);
    }

    private String _previous_auth_token;
    private final String _COOKIE_AUTH_TOKEN;
    private final PatternMatcher _auth_token_pattern;
    private final SharedPreferences _settings;
    private final CryptographyUtilities _crypto;
}
