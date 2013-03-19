package com.imminentmeals.android.base.data.sync;

import javax.inject.Inject;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * <p>Service that provides sync functionality to the SyncManager through the {@link SyncAdapter}.</p>
 * @author Dandré Allison
 */
public class SyncService extends Service {
    /** Adapter that performs syncing feature */
    @Inject /* package */SyncAdapter _sync_adapter;

    @Override
    public IBinder onBind(Intent intent) {
        return _sync_adapter.getSyncAdapterBinder();
    }

    /** Key to acquire to access the blocks it locks */
    private static final Object _sync_adapter_lock = new Object();
}
