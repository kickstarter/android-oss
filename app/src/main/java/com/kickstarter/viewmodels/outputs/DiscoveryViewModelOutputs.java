package com.kickstarter.viewmodels.outputs;

import android.util.Pair;

import com.kickstarter.services.DiscoveryParams;
import com.kickstarter.ui.adapters.data.NavigationDrawerData;

import java.util.List;

import rx.Observable;

public interface DiscoveryViewModelOutputs {
  /**
   * Emits a boolean that determines if the drawer is open or not.
   */
  Observable<Boolean> drawerIsOpen();

  /**
   * Emits a booelan that determines if the sort tab layout should be expanded/collapsed.
   */
  Observable<Boolean> expandSortTabLayout();

  /**
   * Emits when params change so that the tool bar can adjust accordingly.
   */
  Observable<DiscoveryParams> updateToolbarWithParams();

  /**
   * Emits when the params of a particular page should be updated. The page will be responsible for
   * taking those params and creating paginating projects from it.
   */
  Observable<Pair<DiscoveryParams, Integer>> updateParamsForPage();

  Observable<NavigationDrawerData> navigationDrawerData();

  /**
   * Emits a list of pages that should be cleared of all their content.
   */
  Observable<List<Integer>> clearPages();

  Observable<Void> showLoginTout();
  Observable<Void> showProfile();
  Observable<Void> showSettings();
  Observable<Void> showInternalTools();
}
