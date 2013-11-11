/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Base {@link android.database.sqlite.SQLiteOpenHelper}.</p>
 */
@ParametersAreNonnullByDefault
public abstract class BaseSqliteOpenHelper extends SQLiteOpenHelper {

    public BaseSqliteOpenHelper(Context context, String name, CursorFactory factory, int version) {
        super(context, name, factory, version);
        _version = version;
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        onUpgrade(database, 0, _version);
    }

    /**
     * <p>Migrates from an older version of the database.</p>
     * @param database the database
     * @param old_version the old version number
     * @param new_version the new version number
     */
    @Override
    public void onUpgrade(SQLiteDatabase database, int old_version, int new_version) {
        for (int i = (old_version + 1); i <= new_version; i++)
            createMigration(i).up(database);
    }

    /**
     * <p>Retrieves the {@link SqliteMigration} for the given version.</p>
     * @param version the given version
     * @return the migration from {@code version - 1} to {@code version}
     */
    protected abstract SqliteMigration createMigration(int version);

    private int _version;
}
