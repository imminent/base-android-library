package com.imminentmeals.android.base;
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

import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.CryptographyUtilities;
import com.imminentmeals.android.base.utilities.ObjectGraph;
import com.imminentmeals.android.base.utilities.StringUtilities;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import dagger.Lazy;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGE;
import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGV;


/**
 * <p>Provides Android framework with callbacks for handling authentication of Base Android Library accounts. This is used
 * by the Android {@link AccountManager} to provide a centralized location for managing accounts on the device.</p>
 * @author Dandré Allison
 */
public class AccountAuthenticatorService extends Service {
    /** Account authenticator */
    @Inject /* package */Lazy<AccountAuthenticator> account_authenticator;

    /**
     * <p>Authenticates fake accounts constructed by the Base Android Library app.</p>
     * @author Dandre Allison
     */
    @Singleton
    protected static class AccountAuthenticator extends AbstractAccountAuthenticator {

        /**
         * <p>Constructs the account authenticator</p>
         * @param context The context in which the account authenticator operates
         * @param account_utilities The account utilities
         * @param crypto The cryptography utilities
         * @param accounts The accounts manager provider
         */
        @Inject
        public AccountAuthenticator(Context context, AccountUtilities account_utilities, CryptographyUtilities crypto,
                                    Provider<AccountManager> accounts) {
            super(context);
            _account_utilities = account_utilities;
            _crypto = crypto;
            _accounts = accounts;
            _token_matcher = new PatternMatcher(_account_utilities.authTokenType(),
                    PatternMatcher.PATTERN_LITERAL);
        }

        /*
         * The user has requested to add a new account to the system. We return an intent that will launch our login
         * screen if the user has not logged in yet, otherwise our activity will just pass the user's credentials on to
         * the account manager.
         */
        @Override
        public Bundle addAccount(AccountAuthenticatorResponse response, String account_type, String auth_token_type,
                                 String[] required_features, Bundle options) throws NetworkErrorException {
            // If no connect account action is defined, then there is no connect account activity to start
            if (_account_utilities.addAccountActionName() == null) return Bundle.EMPTY;

            AUTOTAGLOGV("Creating add account activity bundle");
            // Creates the intent to start the connect account activity
            return addAccountBundle(response, account_type, auth_token_type, true);
        }

        @Override
        public Bundle confirmCredentials(AccountAuthenticatorResponse response, Account account, Bundle options) {
            final String auth_token = authenticate(account, _accounts.get());
            final boolean confirmed = StringUtilities.isEmpty(auth_token);
            final Bundle result = new Bundle();
            result.putBoolean(AccountManager.KEY_BOOLEAN_RESULT, confirmed);
            if (!confirmed) {
                result.putInt(AccountManager.KEY_ERROR_CODE, AccountManager.ERROR_CODE_BAD_ARGUMENTS);
                result.putString(AccountManager.KEY_ERROR_MESSAGE, "Unable to confirm given credentials");
            }
            return result;
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

            final AccountManager accounts = _accounts.get();
            String auth_token = accounts.peekAuthToken(account, auth_token_type);

            // Attempts to use stored account credentials to authenticate account with the server
            if (StringUtilities.isEmpty(auth_token))
                auth_token = authenticate(account, accounts);

            // Returns the auth_token, since the account already has access to the server
            if (!StringUtilities.isEmpty(auth_token)) {
                final Bundle result = new Bundle(3);
                result.putString(AccountManager.KEY_ACCOUNT_NAME, account.name);
                result.putString(AccountManager.KEY_ACCOUNT_TYPE, account.type);
                result.putString(AccountManager.KEY_AUTHTOKEN, auth_token);
                return result;
            }

            // Starts the connect account activity, since authentication token couldn't be automatically retrieved
            return addAccountBundle(response, account.type, auth_token_type, false);
        }

        @Override
        public String getAuthTokenLabel(String auth_token_type) {
            return _token_matcher.match(auth_token_type) ? auth_token_type : null;
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

        /**
         * <p>Creates the {@link Bundle} used when creating the connect account {@link android.app.Activity} when the user must be prompted
         * for account credentials.</p>
         * @param response The response returned to the account authenticator
         * @param account_type The account type
         * @param auth_token_type The authentication token type
         * @return The created bundle
         */
        private Bundle addAccountBundle(AccountAuthenticatorResponse response, String account_type, String auth_token_type,
                                            boolean is_adding_new_account) {
            final Intent intent = new Intent(_account_utilities.addAccountActionName());
            intent.putExtra(AccountManager.KEY_ACCOUNT_TYPE, account_type);
            intent.putExtra(AccountUtilities.KEY_AUTH_TOKEN_TYPE, auth_token_type);
            intent.putExtra(AccountUtilities.KEY_IS_ADDING_NEW_ACCOUNT, is_adding_new_account);
            intent.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response);
            final Bundle bundle = new Bundle(1);
            bundle.putParcelable(AccountManager.KEY_INTENT, intent);
            return bundle;
        }

        /**
         * <p>Authenticates the given account with the server.</p>
         * @param account The given account
         * @param accounts The accounts manager
         * @return The authentication token retrieved from the server
         */
        private String authenticate(Account account, final AccountManager accounts) {
            final String password = accounts.getPassword(account);
            if (password != null)
                try {
                    return _account_utilities.login(account.name, _crypto.decipher(password));
                } catch (InvalidKeyException exception) {
                    AUTOTAGLOGE(exception);
                } catch (IllegalBlockSizeException exception) {
                    AUTOTAGLOGE(exception);
                } catch (BadPaddingException exception) {
                    AUTOTAGLOGE(exception);
                } catch (NoSuchAlgorithmException exception) {
                    AUTOTAGLOGE(exception);
                } catch (NoSuchPaddingException exception) {
                    AUTOTAGLOGE(exception);
                } catch (UnsupportedEncodingException exception) {
                    AUTOTAGLOGE(exception);
                }
            return null;
        }

        /** Account utilities */
        private final AccountUtilities _account_utilities;
        /** Cryptography utilities */
        private final CryptographyUtilities _crypto;
        /** The {@link AccountManager} provider */
        private final Provider<AccountManager> _accounts;
        /** Matches against {@link com.imminentmeals.android.base.utilities.AccountUtilities#authTokenType()} */
        private final PatternMatcher _token_matcher;
    }

    @Override
    public void onCreate() {
        AUTOTAGLOGV("Authentication service started");
        ObjectGraph.inject(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return account_authenticator.get().getIBinder();
    }
}
