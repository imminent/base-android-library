package com.imminentmeals.android.base.utilities;

import static com.imminentmeals.android.base.utilities.LogUtilities.LOGV;
import static com.imminentmeals.android.base.utilities.LogUtilities.makeLogTag;

import com.imminentmeals.android.base.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.widget.TextView;
import static android.text.Html.fromHtml;

/**
 * <p>A set of methods for showing contextual help information in the app. With strong inspiration from the Google IO
 * session app.</p>
 * @author Dandré Allison
 */
public final class HelpUtilities {

    /**
     * <p>Displays the about screen</p>
     * @author Dandre Allison
     */
    public static class AboutDialog extends DialogFragment {

        /**
         * <p>Constructs the {@link AboutDialog}.</p>
         */
        public AboutDialog() { }

/* DialogFragment Lifecycle */
        @Override
        public Dialog onCreateDialog(Bundle icicle) {
            // Get app version
            final PackageManager package_manager = getActivity().getPackageManager();
            final String package_name = getActivity().getPackageName();
            String version_name;
            try {
                final PackageInfo info = package_manager.getPackageInfo(package_name, 0);
                version_name = info.versionName;
            } catch (PackageManager.NameNotFoundException _) {
                version_name = _VERSION_UNAVAILABLE;
            }

            // Build the about body view
            final SpannableStringBuilder about_body = new SpannableStringBuilder();
            about_body.append(fromHtml(getString(R.string.about_body, version_name)));

            // Inflate dialog view
            final LayoutInflater layout_inflater = (LayoutInflater) getActivity().getSystemService(
                    Context.LAYOUT_INFLATER_SERVICE);
            final TextView about_body_view = (TextView) layout_inflater.inflate(R.layout.dialog_about, null);
            about_body_view.setText(about_body);
            // Enables touch-less navigation of the links in the text
            about_body_view.setMovementMethod(new LinkMovementMethod());

            return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.title_about)
                    .setView(about_body_view)
                    .setPositiveButton(R.string.button_ok,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int __) {
                                    dialog.dismiss();
                                }
                            })
                    .create();
        }

        private static final String _VERSION_UNAVAILABLE = "N/A";
    }

/* Helpers */
    /**
     * Displays the {@link AboutDialog}.
     * @param activity the activity from which the dialog was requested
     */
    public static void showAbout(Activity activity) {
        final FragmentManager fragment_manager = activity.getFragmentManager();
        final FragmentTransaction fragment_transaction = fragment_manager.beginTransaction();
        // Removes any potential previous instances of the About dialog
        final Fragment previous_about_fragment = fragment_manager.findFragmentByTag(_DIALOG_ABOUT);
        if (previous_about_fragment != null)
            fragment_transaction.remove(previous_about_fragment);
        fragment_transaction.addToBackStack(null);

        new AboutDialog().show(fragment_transaction, _DIALOG_ABOUT);
        LOGV(_TAG, "show About from %s", activity.getClass().getSimpleName());
    }

/* Private Constructor */
    /** Blocks instantiation of the {@link HelpUtilities} class. */
    private HelpUtilities() { }

    /** Tag to label {@link UiUtilities} log messages */
    private static final String _TAG = makeLogTag("HelpUtils");
    private static final String _DIALOG_ABOUT = "dialog_about";
}
