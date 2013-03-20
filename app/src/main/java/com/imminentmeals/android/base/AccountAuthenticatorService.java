package com.imminentmeals.android.base;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.ObjectGraph;

import dagger.Lazy;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.accounts.NetworkErrorException;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PatternMatcher;


/**
 * <p>Provides Android framework with callbacks for handling authentication of Base Android Library accounts. This is used
 * by the Android {@link AccountManager} to provide a centralized location for managing accounts on the device.</p>
 * @author Dandré Allison
 */
public class AccountAuthenticatorService extends Service {
    /** Account authenticator */
    @Inject /* package */Lazy<AbstractAccountAuthenticator> account_authenticator;

    /**
     * <p>Authenticates fake accounts constructed by the Base Android Library app.</p>
     * @author Dandre Allison
     */
    @Singleton
    protected static class FakeAccountAuthenticator extends AbstractAccountAuthenticator {

        /**
         * <p>Constructs the account authenticator</p>
         * @param context
         */
        @Inject
        public FakeAccountAuthenticator(Context context) {
            super(context);
            _context = context;
        }

        /*
         * The user has requested to add a new account to the system. We return an intent that will launch our login
         * screen if the user has not logged in yet, otherwise our activity will just pass the user's credentials on to
         * the account manager.
         */
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String account_type, String auth_token_type,
                                 String[] required_features, Bundle options) throws NetworkErrorException {
            return Bundle.EMPTY;
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            return null;
        }

        @Override
        public Bundle editProperties(AccountAuthenticatorResponse response, String account_type) {
            return null;
        }

        /*
         * Note that this method is triggered by AccountManager.getAuthToken()
         */
        @Override
        public Bundle getAuthToken(AccountAuthenticatorResponse response, Account account, String auth_token_type,
                                   Bundle options) throws NetworkErrorException {
            // Returns an error if an auth_token type we don't support is requested
            if (!_token_matcher.match(auth_token_type)) {
                final Bundle result = new Bundle(1);
                result.putString(AccountManager.KEY_ERROR_MESSAGE, "invalid auth token type");
                return result;
            }

            // Returns the stored auth token
            final Bundle result = new Bundle(3);
            result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
            result.putString(AccountManager.KEY_ACCOUNT_TYPE, "fake:" + AccountUtilities.AUTH_TOKEN_TYPE);
            result.putString(AccountManager.KEY_AUTHTOKEN, AccountUtilities.getAuthToken(_context));
            return result;
        }

        @Override
        public String getAuthTokenLabel(String auth_token_type) {
            return _token_matcher.match(AccountUtilities.AUTH_TOKEN_TYPE) ? auth_token_type : null;
        }

        @Override
        public Bundle hasFeatures(AccountAuthenticatorResponse response, Account account, String[] features)
                throws NetworkErrorException {
            final Bundle reply = new Bundle();
            reply.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, false);
            return reply;
        }

        @Override
        public Bundle updateCredentials(AccountAuthenticatorResponse response, Account account, String auth_token_type,
                                        Bundle options) {
            return null;
        }

        /** The {@link Context} in which the {@link FakeAccountAuthenticator} operates */
        private Context _context;
        /** Matches against {@link AccountUtilities#AUTH_TOKEN_TYPE} */
        private static final PatternMatcher _token_matcher = new PatternMatcher(AccountUtilities.AUTH_TOKEN_TYPE,
                                                                                PatternMatcher.PATTERN_LITERAL);
    }

    @Override
    public void onCreate() {
        LOGV("Authentication service started");
        ObjectGraph.inject(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return account_authenticator.get().getIBinder();
    }
}
