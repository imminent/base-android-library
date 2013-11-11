package com.imminentmeals.android.base.utilities.database;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import javax.annotation.CheckForNull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author Dandré Allison
 */
@ParametersAreNonnullByDefault
public class CursorActiveRecordIterator<T extends ActiveRecord> implements Iterator<T>, Closeable {

    public CursorActiveRecordIterator(Cursor cursor, ActiveRecordFactory<T> active_record_factory) {
        _cursor = cursor;
        _active_record_factory = active_record_factory;
        _next_record = null;
        _is_closed = cursor.isClosed();
    }

/* Iterator contract */
    @Override
    public boolean hasNext() {
        if (_is_closed) throw new IllegalStateException("Calling hasNext() when CursorActiveRecordIterator is closed.");
        fillActiveRecordIfAvailable();
        return _next_record != null;
    }

    @Override
    public T next() {
        if (_is_closed) throw new IllegalStateException("Calling next() when CursorActiveRecordIterator is closed.");
        if (!hasNext())
            throw new IllegalStateException("Calling next() when CursorActiveRecordIterator doesn't have a next ActiveRecord.");

        // Nulls the next record after returning it
        try {
            return _next_record;
        } finally {
            _next_record = null;
        }
    }

    @Override
    public void remove() { }

/* Closeable contract */
    @Override
    public void close() throws IOException {
        if (_is_closed) throw new IllegalStateException("Closing an already closed CursorActiveRecordIterator.");
        _is_closed = true;
        _cursor.close();
    }

/* Private helper methods */
    /**
     * <p>Creates a new {@link ActiveRecord} from the current cursor position. The implementation of
     * {@code newActiveRecordFromCursor(android.database.Cursor)} is not allowed to change the position
     * of the cursor.</p>
     * @param cursor the given cursor
     * @return the {@link ActiveRecord} from the given cursor position, or {@code null} if one change be
     * created
     */
    @CheckForNull private T newActiveRecordFromCursor(Cursor cursor) {
        return _active_record_factory.create(cursor);
    }

    /**
     * <p>If there are entries left in the cursor then advance the cursor and use the new row to
     * populate {@link #_next_record}.</p>
     */
    private void fillActiveRecordIfAvailable() {
        // Loops until an ActiveRecord is created or the end of the cursor is reached
        while (_next_record == null && _cursor.moveToNext())
            _next_record = newActiveRecordFromCursor(_cursor);
    }

    /** Cursor over which to iterate */
    private final Cursor _cursor;
    /** Produces new {@link ActiveRecord}s of the appropriate type */
    private final ActiveRecordFactory<T> _active_record_factory;
    /** The next {@link ActiveRecord} to return */
    private volatile T _next_record;
    /** Flag invalidating use of the {@link com.imminentmeals.android.base.utilities.database.CursorActiveRecordIterator} */
    private volatile boolean _is_closed;
}
