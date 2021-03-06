package com.imminentmeals.android.base.data.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.imminentmeals.android.base.utilities.ObjectGraph.ObjectGraphApplication;

import javax.inject.Inject;

import dagger.Lazy;

/**
 * <p>Service that provides sync functionality to the SyncManager through the {@link SyncAdapter}.</p>
 * @author Dandré Allison
 */
public class SyncService extends Service {
    /** Adapter that performs syncing feature */
    @Inject /* package */Lazy<SyncAdapter> sync_adapter;

    @Override
    public void onCreate() {
        super.onCreate();
        ((ObjectGraphApplication) getApplicationContext()).inject(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sync_adapter.get().getSyncAdapterBinder();
    }
}
