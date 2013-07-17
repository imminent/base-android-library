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
     * @param contentUri The content URI on which the builder operates
     */
    protected ValuesBuilder(Context context, Uri contentUri) {
        _content = context.getContentResolver();
        _content_uri = contentUri;
        _query_parameters = newHashMap();
    }

    /**
     * <p>Insert a record with the set values.</p>
     */
    public Uri insert() {
        return _content.insert(uriWithAppendedQueryParameters(), _values);
    }

    /**
     * <p>Inserts a record with the set values.</p>
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public Uri insert(boolean notify_change) {

        Uri uri = uriWithAppendedQueryParameters().buildUpon()
                .appendQueryParameter(
                        BaseContentProvider.PARAM_SHOULD_NOTIFY,
                        Boolean.toString(notify_change)).build();

        return _content.insert(uri, _values);
    }

    /**
     * <p>Updates a record with the given query</p>
     * @param query the given query
     */
    public int update(QueryBuilder query) {
        return _content.update(uriWithAppendedQueryParameters(), _values, query.toString(), query.argumentsAsArray());
    }

    /**
     * <p>Updates with the given query</p>
     * @param query the given query
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(QueryBuilder query, boolean notify_change) {

        final Uri uri = uriWithAppendedQueryParameters().buildUpon()
                .appendQueryParameter(
                        BaseContentProvider.PARAM_SHOULD_NOTIFY,
                        Boolean.toString(notify_change)).build();

        return _content.update(uri, _values, query.toString(), query.argumentsAsArray());
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     */
    public int update(long id) {
        return _content.update(uriWithAppendedQueryParameters().buildUpon().appendPath(Long.toString(id)).build(),
                _values, null, null);
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(long id, boolean notify_change) {
        final Uri uri = uriWithAppendedQueryParameters().buildUpon()
                .appendPath(String.valueOf(id))
                .appendQueryParameter(
                        BaseContentProvider.PARAM_SHOULD_NOTIFY,
                        Boolean.toString(notify_change)).build();

        return _content.update(uri, _values, null, null);
    }

    /**
     * <p>Gets the underlying ContentValues built so far by this builder.</p>
     */
    public ContentValues getValues() {
        return _values;
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an insert operation.</p>
     *
     * @see android.content.ContentProviderOperation#newInsert(android.net.Uri)
     */
    public ContentProviderOperation.Builder toInsertOperationBuilder() {
        return ContentProviderOperation
                .newInsert(uriWithAppendedQueryParameters())
                .withValues(_values);
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an update operation.</p>
     *
     * @see android.content.ContentProviderOperation#newUpdate(android.net.Uri)
     */
    public ContentProviderOperation.Builder toUpdateOperationBuilder() {
        return ContentProviderOperation
                .newUpdate(uriWithAppendedQueryParameters())
                .withValues(_values);
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an delete operation.</p>
     *
     * @see android.content.ContentProviderOperation#newDelete(android.net.Uri)
     */
    public ContentProviderOperation.Builder toDeleteOperationBuilder() {
        return ContentProviderOperation
                .newDelete(uriWithAppendedQueryParameters())
                .withValues(_values);
    }

    /**
     * <p>Takes the values in this builder and creates a new
     * {@link android.content.ContentProviderOperation} as an assert query operation.</p>
     *
     * @see android.content.ContentProviderOperation#newAssertQuery(android.net.Uri)
     */
    public ContentProviderOperation.Builder toAssertQueryOperationBuilder() {
        return ContentProviderOperation
                .newAssertQuery(uriWithAppendedQueryParameters())
                .withValues(_values);
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

    protected ContentValues _values = new ContentValues();
    private Uri _content_uri;
    private ContentResolver _content;
    private HashMap<String, String> _query_parameters;
}
