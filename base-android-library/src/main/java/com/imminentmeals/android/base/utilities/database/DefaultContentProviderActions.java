/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.google.common.io.Closeables;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import nf.fr.eraasoft.pool.ObjectPool;
import nf.fr.eraasoft.pool.PoolException;

import static com.google.common.collect.Lists.newArrayList;
import static com.imminentmeals.android.base.utilities.database.QueryBuilder.Op;

/**
 * <p>Provides default implementation for the CRUD {@link android.content.ContentProvider} actions</p>
 *
 * <p>Has two modes: one with {@link android.net.Uri URI}'s containing ID's, enabled by
 * setting {@code should_expect_appended_id} during construction and one without ID's.
 * </p>
 *
 * @see ContentProviderActions
 */
@SuppressWarnings("UnusedDeclaration") @ParametersAreNonnullByDefault
public class DefaultContentProviderActions extends ContentProviderActions {

    /**
     * <p>Sets up the default action performer.</p>
     * @param table the source table
     * @param should_expect_appended_id indicates URI's are expected to have appended ID's
     * @param active_record_factory the factory that produces the {@link ActiveRecord}s
     */
    public <T extends ActiveRecord> DefaultContentProviderActions(String table, boolean should_expect_appended_id
            , @Nullable ActiveRecordFactory<T> active_record_factory
            , ObjectPool<QueryBuilder> query) {
        _table = table;
        _should_expect_appended_id = should_expect_appended_id;
        _active_record_factory = active_record_factory;
        _query = query;
    }

    /**
     * <p>Sets up the default action performer.</p>
     * @param table the source table
     * @param should_expect_appended_id indicates URI's are expected to have appended ID's
     */
    public DefaultContentProviderActions(String table, boolean should_expect_appended_id
            , ObjectPool<QueryBuilder> query) {
        this(table, should_expect_appended_id, null, query);
    }

    /**
     * <p>Deletes content specified by the given {@link android.net.Uri URI} and content selection.</p>
     * @param content the collection of content
     * @param uri the given URI
     * @param selection the given selection
     * @param selection_arguments the given selection arguments
     * @return the number of rows affected
     */
    @Override
    public int delete(BaseContentProvider content, Uri uri, @Nullable String selection,
                      @Nullable String[] selection_arguments) {
        final SQLiteDatabase database = content.getOpenHelper().getWritableDatabase();
        if (database == null) return -1;

        QueryBuilder query = null;
        try {
           query = _query.getObj();
            return _should_expect_appended_id
                    ? query
                        .expression(BaseColumns._ID, Op.IS_EQUAL_TO, ContentUris.parseId(uri))
                        .append(selection, selection_arguments)
                        .delete(database, _table)
                    : database.delete(_table, selection, selection_arguments);
        } catch (PoolException exception) {
            return -1;
        } finally {
            _query.returnObj(query);
        }
    }

    /**
     * <p>Creates new content with the given {@linkplain android.content.ContentValues values}.</p>
     * @param content the collection of content
     * @param uri the URI to the content
     * @param values the values to attribute to the new content
     * @return URI to the new content, or {@code null} if it wasn't created
     */
    @Override
    @CheckForNull public Uri insert(BaseContentProvider content, Uri uri, ContentValues values) {
        // Insertion with a specified ID has an unspecified behavior
        if (_should_expect_appended_id) return null;

        final SQLiteDatabase database = content.getOpenHelper().getWritableDatabase();
        final long id = database != null? database.insertOrThrow(_table, null, values) : -1;

        return id > -1? ContentUris.withAppendedId(uri, id) : null;
    }

    /**
     * <p>Updates the content specified by the given {@link android.net.Uri URI} and content selection with the
     * given {@linkplain android.content.ContentValues values}.</p>
     * @param content the collection of content
     * @param uri the given URI
     * @param values the values to update
     * @param selection the given selection
     * @param selection_arguments the given selection arguments
     * @return the number of rows affected
     */
    @Override
    public int update(BaseContentProvider content, Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selection_arguments) {
        final SQLiteDatabase database = content.getOpenHelper().getWritableDatabase();
        if (database == null) return -1;

        QueryBuilder query = null;
        try {
            query = _query.getObj();
            return _should_expect_appended_id
                    ? query
                        .expression(BaseColumns._ID, Op.IS_EQUAL_TO, ContentUris.parseId(uri))
                        .append(selection, selection_arguments)
                        .update(database, _table, values)
                    : database.update(_table, values, selection, selection_arguments);
        } catch (PoolException exception) {
            return -1;
        } finally {
            _query.returnObj(query);
        }
    }

    /**
     * <p>Retrieves the content specified by the given {@link android.net.Uri URI} and content selection.</p>
     * @param content the collection of content
     * @param uri the given URI
     * @param projection the vector space on which to project the result
     * @param selection the given selection
     * @param selection_arguments the given selection arguments
     * @param sort_order the order to apply to the result space
     * @return a {@link android.database.Cursor} to the result
     */
    @Override
    @CheckForNull public Cursor query(BaseContentProvider content, Uri uri,
                                 @Nullable String[] projection, @Nullable String selection,
                                 @Nullable String[] selection_arguments, @Nullable String sort_order){
        final SQLiteDatabase database = content.getOpenHelper().getReadableDatabase();
        if (database == null) return null;

        QueryBuilder query = null;
        try {
            query = _query.getObj();
            return _should_expect_appended_id
                    ? query
                        .expression(BaseColumns._ID, QueryBuilder.Op.IS_EQUAL_TO, ContentUris.parseId(uri))
                        .append(selection, selection_arguments)
                        .query(database, _table, projection, sort_order)
                    : database.query(_table, projection, selection, selection_arguments, null, null, sort_order);
        } catch (PoolException exception) {
            return null;
        } finally {
            _query.returnObj(query);
        }
    }

    /**
     * <p>Creates a collection of new content with the given {@linkplain android.content.ContentValues collection of values}</p>
     * @param content the collection of content
     * @param values the given collection of values
     * @return the number of new content created
     */
    @Override
    public int bulkInsert(BaseContentProvider content, ContentValues[] values) {
        final SQLiteDatabase database = content.getOpenHelper().getWritableDatabase();
        if (database == null) return -1;

        try {
            database.beginTransaction();
            for (ContentValues value : values) {
                database.insertOrThrow(_table, null, value);
                database.yieldIfContendedSafely();
            }
            database.setTransactionSuccessful();
        } finally {
            database.endTransaction();
        }

        return values.length;
    }


    @SuppressWarnings("unchecked")
    @Override
    public <T extends ActiveRecord> List<T> selectRecords(BaseContentProvider content,
                                                          Uri uri, QueryBuilder query,
                                                          @Nullable String sort_order) {
        assert _active_record_factory != null;
        final SQLiteDatabase database = content.getOpenHelper().getReadableDatabase();
        if (database == null) return new ArrayList<>();
        Cursor cursor = null;
        final ArrayList<T> items = newArrayList();

        try {
            cursor = database.query(_table, _active_record_factory.projection(), query.toString(), query.argumentsAsArray(), null, null, sort_order);

            while (cursor.moveToNext())
                items.add((T) _active_record_factory.create(cursor));
        } finally {
            try {
                Closeables.close(cursor, true);
            } catch (IOException ignored) { }
        }

        return items;
    }

    @Override
    public <T extends ActiveRecord> Iterable<T> queryRecords(BaseContentProvider content,
                                                             Uri uri, QueryBuilder query,
                                                             @Nullable String sort_order) {
        assert _active_record_factory != null;

        final SQLiteDatabase database = content.getOpenHelper().getReadableDatabase();
        if (database == null) return new Iterable<T>() {

            @Override public Iterator<T> iterator() {
                return new ArrayList<T>().iterator();
            }
        };
        final Cursor cursor = database.query(_table, _active_record_factory.projection(), query.toString(), query.argumentsAsArray(), null, null, sort_order);
        return new Iterable<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public Iterator<T> iterator() {
                return new CursorActiveRecordIterator<>(cursor, (ActiveRecordFactory<T>) _active_record_factory);
            }
        };
    }

    /** The content source */
    private String _table;
    /** Produces new {@link ActiveRecord}s of the appropriate type */
    private ActiveRecordFactory<?> _active_record_factory;
    /** Indicates when the {@link android.content.ContentProvider} should expect an ID in the request URIs */
    private boolean _should_expect_appended_id;
    private final ObjectPool<QueryBuilder> _query;
}
