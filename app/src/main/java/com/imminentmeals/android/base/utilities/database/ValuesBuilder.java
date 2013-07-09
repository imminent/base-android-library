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
    }

    /**
     * <p>Insert a record with the set values.</p>
     */
    public Uri insert() {
        return _content.insert(_content_uri, _values);
    }

    /**
     * <p>Inserts a record with the set values.</p>
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public Uri insert(boolean notify_change) {

        Uri uri = _content_uri.buildUpon()
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
        return _content.update(_content_uri, _values, query.toString(), query.argumentsAsArray());
    }

    /**
     * <p>Updates with the given query</p>
     * @param query the given query
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(QueryBuilder query, boolean notify_change) {

        final Uri uri = _content_uri.buildUpon()
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
        return _content.update(_content_uri.buildUpon().appendPath(Long.toString(id)).build(), _values, null, null);
    }

    /**
     * <p>Updates with the given id</p>
     * @param id the given id
     * @param notify_change indicates when the {@link android.content.ContentProvider} should notify observers
     *                      when content is modified
     */
    public int update(long id, boolean notify_change) {
        final Uri uri = _content_uri.buildUpon()
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
                .newInsert(_content_uri)
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
                .newUpdate(_content_uri)
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
                .newDelete(_content_uri)
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
                .newAssertQuery(_content_uri)
                .withValues(_values);
    }

    protected ContentValues _values = new ContentValues();
    private Uri _content_uri;
    private ContentResolver _content;
}
