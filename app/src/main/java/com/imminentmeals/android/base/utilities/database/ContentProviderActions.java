/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class ContentProviderActions {

    public int delete(BaseContentProvider content, Uri uri, String selection,
                      @Nullable String[] selection_arguments){
        return -1;
    }

    @CheckForNull public Uri insert(BaseContentProvider content, Uri uri, ContentValues values){
        return null;
    }

    @CheckForNull public Cursor query(BaseContentProvider content, Uri uri, @Nullable String[] projection,
                                  @Nullable String selection, @Nullable String[] selection_arguments,
                        String sort_order){
        return null;
    }

    public int update(BaseContentProvider content, Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selection_arguments){
        return -1;
    }

    public int bulkInsert(BaseContentProvider content, ContentValues[] values) {
        return -1;
    }

    public <T extends ActiveRecord> List<T> selectRecords(BaseContentProvider content, Uri uri, QueryBuilder query, String sort_order) {
        return null;
    }

    public <T extends ActiveRecord> Iterable<T> queryRecords(BaseContentProvider content, Uri uri, QueryBuilder query, String sort_order) {
        return null;
    }
}
