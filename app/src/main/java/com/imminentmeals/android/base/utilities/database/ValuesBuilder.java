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

import static com.google.common.collect.Maps.newHashMap;


/**
 * <p>Base database entry builder</p>
 */
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
    }

    /**
     * <p>Insert a record with the set values.</p>
     */
    public Uri insert() {
        try {
            return _content.insert(uriWithAppendedQueryParameters(), _values);
        } finally {
            _values.clear();
        }
    }

    /**
     * <p>Inserts a record with the set values.</p>
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     * @param from_sync_adapter indicates when the insert is called from the sync adapter
     */
    public Uri insert(boolean notify_change, boolean from_sync_adapter) {
        appendQueryParamenter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParamenter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

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
            _values.clear();
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
        appendQueryParamenter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));
        appendQueryParamenter(BaseContentProvider.PARAM_SHOULD_NOTIFY_SYNC_ADAPTER, Boolean.toString(!from_sync_adapter));

        return update(query);
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     */
    public int update(long id) {
        try {
            return _content.update(uriWithAppendedQueryParameters().buildUpon().appendPath(Long.toString(id)).build(),
                    _values, null, null);
        } finally {
            _values.clear();
        }
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(long id, boolean notify_change) {
        appendQueryParamenter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(notify_change));

        return update(id);
    }

    /**
     * <p>Gets the underlying ContentValues built so far by this builder.</p>
     */
    public ContentValues getValues() {
        return _values;
    }

    public ValuesBuilder beginTransaction() {
        _content.call(_content_uri, BaseContentProvider.METHOD_BEGIN_TRANSACTION, null, null);
        return this;
    }

    public ValuesBuilder endTransaction() {
        _content.call(_content_uri, BaseContentProvider.METHOD_END_TRANSACTION, null, null);
        return this;
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
            _values.clear();
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
            _values.clear();
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
            _values.clear();
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
            _values.clear();
        }
    }

    public void appendQueryParamenter(String key, String value) {
        _query_parameters.put(key, value);
    }

    private Uri uriWithAppendedQueryParameters() {
        Uri uri = _content_uri;
        if (_query_parameters.isEmpty())
            return uri;
        else
            for (Map.Entry<String, String> parameter : _query_parameters.entrySet())
                uri = uri.buildUpon().appendQueryParameter(parameter.getKey(), parameter.getValue()).build();
        _query_parameters.clear();
        return uri;
    }

    protected final ContentValues _values;
    private Uri _content_uri;
    private ContentResolver _content;
    private HashMap<String, String> _query_parameters;
}
