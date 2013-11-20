/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentProvider;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;


/**
 * <p>Base {@link android.content.ContentProvider} implementation.</p>
 */
@SuppressWarnings("UnusedDeclaration")
@ParametersAreNonnullByDefault
public abstract class BaseContentProvider extends ContentProvider {
    /** Parameter indicating that the {@link android.content.ContentProvider} should notify observers that the content has been updated */
    public static final String PARAM_SHOULD_NOTIFY = "com.imminentmeals.android.base.param.BaseContentProvider.SHOULD_NOTIFY";
    public static final String PARAM_SHOULD_NOTIFY_SYNC_ADAPTER = "com.imminentmeals.android.base.param.BaseContentProvider.SHOULD_NOTIFY_SYN_ADAPTER";


    @Override
    public boolean onCreate() {
        final Context context = getContext();
        _database_helper = createOpenHelper(context);
        return true;
    }

    public BaseSqliteOpenHelper getOpenHelper() {
        return _database_helper;
    }

    @SuppressWarnings("UnusedParameters")
    @CheckForNull public <T extends ActiveRecord> List<T> selectRecords(Uri uri, QueryBuilder query,
                                                                        @Nullable String sort_order) {
        return null;
    }

    @SuppressWarnings("UnusedParameters")
    @CheckForNull public <T extends ActiveRecord> Iterable<T> queryRecords(Uri uri, QueryBuilder query,
                                                                           @Nullable String sort_order) {
        return null;
    }

    protected abstract ContentProviderActions createActions(int id);

    protected abstract BaseSqliteOpenHelper createOpenHelper(Context context);

    /**
     * Notifies a change (invokes {@link android.content.ContentResolver#notifyChange(android.net.Uri, android.database.ContentObserver)
     * if {@link #PARAM_SHOULD_NOTIFY} parameter is not present in the given Uri, or, if it
     * is present and set to a value other than the string {@code true}.
     * @param uri the URI through which to notify
     */
    protected void tryNotifyChange(Uri uri) {
        final String uri_requests_notification = uri.getQueryParameter(PARAM_SHOULD_NOTIFY);
        final String uri_requests_sync_adapter_notification = uri.getQueryParameter(PARAM_SHOULD_NOTIFY_SYNC_ADAPTER);
        final boolean should_notify = uri_requests_notification == null || Boolean.valueOf(uri_requests_notification);
        final boolean sync_to_network = uri_requests_sync_adapter_notification != null
                && Boolean.valueOf(uri_requests_sync_adapter_notification);

        if (should_notify && getContext() != null)
            getContext().getContentResolver().notifyChange(uri, null, sync_to_network);
    }

    /**
     * Sets the given {@link android.net.Uri URI} as the notification URI for the given {@link android.database.Cursor} and URI doesn't
     * indicate that the cursor should not be notified.
     * @param cursor the given cursor
     * @param uri the given URI
     */
    protected void trySetNotificationUri(@Nullable Cursor cursor, Uri uri) {
        if (cursor == null) return;


        final String uri_requests_notification = uri.getQueryParameter(PARAM_SHOULD_NOTIFY);
        final boolean should_notify = uri_requests_notification == null || Boolean.valueOf(uri_requests_notification);

        if (should_notify && getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
    }

    private BaseSqliteOpenHelper _database_helper;
}
