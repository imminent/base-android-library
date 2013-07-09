/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;

import com.google.common.io.Closeables;

import java.io.IOException;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;


/**
 * <p>Base ActiveRecord implementation.</p>
 */
@ParametersAreNonnullByDefault
public abstract class ActiveRecord {

    @Nonnegative public long id() {
        return _id;
    }

    public void setId(@Nonnegative long id) {
        _id = id;
    }

    protected ActiveRecord(Uri content_uri) {
        _content_uri = content_uri;
    }

    protected abstract String[] projection();

    protected abstract ValuesBuilder createBuilder();

    protected abstract ContentResolver getContentResolver();

    public abstract void makeDirty(boolean dirty_record);

    protected abstract void setPropertiesFromCursor(Cursor cursor);

    /**
     * <p>If the <b>id</b> column for this record is zero, then saving will cause
     * an insert, after saving the <b>id</b> will be set with the new id of the inserted record,
     * If the id column is not zero, then saving will cause an update to the record with the <b>id</b>.
     * </p>
     *
     * @return the new <b>id</b> of the record, the id property of this active record
     * will also be updated
     */
    public long save(){
        final ValuesBuilder builder = createBuilder();

        if(_id > 0)
            builder.update(_id);
        else
            _id = ContentUris.parseId(builder.insert());

        makeDirty(false);

        return _id;
    }

    /**
     * <p>Same as {@link #save()} but with the option to notify content observers that the record
     * has changed, by default, content observers are always notified, set to false to disable.</p>
     * @param notify_change whether to notify observers, default is true
     * @return the new <b>id</b> of the record, the id property of this active record
     * will also be updated
     */
    public long save(boolean notify_change){
        final ValuesBuilder builder = createBuilder();

        if(_id > 0)
            builder.update(_id, notify_change);
        else
            _id = ContentUris.parseId(builder.insert(notify_change));

        makeDirty(false);

        return _id;
    }

    public boolean delete(){
        final boolean result = getContentResolver().delete(
                _content_uri.buildUpon()
                            .appendPath(Long.toString(_id)).build(), null, null) > 0;

        makeDirty(false);

        return result;
    }

    public boolean delete(boolean notify_change){
        final Uri uri = _content_uri.buildUpon()
                .appendPath(String.valueOf(_id))
                .appendQueryParameter(
                        BaseContentProvider.PARAM_SHOULD_NOTIFY,
                        Boolean.toString(notify_change)).build();

        final boolean result = getContentResolver().delete(uri, null, null) > 0;

        makeDirty(false);

        return result;
    }

    public void reload(){
        if (_id == 0)
            return;

        Cursor cursor = null;

        try {
            cursor = getContentResolver().query(_content_uri.buildUpon()
                    .appendPath(Long.toString(_id)).build(), projection(), null, null, null);

            if(cursor.moveToFirst()) {
                setPropertiesFromCursor(cursor);
                makeDirty(false);
            }
        } finally {
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }
    }

    protected static boolean booleanFromInt(int value) {
        return value != 0;
    }

    protected final @Nonnull Uri _content_uri;
    private long _id;
}
