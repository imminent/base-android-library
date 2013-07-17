/*******************************************************************************
 * Copyright (c) 2012, Robotoworks Limited
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/
package com.imminentmeals.android.base.utilities.database;

import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.v4.content.CursorLoader;

import com.google.common.io.Closeables;
import com.imminentmeals.android.base.data.DataModule;
import com.imminentmeals.android.base.utilities.StringUtilities;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static com.google.common.collect.Lists.newArrayList;

/**
 * <p>Construct content provider/database queries using a fluent API.</p>
 *
 * <p>Expressions can be chained using the {@code expression()} overloads, by default expressions are AND'd together
 * unless an explicit call is made to {@link #or()} or {@link #and()} between each expression, e.g.:</p>
 *
 * <pre><code>Cursor cursor = QueryBuilder.newQuery()
 *     .expression(Books.TITLE, Op.IS_LIKE, "A%")
 *     .or()
 *     .expression(Books.TITLE, Op.IS_LIKE, "B%")
 *     .select(Books.CONTENT_URI,
 *         new String[] {
 *             Books._ID,
 *             Books.TITLE
 *         });</code></pre>
 *
 * <p>If a query ends with either {@link #or()} or {@link #and()} they will not be included when executing the query
 * since it is an error to end a query without the right operand of an expression.</p>
 */
@ParametersAreNonnullByDefault
public class QueryBuilder {

    /**
     * SQLite expression literals.
     */
    public static enum Literal {
        NULL("NULL"), CURRENT_TIME("CURRENT_TIME"), CURRENT_DATE("CURRENT_DATE"), CURRENT_TIMESTAMP("CURRENT_TIMESTAMP");

        Literal(String value) {
            this.value = value;
        }

        private final String value;
    }

    /**
     * <p>Comparison operator constants used in {@link com.imminentmeals.android.base.utilities.database.QueryBuilder} expressions.</p>
     *
     * <h2>Example</h2>
     * <pre><code>BooksRecord record = QueryBuilder.newQuery()
     *     .expression(Books.TITLE, Op.IS_EQUAL_TO, "Musashi")
     *     .selectFirst(Books.CONTENT_URI);
     * </code></pre>
     *
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, boolean)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, double)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, float)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, int)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, long)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, String)
     * @see com.imminentmeals.android.base.utilities.database.QueryBuilder#expression(String, String, com.imminentmeals.android.base.utilities.database.QueryBuilder.Literal)
     */
    public interface Op {
        /**
         * The equals (=) operator
         */
        String IS_EQUAL_TO = " = ";
        /**
         * The not equal (!=) operator
         */
        String NOT_EQUAL_TO = " != ";
        /**
         * The greater than (>) operator
         */
        String IS_GREATER_THAN = " > ";
        /**
         * The less than (<) operator
         */
        String IS_LESS_THAN = " < ";
        /**
         * The greater than or equal (>=) operator
         */
        String IS_NO_LESS_THAN = " >= ";
        /**
         * The less than or equal (<=) operator
         */
        String IS_NOT_GREATER_THAN = " <= ";
        /**
         * The LIKE operator
         */
        String IS_LIKE = " LIKE ";
        /**
         * The IS operator
         */
        String IS = " IS ";
        /**
         * The IS NOT operator
         */
        String IS_NOT = " IS NOT ";
        /**
         * The REGEXP operator
         */
        String MATCHES_EXPRESSION = " REGEXP ";
    }

    public QueryBuilder() {
        _query_string = new StringBuilder();
    }

    @Override
    public String toString() {
        return _query_string.toString();
    }

    public static QueryBuilder newQuery() {
        return new QueryBuilder();
    }

    /**
     * Retrieves the list of expression arguments.
     * @return the list of expression arguments
     */
    public List<String> arguments() {
        return _arguments;
    }

    /**
     * Retrieves the list of expression arguments.
     * @return the list of expression arguments
     */
    public String[] argumentsAsArray() {
        return _arguments.toArray(new String[_arguments.size()]);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, String argument) {
        ensureOp();
        _query_string.append(column).append(operator).append("?");
        _arguments.add(argument);
        _next_operator = null;

        return this;
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument.
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, Literal argument) {
        ensureOp();
        _query_string.append(column).append(operator).append(" ").append(argument.value);
        _next_operator = null;

        return this;
    }

    /**
     * <p>An ISNULL expression on the given column name.</p>
     * @param column the column name on the left side of the expression
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expressionIsNull(String column) {
        ensureOp();
        _query_string.append(column).append(" ISNULL");
        _next_operator = null;

        return this;
    }

    /**
     * A NOTNULL expression on the given column name.
     * @param column the column name on the left side of the expression
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expressionNotNull(String column) {
        ensureOp();
        _query_string.append(column).append(" NOTNULL");
        _next_operator = null;

        return this;
    }

    /**
     * Add a sub-expression to this expression, the sub-expression will be enclosed in parenthesises, like
     * a=? AND (b=?) AND c=? where "b=?" is the sub-expression
     * @param query the sub-expression query
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(QueryBuilder query) {

        final List<String> arguments = query.arguments();


        if (!arguments.isEmpty()) {
            ensureOp();
            _query_string.append("(").append(query).append(")");
            _arguments.addAll(arguments);
        }

        _next_operator = null;

        return this;
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, boolean argument) {
        return expression(column, operator, argument? "1" : "0");
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, int argument) {
        return expression(column, operator, Integer.toString(argument));
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, long argument) {
        return expression(column, operator, Long.toString(argument));
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, float argument) {
        return expression(column, operator, Float.toString(argument));
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder expression(String column, String operator, double argument) {
        return expression(column, operator, Double.toString(argument));
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code null}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, @Nullable String argument) {
        if (argument == null) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code 0}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, int argument) {
        if (argument == 0) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code false}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, boolean argument) {
        if (!argument) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code 0}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, long argument) {
        if(argument == 0) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code 0}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, float argument) {
        if(argument == 0) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds an expression to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression. Expression is omitted when
     * the given argument is {@code 0}.</p>
     * @param column the column on the left side of the expression
     * @param operator the operator, see {@link com.imminentmeals.android.base.utilities.database.QueryBuilder.Op} for available operators
     * @param argument the argument for the right side of the expression to be bound at a
     *                 later time
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder optional(String column, String operator, double argument) {
        if(argument == 0) return this;
        return expression(column, operator, argument);
    }

    /**
     * <p>Adds a query to the end of the currently added expressions, if
     * no previous boolean operator has been given ({@link #and()} or {@link #or()}) then
     * AND will be used by default when appending this expression.</p>
     * @param query the given query
     * @param arguments the list of arguments to the query
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder append(@Nullable String query, String... arguments) {

        if (StringUtilities.notEmpty(query)) {
            ensureOp();

            _query_string.append(query);

            if (arguments != null && arguments.length > 0)
                Collections.addAll(_arguments, arguments);

            _next_operator = null;
        }

        return this;
    }

    /**
     * Joins the next expression via an {@link #AND}.
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder and() {
        _next_operator = AND;

        return this;
    }

    /**
     * Joins the next expression via an {@link #OR}
     * @return the {@link com.imminentmeals.android.base.utilities.database.QueryBuilder}
     */
    public QueryBuilder or() {
        _next_operator = OR;

        return this;
    }

    /**
     * Queries the given table of the given database, projecting the result onto the given projection and ordering it
     * by the given specification.
     * @param database the given database
     * @param table the given table
     * @param projection the vector space on which to project the result
     * @param order_by the order to apply to the result space
     * @return a {@link android.database.Cursor} to the result
     */
    public Cursor query(SQLiteDatabase database, String table, @Nullable String[] projection,
                        @Nullable String order_by) {
        return database.query(table, projection, _query_string.toString(), argumentsAsArray(), null, null, order_by);
    }

    /**
     * Queries the given table of the given database for the integer value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public int intFromFirstRow(SQLiteDatabase database, String table, String column) {
        return intFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the integer value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public int intFromFirstRow(SQLiteDatabase database, String table, String column, @Nullable String order_by) {
        Cursor cursor = null;
        int value = 0;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if(cursor.moveToFirst())
                value = cursor.getInt(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the long value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public long longFromFirstRow(SQLiteDatabase database, String table, String column) {
        return firstLong(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the long value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public long firstLong(SQLiteDatabase database, String table, String column, @Nullable String order_by) {
        Cursor cursor = null;
        long value = 0;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if(cursor.moveToFirst())
                value = cursor.getLong(0);

        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the double value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public double doubleFromFirstRow(SQLiteDatabase database, String table, String column) {
        return doubleFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the double value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public double doubleFromFirstRow(SQLiteDatabase database, String table, String column,
                                     @Nullable String order_by) {
        Cursor cursor = null;
        double value = 0;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if(cursor.moveToFirst())
                value = cursor.getDouble(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the float value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public float floatFromFirstRow(SQLiteDatabase database, String table, String column) {
        return floatFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the float value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public float floatFromFirstRow(SQLiteDatabase database, String table, String column,
                                   @Nullable String order_by) {
        Cursor cursor = null;
        float value = 0;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if (cursor.moveToFirst())
                value = cursor.getFloat(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the short value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public short shortFromFirstRow(SQLiteDatabase database, String table, String column) {
        return shortFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the short value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public short shortFromFirstRow(SQLiteDatabase database, String table, String column,
                                   @Nullable String order_by) {
        Cursor cursor = null;
        short value = 0;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if( cursor.moveToFirst())
                value = cursor.getShort(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException e) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the blob value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public byte[] blobFromFirstRow(SQLiteDatabase database, String table, String column) {
        return blobFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the blob value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public byte[] blobFromFirstRow(SQLiteDatabase database, String table, String column,
                                                 @Nullable String order_by) {
        Cursor cursor = null;
        byte[] value = null;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if (cursor.moveToFirst())
                value = cursor.getBlob(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries the given table of the given database for the boolean value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public boolean booleanFromFirstRow(SQLiteDatabase database, String table, String column) {
        return booleanFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the boolean value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public boolean booleanFromFirstRow(SQLiteDatabase database, String table, String column,
                                       @Nullable String order_by) {
        return shortFromFirstRow(database, table, column, order_by) != 0;
    }

    /**
     * Queries the given table of the given database for the String value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public String stringFromFirstRow(SQLiteDatabase database, String table, String column) {
        return stringFromFirstRow(database, table, column, null);
    }

    /**
     * Queries the given table of the given database for the String value of the given column from the first row in the result.
     * @param database the given database
     * @param table the given table
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public String stringFromFirstRow(SQLiteDatabase database, String table, String column,
                                                   @Nullable String order_by) {
        Cursor cursor = null;
        String value = null;
        try {
            cursor = query(database, table, new String[] { column }, order_by);

            if (cursor.moveToFirst())
                value = cursor.getString(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException e) { }
        }

        return value;
    }

    /**
     * Updates the given table of the given database with the given values.
     * @param database the given database
     * @param table the given table
     * @param values the given values
     * @return the number of rows affected
     */
    public int update(SQLiteDatabase database, String table, @Nullable ContentValues values) {
        return database.update(table, values, _query_string.toString(), argumentsAsArray());
    }

    /**
     * Deletes from the given table of the given database.
     * @param database the given database
     * @param table the given table
     * @return the number of rows affected
     */
    public int delete(SQLiteDatabase database, String table) {
        return database.delete(table, _query_string.toString(), argumentsAsArray());
    }

    /**
     * <p>Retrieves list of {@link ActiveRecord}s that match query.</p>
     * @param uri the URI over which to query
     * @param sort_order the order to apply to the result space
     * @return the result
     */
    public <T extends ActiveRecord> List<T> select(Uri uri, @Nullable String sort_order) {
        final ContentProviderClient client = DataModule.getContentResolver().acquireContentProviderClient(uri);
        return ((BaseContentProvider) client.getLocalContentProvider()).selectRecords(uri, this, sort_order);
    }

    /**
     * <p>Retrieves list of {@link ActiveRecord}s that match query.</p>
     * @param uri the URI over which to query
     * @return the result
     */
    public <T extends ActiveRecord> List<T> select(Uri uri) {
        return select(uri, (String) null);
    }

    /**
     * <p>Retrieves the first {@link ActiveRecord} that matches query.</p>
     * @param uri the URI over which to query
     * @param sort_order the order to apply to the result space
     * @return The result
     */
    @CheckForNull public <T extends ActiveRecord> T selectFirst(Uri uri, @Nullable String sort_order) {
        final ContentProviderClient client = DataModule.getContentResolver().acquireContentProviderClient(uri);
        final List<T> records = ((BaseContentProvider) client.getLocalContentProvider()).selectRecords(uri, this, sort_order);
        return records.isEmpty()? null : records.get(0);
    }

    /**
     * <p>Retrieves the first {@link ActiveRecord} that matches query.</p>
     * @param uri the URI over which to query
     * @return The result
     */
    @CheckForNull public <T extends ActiveRecord> T selectFirst(Uri uri) {
        return selectFirst(uri, null);
    }

    /**
     * <p>Retrieves the cursor to the matches of the query.</p>
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param sort_order the order to apply to the result space
     * @return a {@link android.database.Cursor} to the result
     */
    public Cursor select(Uri uri, String[] projection, @Nullable String sort_order) {
        return DataModule.getContentResolver().query(uri, projection, toString(), argumentsAsArray(), sort_order);
    }

    /**
     * <p>Retrieves the cursor to the matches of the query.</p>
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param sort_order the order to apply to the result space
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return a {@link android.database.Cursor} to the result
     */
    public Cursor select(Uri uri, String[] projection, @Nullable String sort_order, boolean should_notify) {
        uri = uri.buildUpon().appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(should_notify)).build();
        return DataModule.getContentResolver().query(uri, projection, toString(), argumentsAsArray(), sort_order);
    }

    /**
     * <p>Retrieves the cursor to the matches of the query.</p>
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @return a {@link android.database.Cursor} to the result
     */
    public Cursor select(Uri uri, String[] projection) {
        return select(uri, projection, null);
    }

    /**
     * <p>Retrieves the cursor to the matches of the query.</p>
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return a {@link android.database.Cursor} to the result
     */
    public Cursor select(Uri uri, String[] projection, boolean should_notify) {
        return select(uri, projection, null, should_notify);
    }

    /**
     * <p>Constructs a {@link android.support.v4.content.CursorLoader} that retrieves the {@link android.database.Cursor} to the matches of the query.</p>
     * @param context the context in which to create the loader
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param sort_order the order to apply to the result space
     * @return a {@link android.support.v4.content.CursorLoader} to load the {@link android.database.Cursor}
     */
    public CursorLoader createLoader(Context context, Uri uri, String[] projection, String sort_order) {
        return new CursorLoader(context.getApplicationContext(), uri, projection, toString(), argumentsAsArray(), sort_order);
    }

    /**
     * <p>Constructs a {@link android.support.v4.content.CursorLoader} that retrieves the {@link android.database.Cursor} to the matches of the query.</p>
     * @param context the context in which to create the loader
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param sort_order the order to apply to the result space
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return a {@link android.support.v4.content.CursorLoader} to load the {@link android.database.Cursor}
     */
    public CursorLoader createLoader(Context context, Uri uri, String[] projection,
                                     @Nullable String sort_order, boolean should_notify) {
        uri = uri.buildUpon().appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(should_notify)).build();

        return new CursorLoader(context.getApplicationContext(), uri, projection, toString(), argumentsAsArray(), sort_order);
    }

    /**
     * <p>Constructs a {@link android.support.v4.content.CursorLoader} that retrieves the {@link android.database.Cursor} to the matches of the query.</p>
     * @param context the context in which to create the loader
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @return a {@link android.support.v4.content.CursorLoader} to load the {@link android.database.Cursor}
     */
    public CursorLoader createLoader(Context context, Uri uri, String[] projection) {
        return createLoader(context, uri, projection, null);
    }

    /**
     * <p>Constructs a {@link android.support.v4.content.CursorLoader} that retrieves the {@link android.database.Cursor} to the matches of the query.</p>
     * @param context the context in which to create the loader
     * @param uri the URI over which to query
     * @param projection the vector space on which to project the result
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return a {@link android.support.v4.content.CursorLoader} to load the {@link android.database.Cursor}
     */
    public CursorLoader createLoader(Context context, Uri uri, String[] projection,
                                     boolean should_notify) {
        return createLoader(context, uri, projection, null, should_notify);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the integer value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public int intFromFirstRow(Uri uri, String column) {
        return intFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the integer value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public int intFromFirstRow(Uri uri, String column, String order_by) {
        Cursor cursor = null;
        int value = 0;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getInt(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the long value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public long longFromFirstRow(Uri uri, String column) {
        return longFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the long value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public long longFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        long value = 0;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getLong(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException e) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the double value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public double doubleFromFirstRow(Uri uri, String column) {
        return doubleFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the double value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public double doubleFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        double value = 0;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getDouble(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the float value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public float floatFromFirstRow(Uri uri, String column) {
        return floatFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the integer value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public float floatFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        float value = 0;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getFloat(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the short value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public short shortFromFirstRow(Uri uri, String column) {
        return shortFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the short value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public short shortFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        short value = 0;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if(cursor.moveToFirst()) {
                value = cursor.getShort(0);
            }

        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the blob value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public byte[] blobFromFirstRow(Uri uri, String column) {
        return blobFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the integer value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    @CheckForNull public byte[] blobFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        byte[] value = null;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getBlob(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the boolean value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public boolean booleanFromFirstRow(Uri uri, String column) {
        return booleanFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the boolean value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public boolean booleanFromFirstRow(Uri uri, String column, String order_by) {
        return shortFromFirstRow(uri, column, order_by) > 0;
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the String value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @return the value of the given column in the first row of the result
     */
    public String stringFromFirstRow(Uri uri, String column) {
        return stringFromFirstRow(uri, column, null);
    }

    /**
     * Queries over the given {@link android.net.Uri URI} for the String value of the given column from the first row in the result.
     * @param uri the given URI
     * @param column the given column
     * @param order_by the order to apply to the result space
     * @return the value of the given column in the first row of the result
     */
    public String stringFromFirstRow(Uri uri, String column, @Nullable String order_by) {
        Cursor cursor = null;
        String value = null;

        try {
            cursor = select(uri, new String[] { column }, order_by, false);

            if (cursor.moveToFirst())
                value = cursor.getString(0);
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }

        return value;
    }

    /**
     * Updates content at the given {@link android.net.Uri URI} with the given values.
     * @param uri the given URI
     * @param values the given values
     * @return the number of rows affected
     */
    public int update(Uri uri, @Nullable ContentValues values) {
        return DataModule.getContentResolver().update(uri, values, toString(), argumentsAsArray());
    }

    /**
     * Updates content at the given {@link android.net.Uri URI} with the given values.
     * @param uri the given URI
     * @param values the given values
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return the number of rows affected
     */
    public int update(Uri uri, @Nullable ContentValues values, boolean should_notify) {
        uri = uri.buildUpon().appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(should_notify)).build();
        return DataModule.getContentResolver().update(uri, values, toString(), argumentsAsArray());
    }

    /**
     * Deletes content at the given {@link android.net.Uri URI}.
     * @param uri the given URI
     * @return the number of rows affected
     */
    public int delete(Uri uri) {
        return DataModule.getContentResolver().delete(uri, toString(), argumentsAsArray());
    }

    /**
     * Deletes content at the given {@link android.net.Uri URI}.
     * @param uri the given URI
     * @param should_notify indicates when the {@link android.content.ContentProvider} should notify observers when content is
     *                      modified
     * @return the number of rows affected
     */
    public int delete(Uri uri, boolean should_notify) {
        uri = uri.buildUpon().appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(should_notify)).build();
        return DataModule.getContentResolver().delete(uri, toString(), argumentsAsArray());
    }

    /**
     * Counts the number of matches at the given {@link android.net.Uri URI}.
     * @param uri the given URI
     * @return the number matching rows
     */
    public int count(Uri uri) {
        Cursor cursor = null;
        uri = uri.buildUpon().appendQueryParameter(BaseContentProvider.PARAM_SHOULD_NOTIFY, Boolean.toString(false)).build();

        try {
            cursor = DataModule.getContentResolver().query(uri, new String[] { "count(*)" }, toString(), argumentsAsArray(), null);

            int count = 0;

            if (cursor.moveToFirst())
                count = cursor.getInt(0);
            return count;
        } finally {
            //noinspection EmptyCatchBlock
            try {
                Closeables.close(cursor, true);
            } catch (IOException _) { }
        }
    }

    /**
     * Checks the existence of a match at the given {@link android.net.Uri URI}.
     * @param uri the given URI
     * @return {@code true} indicates that there is at least one match
     */
    public boolean exists(Uri uri) {
        return count(uri) > 0;
    }

    /**
     * Appends the next operator onto the query.
     */
    private void ensureOp() {
        if (StringUtilities.isEmpty(_query_string)) return;

        if(_next_operator == null)
            _query_string.append(AND);
        else {
            _query_string.append(_next_operator);
            _next_operator = null;
        }
    }

    /** The AND operator */
    private static final String AND = " AND ";
    /** The OR operator */
    private static final String OR = " OR ";
    private StringBuilder _query_string;
    private List<String> _arguments = newArrayList();
    private String _next_operator = null;
}
