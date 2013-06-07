package com.imminentmeals.android.base.data.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;

import com.imminentmeals.android.base.BuildConfig;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGD;
import static com.imminentmeals.android.base.utilities.LogUtilities.AUTOTAGLOGE;

/**
 * <p>Sync adapter.</p>
 * @author Dandre Allison
 */
@Singleton
public class SyncAdapter extends AbstractThreadedSyncAdapter {

    /**
     * <p>Constructs a {@link SyncAdapter}.</p>
     * @param context The context in which to create the sync adapter
     */
    @Inject
    public SyncAdapter(Context context) {
        this(context, true);
    }

    /**
     * <p>Constructs a {@link SyncAdapter}.</p>
     * @param context The context in which to create the sync adapter
     * @param should_auto_initialize Indicates whether the sync adapter should auto initialize accounts
     */
    public SyncAdapter(Context context, boolean should_auto_initialize) {
        super(context, should_auto_initialize);

        if (!BuildConfig.DEBUG) {
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(Thread _, Throwable throwable) {
                    AUTOTAGLOGE(throwable, "Uncaught sync exception, suppressing UI in release build.");
                }
            });
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider,
            SyncResult sync_result) {
        // TODO Auto-generated method stub
        AUTOTAGLOGD("perform sync");
    }
}
