/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import static com.google.common.collect.Maps.newHashMap;


/**
 * <p>Base database entry builder</p>
 */
@ParametersAreNonnullByDefault
public abstract class ValuesBuilder {

    /**
     * Constructs a {@link com.imminentmeals.android.base.utilities.database.ValuesBuilder} for the given {@link android.net.Uri content URI}.
     * @param context the context from which to retrieve the {@link android.content.ContentResolver}
     * @param content_uri The content URI on which the builder operates
     */
    protected ValuesBuilder(Context context, Uri content_uri) {
        _content = context.getContentResolver();
        _content_uri = content_uri;
        _query_parameters = newHashMap();
        _values = new ContentValues();
        _content_value_pool = null;
    }

    /**
     * Constructs a {@link com.imminentmeals.android.base.utilities.database.ValuesBuilder} for the given {@link android.net.Uri content URI}.
     * @param context the context from which to retrieve the {@link android.content.ContentResolver}
     * @param content_uri The content URI on which the builder operates
     * @param content_value_pool The pool from which to retrieve ContentValue objects
     */
    protected ValuesBuilder(Context context, Uri content_uri, ObjectPool<ContentValues> content_value_pool) {
        _content = context.getContentResolver();
        _content_uri = content_uri;
        _query_parameters = newHashMap();
        _values = null;
        _content_value_pool = content_value_pool;
    }

    /**
     * <p>Insert a record with the set values.</p>
     */
    public Uri insert() {
        try {
            return _content.insert(uriWithAppendedQueryParameters(), _values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Inserts a record with the set values.</p>
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     * @param from_sync_adapter indicates when the insert is called from the sync adapter
     */
    public Uri insert(boolean notify_change, boolean from_sync_adapter) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

        return insert();
    }

    /**
     * <p>Updates a record with the given query</p>
     * @param query the given query
     */
    public int update(QueryBuilder query) {
        try {
            return _content.update(uriWithAppendedQueryParameters(), _values, query.toString(), query.argumentsAsArray());
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Updates with the given query</p>
     * @param query the given query
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     * @param from_sync_adapter indicates when the insert is called from the sync adapter
     */
    public int update(QueryBuilder query, boolean notify_change, boolean from_sync_adapter) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

        return update(query);
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     */
    @SuppressWarnings("ConstantConditions")
    public int update(long id) {
        try {
            return _content.update(uriWithAppendedQueryParameters().buildUpon().appendPath(Long.toString(id)).build()
                    , _values, null, null);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(long id, boolean notify_change) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));

        return update(id);
    }

    /**
     * <p>Insert a record with the set values.</p>
     * @param uri Uri over which to insert
     */
    public Uri insert(Uri uri) {
        try {
            return _content.insert(uriWithAppendedQueryParameters(uri), _values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Inserts a record with the set values.</p>
     * @param uri Uri over which to insert
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     * @param from_sync_adapter indicates when the insert is called from the sync adapter
     */
    public Uri insert(Uri uri, boolean notify_change, boolean from_sync_adapter) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

        return insert(uri);
    }

    /**
     * <p>Updates a record with the given query</p>
     * @param uri Uri over which to update
     * @param query the given query
     */
    public int update(Uri uri, QueryBuilder query) {
        try {
            return _content.update(uriWithAppendedQueryParameters(uri), _values, query.toString(), query.argumentsAsArray());
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Updates with the given query</p>
     * @param uri Uri over which to update
     * @param query the given query
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     * @param from_sync_adapter indicates when the insert is called from the sync adapter
     */
    public int update(Uri uri, QueryBuilder query, boolean notify_change, boolean from_sync_adapter) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

        return update(uri, query);
    }

    /**
     * <p>Updates with the given id</p>
     * @param uri Uri over which to update
     * @param id the given id
     */
    @SuppressWarnings("ConstantConditions")
    public int update(Uri uri, long id) {
        try {
            return _content.update(uriWithAppendedQueryParameters(uri).buildUpon().appendPath(Long.toString(id)).build()
                    , _values, null, null);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Updates with the given id</p>
     * @param uri Uri over which to update
     * @param id the given id
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(Uri uri, long id, boolean notify_change) {
        appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));

        return update(uri, id);
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an insert operation.</p>
     *
     * @see android.content.ContentProviderOperation#newInsert(android.net.Uri)
     */
    public ContentProviderOperation.Builder toInsertOperationBuilder() {
        try {
            return ContentProviderOperation
                    .newInsert(uriWithAppendedQueryParameters())
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an update operation.</p>
     *
     * @see android.content.ContentProviderOperation#newUpdate(android.net.Uri)
     */
    public ContentProviderOperation.Builder toUpdateOperationBuilder() {
        try {
            return ContentProviderOperation
                    .newUpdate(uriWithAppendedQueryParameters())
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an delete operation.</p>
     *
     * @see android.content.ContentProviderOperation#newDelete(android.net.Uri)
     */
    public ContentProviderOperation.Builder toDeleteOperationBuilder() {
        try {
            return ContentProviderOperation
                    .newDelete(uriWithAppendedQueryParameters())
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an assert query operation.</p>
     *
     * @see android.content.ContentProviderOperation#newAssertQuery(android.net.Uri)
     */
    public ContentProviderOperation.Builder toAssertQueryOperationBuilder() {
        try {
            return ContentProviderOperation
                    .newAssertQuery(uriWithAppendedQueryParameters())
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an insert operation.</p>
     * @param uri Uri over which to insert
     *
     * @see android.content.ContentProviderOperation#newInsert(android.net.Uri)
     */
    public ContentProviderOperation.Builder toInsertOperationBuilder(Uri uri) {
        try {
            return ContentProviderOperation
                    .newInsert(uriWithAppendedQueryParameters(uri))
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an update operation.</p>
     * @param uri Uri over which to update
     *
     * @see android.content.ContentProviderOperation#newUpdate(android.net.Uri)
     */
    public ContentProviderOperation.Builder toUpdateOperationBuilder(Uri uri) {
        try {
            return ContentProviderOperation
                    .newUpdate(uriWithAppendedQueryParameters(uri))
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an delete operation.</p>
     * @param uri Uri over which to delete
     *
     * @see android.content.ContentProviderOperation#newDelete(android.net.Uri)
     */
    public ContentProviderOperation.Builder toDeleteOperationBuilder(Uri uri) {
        try {
            return ContentProviderOperation
                    .newDelete(uriWithAppendedQueryParameters(uri))
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an assert query operation.</p>
     * @param uri Uri over which to assert query
     *
     * @see android.content.ContentProviderOperation#newAssertQuery(android.net.Uri)
     */
    public ContentProviderOperation.Builder toAssertQueryOperationBuilder(Uri uri) {
        try {
            return ContentProviderOperation
                    .newAssertQuery(uriWithAppendedQueryParameters(uri))
                    .withValues(_values);
        } finally {
            cleanUp();
        }
    }

    public void appendQueryParameter(String key, String value) {
        _query_parameters.put(key, value);
    }

    @SuppressWarnings("ConstantConditions")
    @Nonnull private Uri uriWithAppendedQueryParameters() {
        if (_content_uri == null) throw new IllegalStateException("calendar with null content URI.");
        return uriWithAppendedQueryParameters(_content_uri);
    }

    @SuppressWarnings("ConstantConditions")
    @Nonnull private Uri uriWithAppendedQueryParameters(Uri uri) {
        if (_query_parameters.isEmpty())
            return uri;
        else
            for (Map.Entry<String, String> parameter : _query_parameters.entrySet())
                uri = uri.buildUpon().appendQueryParameter(parameter.getKey(), parameter.getValue()).build();
        _query_parameters.clear();
        return uri;
    }

    @Nonnull protected ContentValues contentValues() {
        try {
            if (_values == null) _values = _content_value_pool.getObj();
            return _values;
        } catch (PoolException _) {
            return new ContentValues();
        }
    }

    private void cleanUp() {
        _values.clear();
        if (_content_value_pool != null) _content_value_pool.returnObj(_values);
    }

    private ContentValues _values;
    private final ObjectPool<ContentValues> _content_value_pool;
    private Uri _content_uri;
    private ContentResolver _content;
    private HashMap<String, String> _query_parameters;
}
