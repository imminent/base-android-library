package com.imminentmeals.android.base.data.provider;

/**
 * <p>The base {@link android.content.ContentProvider} API contract that exposes the interface to
 * a client that wants to interact with the content.</p>
 * @author Dandré Allison
 */
public final class BaseContract {
    /** URI authority for the ImminentMeals app's content */
    public static final String CONTENT_AUTHORITY = "com.imminentmeals.android.base";

/* Private Constructor */
    /** Blocks instantiation of the {@link BaseContract} class. */
    private BaseContract() {}
}
