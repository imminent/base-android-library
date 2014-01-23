package com.imminentmeals.android.base.utilities;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Patterns;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.imminentmeals.android.base.data.provider.BaseContract;
import com.imminentmeals.android.base.ui.AccountActivity;
import java.util.List;
import java.util.regex.Matcher;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.inject.Inject;

import static android.content.Context.TELEPHONY_SERVICE;
import static com.google.common.collect.Lists.newArrayList;
import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGV;
import static com.imminentmeals.android.base.utilities.LogUtilities.LOGE;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;

/**
 * <p>A collection of authentication and account connection utilities. With strong inspiration from the Google IO session
 * app.</p>
 * @author Dandré Allison
 */
@SuppressWarnings("UnusedDeclaration")
public class AccountUtilities {
    /** Name of the owner name stored in a {@link Bundle} */
    public static final String KEY_OWNER_NAME = "com.imminentmeals.android.base.utilities.key.AccountUtlities.OWNER_NAME";
    /** Name of the account ID stored in a {@link Bundle} */
    public static final String KEY_ACCOUNT_ID = "com.imminentmeals.android.base.utilities.key.AccountUtilities.ACCOUNT_ID";
    /** Name of the authentication token type stored in a {@link Bundle} */
    public static final String KEY_AUTH_TOKEN_TYPE = "com.imminentmeals.android.base.utilities.key.AccountUtilities." +
    		"AUTH_TOKEN_TYPE";
    /** Name of the flag indicating when connecting a new account occurs, stored in a {@link Bundle} */
    public static final String KEY_IS_ADDING_NEW_ACCOUNT = "com.imminentmeals.android.base.utilities.key.AccountUtilities." +
    		"IS_ADDING_NEW_ACCOUNT";

    /** Injectable constructor */
    @Inject
    public AccountUtilities() { }

    /**
     * <p>Callback interface for account authentication process.</p>
     */
    public interface AuthenticationCallbacks {
        /**
         * <p>Determines if the authentication process should be cancelled.</p>
         * @return {@code true} indicates that the authentication process should be cancelled
         */
        boolean shouldCancelAuthentication();

        /**
         * <p>Callback when the the authentication token is available.</p>
         * @param auth_token the acquired authentication token
         */
        void onAuthTokenAvailable(String auth_token);
    }

    /**
     * <p>Interface for interacting with the result of {@link AccountUtilities#getUserProfile}.</p>
     */
    @SuppressWarnings("UnusedDeclaration")
    public static class UserProfile {

        /**
         * Adds an email address to the list of possible email addresses for the user
         * @param email the possible email address
         */
        public void addPossibleEmail(String email) {
            addPossibleEmail(email, false);
        }

        /**
         * <p>Adds an email address to the list of possible email addresses for the user. Retains information about
         * whether this email address is the primary email address of the user.</p>
         * @param email the possible email address
         * @param is_primary whether the email address is the primary email address
         */
        public void addPossibleEmail(String email, boolean is_primary) {
            if (email == null) return;
            if (is_primary) {
                _primary_email = email;
                _possible_emails.add(email);
            } else
                _possible_emails.add(email);
        }

        /**
         * <p>Adds a name to the list of possible names for the user.</p>
         * @param name the possible name
         */
        public void addPossibleName(String name) {
            if (name != null) _possible_names.add(name);
        }

        /**
         * <p>Adds a given name and family name to the lists of possible given and family names for
         * the user.</p>
         * @param given_name the possible given name
         * @param family_name the possible family name
         */
        public void addPossibleName(String given_name, String family_name) {
            if (given_name != null && family_name != null) {
                _possible_given_names.add(given_name);
                _possible_family_names.add(family_name);
                _possible_names.add(given_name + " " + family_name);
            }
        }

        /**
         * <p>Adds a phone number to the list of possible phone numbers for the user.</p>
         * @param phone_number the possible phone number
         */
        public void addPossiblePhoneNumber(String phone_number) {
            if (phone_number != null) _possible_phone_numbers.add(phone_number);
        }

        /**
         * <p>Adds a phone number to the list of possible phone numbers for the user.  Retains information about
         * whether this phone number is the primary phone number of the user.</p>
         * @param phone_number the possible phone number
         * @param is_primary whether the phone number is the primary phone number
         */
        public void addPossiblePhoneNumber(String phone_number, boolean is_primary) {
            if (phone_number == null) return;
            if (is_primary) {
                _primary_phone_number = phone_number;
                _possible_phone_numbers.add(phone_number);
            } else
                _possible_phone_numbers.add(phone_number);
        }

        /**
         * <p>Sets the possible photo for the user.</p>
         * @param photo the possible photo
         */
        public void addPossiblePhoto(Uri photo) {
            if (photo != null) _possible_photo = photo;
        }

        /**
         * <p>Retrieves the list of possible email addresses.</p>
         * @return the list of possible email addresses
         */
        public List<String> possibleEmails() {
            return _possible_emails;
        }

        /**
         * <p>Retrieves the list of possible names.</p>
         * @return the list of possible names
         */
        public List<String> possibleNames() {
            return _possible_names;
        }

        /**
         * <p>Retrieves the list of possible given names.</p>
         * @return the list of possible given names
         */
        public List<String> possibleGivenNames() {
            return _possible_given_names;
        }

        /**
         * <p>Retrieves the list of possible family names.</p>
         * @return the list of possible family names
         */
        public List<String> possibleFamilyNames() {
            return _possible_family_names;
        }

        /**
         * <p>Retrieves the list of possible phone numbers.</p>
         * @return the list of possible phone numbers
         */
        public List<String> possiblePhoneNumbers() {
            return _possible_phone_numbers;
        }

        /**
         * <p>Retrieves the possible photo.</p>
         * @return the possible photo
         */
        public Uri possiblePhoto() {
            return _possible_photo;
        }

        /**
         * <p>Retrieves the primary email address.</p>
         * @return the primary email address
         */
        public String primaryEmail() {
            return _primary_email;
        }

        /**
         * <p>Retrieves the primary name</p>
         * @return the primary name
         */
        public String primaryName() {
            return _primary_name;
        }

        /**
         * <p>Retrieves the primary phone number.</p>
         * @return the primary phone number
         */
        public String primaryPhoneNumber() {
            return _primary_phone_number;
        }

        /** The primary email address */
        private String _primary_email;
        /** The primary name */
        private String _primary_name;
        /** The primary phone number */
        private String _primary_phone_number;
        /** A list of possible email addresses for the user */
        private List<String> _possible_emails = newArrayList();
        /** A list of possible names for the user */
        private List<String> _possible_names = newArrayList();
        /** A list of possible first names for the user */
        private List<String> _possible_given_names = newArrayList();
        /** A list of possible last names for the user */
        private List<String> _possible_family_names = newArrayList();
        /** A list of possible phone numbers for the user */
        private List<String> _possible_phone_numbers = newArrayList();
        /** A possible photo for the user */
        private Uri _possible_photo;
    }

    /**
     * <p>Attempts to get the auth token for the given account, using
     * {@linkplain android.accounts.AccountManager#getAuthToken(android.accounts.Account, String, boolean,
     * android.accounts.AccountManagerCallback, android.os.Handler)}.</p>
     * @param activity the context method was called from
     * @param callback the authentication callback
     * @param activity_request_code used to set the request code of a subsequently started {@link android.app.Activity}
     * @param account the account that is trying to authenticate
     */
    public void tryAuthenticate(Activity activity, AuthenticationCallbacks callback, int activity_request_code,
                                       Account account) {
        AccountManager.get(activity).getAuthToken(
                account,
                authTokenType(),
                Bundle.EMPTY,
                false,
                getAccountManagerCallback(callback, account, activity, activity, activity_request_code),
                null);
    }

    /**
     * <p>Attempts to get the auth token for the given account, using
     * {@linkplain android.accounts.AccountManager#getAuthToken(android.accounts.Account, String, boolean,
     * android.accounts.AccountManagerCallback, android.os.Handler)}. Useful when required from a location that
     * isn't part of an {@link Activity}.</p>
     * @param context the context method was called from
     * @param callback the authentication callback
     * @param account the account that is trying to authenticate
     */
    public void tryAuthenticateWithErrorNotification(Context context, AuthenticationCallbacks callback,
                                                            Account account) {
        AccountManager.get(context).getAuthToken(
                account,
                authTokenType(),
                Bundle.EMPTY,
                true,
                getAccountManagerCallback(callback, account, context, null, 0),
                null);
    }

    /**
     * <p>Confirms if there is currently an authenticated account.</p>
     * @param context the context to check for an authenticated account
     * @return the result of looking for an authenticated account
     */
    public static boolean isAuthenticated(Context context) {
        return !TextUtils.isEmpty(getChosenAccount(context));
    }

    /**
     * <p>Retrieves the stored chosen account name, if one exists.</p>
     * @param context the context from which to retrieve the stored account name
     * @return the stored chosen account name
     */
    public static String getChosenAccount(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(_PREF_CHOSEN_ACCOUNT, null);
    }

    /**
     * <p>Retrieves the stored auth token for the chosen account, if one exists.</p>
     * @param context the context from which to retrieve the stored auth token
     * @return the stored auth token
     */
    public static String getAuthToken(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(_PREF_AUTH_TOKEN, null);
    }

    /**
     * <p>Retrieves the stored name for the chosen account's owner, if one exists.</p>
     * @param context the context from which to retrieve the stored name
     * @return the stored name
     */
    public static String getChosenAccountOwnerName(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(_PREF_CHOSEN_ACCOUNT_NAME, null);
    }

    /**
     * <p>Retrieves the stored account ID for the chosen account, if one exists.</p>
     * @param context the context from which to retrieve the stored account ID
     * @return the stored account ID
     */
    public static String getChosenAccountId(Context context) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        return settings.getString(_PREF_CHOSEN_ACCOUNT_ID, null);
    }

    /**
     * <p>Starts the add account connection activity when the add account action is selected.</p>
     * <p>Note: this is the activity in {@link com.imminentmeals.android.base.AccountAuthenticatorService}'s
     * {@link android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts.AccountAuthenticatorResponse, String, String, String[], Bundle)}
     * method, as it will be registered for this intent</p>
     * @param context The context from which to start the account connection activity
     */
    public final void startAddAccountActivity(Activity context) {
        // Starts the add account connection activity when the add account action is selected
        // Note: this is the activity in AccountAuthenticatorService addAccount method, as it gets registered
        //       for this intent
        AUTOTAGLOGV("Starting add account activity for authority: " + authority());
        final Intent add_account_intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        add_account_intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{ authority() });
        context.startActivityForResult(add_account_intent, AccountActivity.REQUEST_ADD_ACCOUNT);
    }

    /**
     * <p>Starts the add account connection activity when the add account action is selected.</p>
     * <p>Note: this is the activity in {@link com.imminentmeals.android.base.AccountAuthenticatorService}'s
     * {@link android.accounts.AbstractAccountAuthenticator#addAccount(android.accounts.AccountAuthenticatorResponse, String, String, String[], Bundle)}
     * method, as it will be registered for this intent</p>
     * @param fragment The fragment from which to start the account connection activity
     */
    public final void startAddAccountActivity(Fragment fragment) {
        // Starts the add account connection activity when the add account action is selected
        // Note: this is the activity in AccountAuthenticatorService addAccount method, as it gets registered
        //       for this intent
        AUTOTAGLOGV("Starting add account activity for authority: " + authority());
        final Intent add_account_intent = new Intent(Settings.ACTION_ADD_ACCOUNT);
        add_account_intent.putExtra(Settings.EXTRA_AUTHORITIES, new String[]{ authority() });
        fragment.startActivityForResult(add_account_intent, AccountActivity.REQUEST_ADD_ACCOUNT);
    }

    /**
     * <p>Invalidates the chosen account within the {@linkplain android.accounts.AccountManager} and removes its
     * stored auth token from the user settings.</p>
     * @param context the context from which to retrieve the account manger
     */
    public final void invalidateAuthToken(Context context) {
        final AccountManager account_manager = AccountManager.get(context);
        account_manager.invalidateAuthToken(accountType(), getAuthToken(context));
        setAuthToken(context, null);
    }

    /**
     * <p>Retrieves the user profile information.</p>
     * @param context the context from which to retrieve the user profile
     * @return the user profile
     */
    public static UserProfile getUserProfile(Context context) {
        return GateKeeper.hasIcs()
                ? getUserProfileOnIcsDevice(context)
                : getUserProfileOnGingerbreadDevice(context);
    }

/* Methods to override */
    /**
     * <p>Defines the launching {@link Intent} action to launch the connect account {@link Activity}.</p>
     * @return The name of the action, {@code null} indicates that there is no connect account activity
     */
    @Nullable public String connectAccountActionName() {
        return null;
    }

    /**
     * <p>Defines the launching {@link Intent} action to launch the add account {@link Activity}.</p>
     * @return The name of the action, {@code null} indicates that there is no add account activity
     */
    @Nullable public String addAccountActionName() {
        return null;
    }

    @Nullable public String login(String account_name, String password) {
        return null;
    }

    /**
     * <p>Invalidates the chosen account and removes it from the stored user settings.</p>
     * @param context the context to disconnect the account from
     */
    public void logout(Context context) {
        invalidateAuthToken(context);
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().clear().apply();
    }

    /**
     * <p>The account type for the app.</p>
     * @return The account type
     */
    @Nonnull public String accountType() {
        return GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE;
    }

    /**
     * <p>The auth token type for the app.</p>
     * @return The auth token type
     */
    @Nonnull public String authTokenType() {
        return "https://api.example.com";
    }

    @Nonnull protected String authority() {
        return BaseContract.CONTENT_AUTHORITY;
    }

    /**
     * <p>Allows subclass to retrieve user data.</p>
     * @param context the current context
     * @param user_data the user data
     */
    protected void didRetrieveUserData(Context context, Bundle user_data) { }

    /**
     * <p>Contacts user profile query interface.</p>
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private interface ProfileQuery {
        /** The set of columns to extract from the profile query results */
        @SuppressLint("InlinedApi")
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
                ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME,
                ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.IS_PRIMARY,
                ContactsContract.CommonDataKinds.Photo.PHOTO_URI,
                ContactsContract.Contacts.Data.MIMETYPE
        };

        /** Column index for the email address in the profile query results */
        int EMAIL = 0;
        /** Column index for the primary email address indicator in the profile query results */
        int IS_PRIMARY_EMAIL = 1;
        /** Column index for the family name in the profile query results */
        int FAMILY_NAME = 2;
        /** Column index for the given name in the profile query results */
        int GIVEN_NAME = 3;
        /** Column index for the phone number in the profile query results */
        int PHONE_NUMBER = 4;
        /** Column index for the primary phone number in the profile query results */
        int IS_PRIMARY_PHONE_NUMBER = 5;
        /** Column index for the photo in the profile query results */
        int PHOTO = 6;
        /** Column index for the MIME type in the profile query results */
        int MIME_TYPE = 7;
    }

    /**
     * <p>Constructs an {@link android.accounts.AccountManagerCallback} that can handle cancellation through
     * {@link AuthenticationCallbacks#shouldCancelAuthentication()}.
     * The call back will start the {@link android.content.Intent} returned to it or establish the authenticated
     * account and trigger
     * {@link AuthenticationCallbacks#onAuthTokenAvailable(String)}
     * on the given {@link AuthenticationCallbacks}.</p>
     * @param callback the given AuthenticationCallbacks
     * @param account the account of interest
     * @param context the current context
     * @param activity the current activity
     * @param activity_request_code the current activity's request code used when starting the intent
     * @return the constructed AccountManagerCallback
     */
    private AccountManagerCallback<Bundle> getAccountManagerCallback(final AuthenticationCallbacks callback,
            final Account account, final Context context, final Activity activity, final int activity_request_code) {
        return new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                // End if authentication is cancelled, or the callback doesn't exist
                if (callback != null && callback.shouldCancelAuthentication()) return;

                try {
                    final Bundle bundle = future.getResult();
                    // Starts the given intent in the same task if one is given in the bundle
                    if (activity != null && bundle.containsKey(AccountManager.KEY_INTENT)) {
                        final Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
                        intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);
                        activity.startActivityForResult(intent, activity_request_code);
                    // Store the token, account name, account ID, and owner name if a token is given in the bundle
                    } else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
                        final String token = bundle.getString(AccountManager.KEY_AUTHTOKEN);
                        setAuthToken(context, token);
                        setChosenAccount(context, account.name);
                        final Bundle user_data = bundle.getBundle(AccountManager.KEY_USERDATA);
                        if (user_data != null) {
                            setChosenAccountId(context, user_data.getString(KEY_ACCOUNT_ID));
                            setChosenAccountOwnerName(context, user_data.getString(KEY_OWNER_NAME));
                            didRetrieveUserData(context, user_data);
                        }
                        // Notify the callback that the token is ready
                        if (callback != null)
                            callback.onAuthTokenAvailable(token);
                    }
                } catch (Exception error) {
                    LOGE(_TAG, error, "Authentication error");
                }
            }
        };
    }

    /**
     * <p>Stores the given auth token in the {@linkplain android.content.SharedPreferences user settings}.</p>
     * @param context the context to retrieve the user settings from
     * @param auth_token the given auth token
     */
    private static void setAuthToken(Context context, String auth_token) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(_PREF_AUTH_TOKEN, auth_token).apply();
    }

    /**
     * <p>Stores the given account name in the {@linkplain android.content.SharedPreferences user settings}.</p>
     * @param context the context to retrieve the user settings from
     * @param account_name the given auth token
     */
    private static void setChosenAccount(Context context, String account_name) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(_PREF_CHOSEN_ACCOUNT, account_name).apply();
    }

    /**
     * <p>Stores the given name in the {@linkplain android.content.SharedPreferences user settings}.</p>
     * @param context the context to retrieve the user settings from
     * @param name the given name
     */
    private static void setChosenAccountOwnerName(Context context, String name) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(_PREF_CHOSEN_ACCOUNT_NAME, name).apply();
    }

    /**
     * <p>Stores the given account ID in the {@linkplain android.content.SharedPreferences user settings}.</p>
     * @param context the context to retrieve the user settings from
     * @param account_id the given account ID
     */
    private static void setChosenAccountId(Context context, String account_id) {
        final SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(context);
        settings.edit().putString(_PREF_CHOSEN_ACCOUNT_ID, account_id).apply();
    }

    /**
     * <p>Retrieves the user profile information in a manner supported by Gingerbread devices.</p>
     * @param context the context from which to retrieve the user's email address and name
     * @return a list of the possible user's email address and name
     */
    private static UserProfile getUserProfileOnGingerbreadDevice(Context context) {
        final Account[] accounts = AccountManager.get(context).getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        final UserProfile user_profile = new UserProfile();
        // As far as I can tell, there is no way to get the real name or phone number from the Google account
        for (Account account : accounts) {
            if (VALID_EMAIL_ADDRESS.reset(account.name).matches())
                user_profile.addPossibleEmail(account.name);
        }
        // Gets the phone number of the device is the device has one
        if (context.getPackageManager().hasSystemFeature(TELEPHONY_SERVICE)) {
            final TelephonyManager telephony = (TelephonyManager) context.getSystemService(TELEPHONY_SERVICE);
            user_profile.addPossiblePhoneNumber(telephony.getLine1Number());
        }

        return user_profile;
    }

    /**
     * <p>Retrieves the user profile information in a manner supported by Ice Cream Sandwich devices.</p>
     * @param context the context from which to retrieve the user's email address and name
     * @return  a list of the possible user's email address and name
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static UserProfile getUserProfileOnIcsDevice(Context context) {
        final Cursor cursor = context.getContentResolver().query(
                // Retrieves data rows for the device user's 'profile' contact
                Uri.withAppendedPath(
                        ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
                ProfileQuery.PROJECTION,

                // Selects only email addresses or names
                ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                        + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                        + ContactsContract.Contacts.Data.MIMETYPE + "=? OR "
                        + ContactsContract.Contacts.Data.MIMETYPE + "=?",
                new String[]{
                        ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
                        ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
                },

                // Show primary rows first. Note that there won't be a primary email address if the
                // user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC"
        );

        final UserProfile user_profile = new UserProfile();
        String mime_type;
        while (cursor.moveToNext()) {
            mime_type = cursor.getString(ProfileQuery.MIME_TYPE);
            if (mime_type.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE))
                user_profile.addPossibleEmail(cursor.getString(ProfileQuery.EMAIL),
                        cursor.getInt(ProfileQuery.IS_PRIMARY_EMAIL) > 0);
            else if (mime_type.equals(ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE))
                user_profile.addPossibleName(cursor.getString(ProfileQuery.GIVEN_NAME),
                                             cursor.getString(ProfileQuery.FAMILY_NAME));
            else if (mime_type.equals(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE))
                user_profile.addPossiblePhoneNumber(cursor.getString(ProfileQuery.PHONE_NUMBER),
                        cursor.getInt(ProfileQuery.IS_PRIMARY_PHONE_NUMBER) > 0);
            else if (mime_type.equals(ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE))
                user_profile.addPossiblePhoto(Uri.parse(cursor.getString(ProfileQuery.PHOTO)));
        }

        cursor.close();

        return user_profile;
    }

    private static final String _PREF = "com.imminentmeals.android.base.utilities.pref";
    /** Name for the chosen account stored in the {@linkplain android.content.SharedPreferences user settings} */
    private static final String _PREF_CHOSEN_ACCOUNT = _PREF + ".AccountUtilities.CHOSEN_ACCOUNT";
    /** Name for the auth token stored in the {@linkplain android.content.SharedPreferences user settings} */
    private static final String _PREF_AUTH_TOKEN = _PREF + "AccountUtilities.AUTH_TOKEN";
    /** Name for the chosen account's ID stored in the {@linkplain android.content.SharedPreferences user settings} */
    private static final String _PREF_CHOSEN_ACCOUNT_ID = _PREF + "AccountUtilities.CHOSEN_ACCOUNT.ID";
    /** Name for the chosen account owner's name stored in the {@linkplain android.content.SharedPreferences user settings} */
    private static final String _PREF_CHOSEN_ACCOUNT_NAME = _PREF + "AccountUtilities.CHOSEN_ACCOUNT.NAME";
    /** Matches valid email address */
    private static final Matcher VALID_EMAIL_ADDRESS = Patterns.EMAIL_ADDRESS.matcher("");
    /** Tag to label {@link AccountUtilities} log messages */
    private static final String _TAG = makeLogTag(AccountUtilities.class);
}
