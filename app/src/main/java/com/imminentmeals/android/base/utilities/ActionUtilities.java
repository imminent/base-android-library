package com.imminentmeals.android.base.utilities;

import android.view.Menu;
import android.view.MenuInflater;

import com.imminentmeals.android.base.R.id;
import com.imminentmeals.android.base.R.menu;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * <p>Utilities for generating the {@link android.app.ActionBar} actions.</p>
 * @author Dandre Allison
 */
@SuppressWarnings("UnusedDeclaration")
@ParametersAreNonnullByDefault
public final class ActionUtilities {

   /**
    * <p>Generates the base actions and amends the given actions, if more are provided.</p>
    * @param inflater Used to inflate the actions
    * @param actions Holds the inflated actions
    * @param resource Menu resource ID of the given actions if greater than {@code 0}
    */
   public static void generateActions(MenuInflater inflater, Menu actions, int resource) {
       inflater.inflate(_BASE_ACTIONS, actions);
       if (resource > 0) inflater.inflate(resource, actions);
   }

   /**
    * <p>Generates the base actions and amends the given actions, if more are provided.</p>
    * @param inflater Used to inflate the actions
    * @param actions Holds the inflated actions
    * @param resource Menu resource ID of the given actions if greater than {@code 0}
    */
   public static void generateActionsWithSyncStatus(MenuInflater inflater, Menu actions, int resource) {
       inflater.inflate(_BASE_ACTIONS, actions);
       actions.findItem(id.menu_refresh).setVisible(true);
       if (resource > 0) inflater.inflate(resource, actions);
   }

/* Private Constructor */
    /** Blocks instantiation of the {@link HelpUtilities} class. */
    private ActionUtilities() { }
    /** The base actions resource ID */
    private static final int _BASE_ACTIONS = menu.base;
}
