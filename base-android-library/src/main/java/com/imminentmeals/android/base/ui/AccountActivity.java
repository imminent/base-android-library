package com.imminentmeals.android.base.ui;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Fragment;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.common.base.Stopwatch;
import com.imminentmeals.android.base.R.id;
import com.imminentmeals.android.base.R.layout;
import com.imminentmeals.android.base.R.menu;
import com.imminentmeals.android.base.R.string;
import com.imminentmeals.android.base.utilities.AccountUtilities;
import com.imminentmeals.android.base.utilities.ObjectGraph;
import dagger.Lazy;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;

import static com.imminentmeals.android.base.utilities.AnalyticsUtilities.ACTION_BUTTON_PRESS;
import static com.imminentmeals.android.base.utilities.AnalyticsUtilities.CATEGORY_UX;
import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGV;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;

//import com.google.android.gms.R;

/**
 * <p>This wizard-like activity first presents an {@linkplain ChooseAccountFragment account selection fragment}, and
 * then an {@link AuthProgressFragment authentication progress fragment}.</p>
 *
 * @author Dandré Allison
 */
public class AccountActivity extends Activity implements AccountUtilities.AuthenticationCallbacks {
    @Inject /* package */Lazy<AccountUtilities> account_utilities;
    /** Name for the {@link Intent} to launch when the authentication finishes stored in the calling {@link Intent} */
    public static final String EXTRA_FINISH_INTENT = "com.imminentmeals.android.base.ui.extra.FINISH_INTENT";
    /** Request code indicating the result of the add account request */
    public static final int REQUEST_ADD_ACCOUNT = 200;

/* Lifecycle */
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(layout.activity_account);

        // Gets the continuation intent from the calling intent
        if (getIntent().hasExtra(EXTRA_FINISH_INTENT)) {
            _continuation_intent = getIntent().getParcelableExtra(EXTRA_FINISH_INTENT);
            if (_continuation_intent != null) {
                _continuation_intent.addCategory(Intent.CATEGORY_LAUNCHER);
                _continuation_intent.setAction(Intent.ACTION_MAIN);
                _continuation_intent.addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            }
        }

        AUTOTAGLOGV("Started connect account activity with continuation " + _continuation_intent);

        // Adds the Choose Account fragment to the screen, if this is a new creation of the activity
        if (icicle == null)
            getFragmentManager().beginTransaction()
                    .add(id.fragment_container, new ChooseAccountFragment(), _TAG_CHOOSE_ACCOUNT)
                    .commit();
    }

/* Activity Callbacks */
    @Override
    protected void onActivityResult(int request_code, int result_code, Intent data) {
        // Receives the response from getting the chosen account, if getting the account was successful,
        // then tries to authenticate the account, otherwise it goes back to the previous step
        if (request_code == _REQUEST_AUTHENTICATE) {
            if (result_code == RESULT_OK) tryAuthenticate();
            // goes back to previous step
            else _handler.post(new Runnable() {
              @Override public void run() {
                getFragmentManager().popBackStack();
              }
            });
        }
    }

/* AccountUtils.AuthenticationCallbacks */
    /**
     * <p>An {@link com.imminentmeals.android.base.utilities.AccountUtilities.AuthenticationCallbacks} to check the
     * status of the authentication process to determine if it has been cancelled.</p>
     * @return boolean status indicating whether the authentication process has been cancelled
     */
    @Override
    public boolean shouldCancelAuthentication() {
        return _authentication_was_cancelled;
    }

    /**
     * <p>An {@link com.imminentmeals.android.base.utilities.AccountUtilities.AuthenticationCallbacks} to notify the
     * {@link AccountActivity} that the authentication token for the {@linkplain #_chosen_account chosen account}
     * is available (and what that token is).</p>
     * @param auth_token the authentication token for the chosen account
     */
    @Override
    public void onAuthTokenAvailable(String auth_token) {
        // Launches the continuation Activity, when one was given
        if (_continuation_intent != null) startActivity(_continuation_intent);

        // Finishes the AccountActivity (and the authentication flow)
        finish();
    }

/* Helpers */
    /**
     * <p>Attempts to authenticate the {@linkplain #_chosen_account chosen account}.</p>
     */
    private void tryAuthenticate() {
        account_utilities.get().tryAuthenticate(this, this, _REQUEST_AUTHENTICATE, _chosen_account);
    }

    /**
     * <p>A fragment that presents the user with a list of connected accounts to choose from. When an account is
     * selected, the {@linkplain AuthProgressFragment authentication progress fragment} is displayed.</p>
     */
    public static class ChooseAccountFragment extends ListFragment {
        @Inject /* package */Lazy<AccountUtilities> account_utilities;

        /** Constructor */
        public ChooseAccountFragment() { }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            EasyTracker.getInstance().setContext(activity);
        }

        @Override
        public void onCreate(Bundle icicle) {
            super.onCreate(icicle);
            // Informs the action bar, that this fragment has actions to add
            setHasOptionsMenu(true);
            ObjectGraph.inject(this);
        }

        @Override
        public void onStart() {
            super.onStart();
            EasyTracker.getTracker().sendView("choose_account");
        }

        @Override
        public void onResume() {
            super.onResume();
            reloadAccountList();
            _timer = Stopwatch.createStarted();
        }

        @Override
        public void onCreateOptionsMenu(Menu actions, MenuInflater inflater) {
            inflater.inflate(menu.add_account, actions);
            super.onCreateOptionsMenu(actions, inflater);
        }

      @Override
      public void onPrepareOptionsMenu(Menu actions) {
        super.onPrepareOptionsMenu(actions);
        // Hides action bar action if empty, will display inline instead
        actions.findItem(id.action_add_account).setVisible(!_show_inline);
      }

        @Override
        public boolean onOptionsItemSelected(MenuItem action) {
            if (action.getItemId() == id.action_add_account) {
                // Tracks the add account event
                EasyTracker.getTracker().sendTiming(CATEGORY_UX, _timer.elapsed(TimeUnit.MILLISECONDS),
                                                    ACTION_BUTTON_PRESS, "add_account");
              assert getActivity() != null;
              account_utilities.get().startAddAccountActivity(this);
                return true;
            }
            return super.onOptionsItemSelected(action);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
            final ViewGroup root_view = (ViewGroup) inflater.inflate(
                    layout.fragment_choose_account, container, false);
            // Sets the description above the list of accounts, notice that it is parsed from HTML
            final TextView description = (TextView) root_view.findViewById(id.choose_account_intro);
            description.setText(Html.fromHtml(getString(string.description_choose_account)));
            // Sets add account button action
            final Button add_account = (Button) root_view.findViewById(android.R.id.empty);
            add_account.setOnClickListener(new View.OnClickListener() {

              public void onClick(View _) {
                assert getActivity() != null;
                account_utilities.get().startAddAccountActivity(ChooseAccountFragment.this);
              }
            });
            return root_view;
        }

        @Override public void onActivityResult(int request_code, int result_code, Intent _) {
          if (request_code == REQUEST_ADD_ACCOUNT && result_code == RESULT_OK && getActivity() != null)
              getActivity().finish();
        }

        @Override
        public void onListItemClick(ListView _, View __, int position, long ___) {
            // Confirms that the device is connected to an Internet network, and indicates when it isn't
            final AccountActivity activity = (AccountActivity) getActivity();
            assert activity != null;
            final NetworkInfo active_network = ((ConnectivityManager) activity
                    .getSystemService(CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
            if (active_network == null || !active_network.isConnected()) {
                Crouton.showText(activity, string.no_connection_cant_login, Style.INFO);
                return;
            }

            // Starts the authentication progress fragment
            activity._authentication_was_cancelled = false;
            activity._chosen_account = _account_list_adapter.getItem(position);
            activity.getFragmentManager().beginTransaction()
                    .replace(id.fragment_container, new AuthProgressFragment(), _TAG_LOADING)
                    .addToBackStack(_TAG_CHOOSE_ACCOUNT)
                    .commit();

            // Attempts to authenticate the selected account
            activity.tryAuthenticate();
        }

        /**
         * {@link android.widget.ListAdapter} that binds the account data to the view.
         */
        /* package */static class AccountListAdapter extends ArrayAdapter<Account> {

            /**
             * <p>Constructs the {@link android.widget.ListAdapter} that binds the account data to
             * the view.</p>
             * @param context The context from which to retrieve the {@link LayoutInflater}
             * @param accounts The list of accounts to bind
             */
            public AccountListAdapter(Context context, List<Account> accounts) {
                super(context, _LIST_ITEM_LAYOUT, accounts);
                _inflater = LayoutInflater.from(context);
            }

            @Override
            public View getView(int position, View reusable_view, ViewGroup parent) {
                final TextView text_view;
                final ViewHolder holder;
                // Inflates a new list item view when a reusable one isn't provided
                if (reusable_view == null) {
                    reusable_view = _inflater.inflate(_LIST_ITEM_LAYOUT, null);
                    holder = new ViewHolder(reusable_view);

                    reusable_view.setTag(holder);
                // Otherwise, retrieves the text sub view from reusable view
                } else
                    holder = (ViewHolder) reusable_view.getTag();

                text_view = holder.text_view;

                // Binds an account to a view representation
                final Account account = getItem(position);
                text_view.setText(account != null ? account.name : "");

                return reusable_view;
            }

            /**
             * <p>Utility to remember found {@link View}s.</p>
             * @author Dandre Allison
             */
            /* package */static class ViewHolder {
                /** The {@link TextView} into which to bind the account name */
                /* package */TextView text_view;

                /**
                 * <p>Finds the {@link View}s, and remembers where they are.</p>
                 * @param view
                 */
                public ViewHolder(View view) {
                    text_view = (TextView) view.findViewById(android.R.id.text1);
                }
            }

            /** The layout to use to inflate the list items */
            private static final int _LIST_ITEM_LAYOUT = android.R.layout.simple_list_item_1;
            /** The adapter's {@link LayoutInflater} */
            private final LayoutInflater _inflater;
        }

        /**
         * Resets the {@link #_account_list_adapter} and loads in the connected accounts from the
         * {@link android.accounts.AccountManager}.
         */
        private void reloadAccountList() {
            if (_account_list_adapter != null)
                _account_list_adapter = null;

            // Notice that AccountManager doesn't provide tools to determine what has changed from previous
            // account lists, so it is simpler to forget the previous list each time
            final AccountManager account_manager = AccountManager.get(getActivity());
            final Account[] accounts = account_manager.getAccountsByType(account_utilities.get().accountType());
            _account_list_adapter = new AccountListAdapter(getActivity(), Arrays.asList(accounts));
            setListAdapter(_account_list_adapter);
            _show_inline = accounts.length == 0;
            getActivity().invalidateOptionsMenu();
        }

        /** The {@link android.widget.ListAdapter}*/
        private AccountListAdapter _account_list_adapter;
        /** A stopwatch to measure elapsed time */
        private Stopwatch _timer;
        /** Indicates whether to show the add account action inline or in the action bar */
        private boolean _show_inline = true;
    }

    /**
     * <p>This fragment shows a login progress spinner. When it appears that the process is taking too long (in case
     * of a poor network connection), a retry button appears so the user can try again.</p>
     */
    public static class AuthProgressFragment extends Fragment {

        /** Constructor */
        public AuthProgressFragment() { }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            EasyTracker.getInstance().setContext(activity);
        }

        @Override
        public void onResume() {
            super.onResume();
            _timer = Stopwatch.createStarted();
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle icicle) {
            final ViewGroup root_view = (ViewGroup) inflater.inflate(layout.fragment_authentication_progress,
                    container, false);
            final View taking_a_while_panel = root_view.findViewById(id.taking_a_while_panel);
            final View try_again_button = root_view.findViewById(id.retry_button);
            // Returns to Choose Account screen if user wants to retry
            try_again_button.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View _) {
                        // Tracks user trying again
                        EasyTracker.getTracker().sendTiming(CATEGORY_UX, _timer.elapsed(TimeUnit.MILLISECONDS),
                                                            ACTION_BUTTON_PRESS, "try_login_again");
                        getFragmentManager().popBackStack();
                    }
                }
            );

            // Schedules the retry button to be displayed after the user has been on the progress screen for a
            // sufficient time to consider the process as taking too long to complete
            _handler.postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (!isAdded()) return;

                        taking_a_while_panel.setVisibility(View.VISIBLE);
                    }
                }, _DELAY_BEFORE_SHOULD_TRY_AGAIN);

            return root_view;
        }

        @Override
        public void onDetach() {
            super.onDetach();
            // If the screen is exited, then that is equivalent to canceling the authentication process
            ((AccountActivity) getActivity())._authentication_was_cancelled = true;
        }

        /** Delay before showing the retry button so the user can try again*/
        private static final long _DELAY_BEFORE_SHOULD_TRY_AGAIN = 7 * DateUtils.SECOND_IN_MILLIS;
        /** Main thread message handler, useful for scheduling an action to be run at a later time */
        private final Handler _handler = new Handler();
        /** A stopwatch to measure elapsed time */
        private Stopwatch _timer;
    }

    /** The chosen account */
    private Account _chosen_account;
    /** The {@link Intent} to launch when the authentication finishes. This will come from the
     * {@linkplain #getIntent()} in the {@linkplain #EXTRA_FINISH_INTENT finish intent extra} */
    /* package */Intent _continuation_intent;
    /** Indicates that the authentication process has been cancelled */
    private boolean _authentication_was_cancelled = false;
    /** The {@link Handler} that performs the actions on the main thread */
    private final Handler _handler = new Handler();
    /** Identifies the {@link ChooseAccountFragment} within the {@link #getFragmentManager() FragmentManager} */
    static final String _TAG_CHOOSE_ACCOUNT = "choose_account";
    /** Identifies the {@link AuthProgressFragment} within the {@link #getFragmentManager() FragmentManager} */
    static final String _TAG_LOADING = "loading";
    /** Tag to label {@link AccountActivity} log messages */
    @SuppressWarnings("unused")
    private static final String _TAG = makeLogTag("AccountActivity");
    /** Request code indicating the result of an authentication request */
    private static final int _REQUEST_AUTHENTICATE = 100;
    /** Indicates the the account is syncable */
    @SuppressWarnings("unused")
    private static final int _SYNCABLE = 1;
}
