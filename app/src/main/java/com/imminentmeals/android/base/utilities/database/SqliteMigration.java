/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.database.sqlite.SQLiteDatabase;

import java.util.Locale;

import static android.text.TextUtils.join;

/**
 * <p>Base for all database migrations.</p>
 */
public abstract class SqliteMigration {

    public abstract void up(SQLiteDatabase database);

    protected String replaceOnConflictKeys(String... keys) {
        return String.format(Locale.US, _REPLACE_ON_CONFLICT, join(",", keys));
    }

    protected static final String CREATE_TABLE = "CREATE TABLE ";
    protected static final String CREATE_TRIGGER = "CREATE TRIGGER ";
    protected static final String CREATE_VIEW = "CREATE VIEW ";
    protected static final String IS_INTEGER_PRIMARY_KEY = " INTEGER PRIMARY KEY AUTOINCREMENT";
    protected static final String IS_INTEGER = " INTEGER";
    protected static final String IS_NON_NULL_INTEGER = " INTEGER NOT NULL";
    protected static final String IS_TEXT = " TEXT";
    protected static final String IS_NON_NULL_TEXT = " TEXT NOT NULL";
    protected static final String IS_INTEGER_PRIMARY_KEY_AND = IS_INTEGER_PRIMARY_KEY + ", ";
    protected static final String IS_INTEGER_AND = IS_INTEGER + ", ";
    protected static final String IS_NON_NULL_INTEGER_AND = IS_NON_NULL_INTEGER + ", ";
    protected static final String IS_TEXT_AND = IS_TEXT + ", ";
    protected static final String IS_NON_NULL_TEXT_AND = IS_NON_NULL_TEXT + ", ";
    protected static final String IS_DEFERRABLE = " DEFERRABLE INITIALLY DEFERRED";
    protected static final String IS_DEFERRABLE_AND = IS_DEFERRABLE + ", ";
    private static final String _REPLACE_ON_CONFLICT = "UNIQUE (%s) ON CONFLICT REPLACE";
}
