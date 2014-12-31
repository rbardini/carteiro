package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.AnimateDismissAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostalListFragment extends ListFragment implements ContextualSwipeUndoAdapter.DeleteItemCallback, ContextualSwipeUndoAdapter.OnSwipeCallback, OnDismissCallback {
  public interface OnPostalListActionListener {
    public void onPostalListAttached(PostalListFragment f);
  }

  private OnPostalListActionListener mListener;

  private CarteiroApplication app;
  private Activity activity;
  private DatabaseHelper dh;

  private int category;
  private String query;

  private MultiChoiceModeListener mMultiChoiceModeListener;
  private ArrayList<PostalItem> mList;
  private ArrayList<PostalItem> mSelectedList;
  private PostalItemListAdapter mListAdapter;
  private AnimateDismissAdapter mDismissAdapter;
  private ContextualSwipeUndoAdapter mUndoAdapter;
  private SwipeRefreshLayout mSwipeRefreshLayout;

  public static PostalListFragment newInstance(int category) {
    PostalListFragment f = new PostalListFragment();
    Bundle args = new Bundle();
    args.putInt("category", category);
    f.setArguments(args);

    return f;
  }

  public static PostalListFragment newInstance(String query) {
    PostalListFragment f = new PostalListFragment();
    Bundle args = new Bundle();
    args.putString("query", query);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      mListener = (OnPostalListActionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnPostalListActionListener");
    }
  }

  @Override @SuppressWarnings("unchecked")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = getActivity();
    app = (CarteiroApplication) activity.getApplication();
    dh = app.getDatabaseHelper();

    Bundle arguments = getArguments();
    setCategory(arguments.getInt("category"));
    setQuery(arguments.getString("query"));

    // Restore postal and selected lists
    if (savedInstanceState != null) {
      mList = (ArrayList<PostalItem>) savedInstanceState.getSerializable("postalList");
      mSelectedList = (ArrayList<PostalItem>) savedInstanceState.getSerializable("selectedList");

    } else {
      mList = new ArrayList<>();
      mSelectedList = new ArrayList<>();

      updateList();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.postal_list, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mListAdapter = new PostalItemListAdapter(activity, mList, app.getUpdatedCods());
    mMultiChoiceModeListener = new PostalListFragment.MultiChoiceModeListener();
    getListView().setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    getListView().setMultiChoiceModeListener(mMultiChoiceModeListener);

    mDismissAdapter = new AnimateDismissAdapter(mListAdapter, this);
    mDismissAdapter.setAbsListView(getListView());

    mUndoAdapter = new ContextualSwipeUndoAdapter(mListAdapter, shouldDeleteItems() ? R.layout.undo_delete_row : R.layout.undo_archive_row, R.id.undo_button, this, this);
    mUndoAdapter.setAbsListView(getListView());
    getListView().setAdapter(mUndoAdapter);

    mSwipeRefreshLayout = (SwipeRefreshLayout) getView().findViewById(R.id.swipe_layout);
    mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        onRefreshStarted();
      }
    });
    mSwipeRefreshLayout.setColorSchemeResources(
      R.color.theme_accent,
      R.color.theme_primary_light,
      R.color.theme_accent,
      R.color.theme_primary_dark
    );

    if (CarteiroApplication.state.syncing) setRefreshing();

    mListener.onPostalListAttached(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (app.hasUpdate()) refreshList(false);
  }

  @Override
  public void onPause() {
    super.onPause();
    mUndoAdapter.animateRemovePendingItem();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("postalList", mList);
    outState.putSerializable("selectedList", mSelectedList);
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    PostalItem pi = mList.get(position);
    Intent intent = new Intent(activity, RecordActivity.class).putExtra("postalItem", pi);
    startActivity(intent);
  }

  @Override
  public void deleteItem(int position) {
    PostalItem pi = mList.get(position);

    if (shouldDeleteItems()) dh.deletePostalItem(pi.getCod());
    else dh.archivePostalItem(pi.getCod());

    mListAdapter.remove(position);
    app.setUpdatedList();
  }

  @Override
  public void onSwipe(int position) {
    clearSelection();
  }

  @Override
  public void onDismiss(AbsListView listView, int[] reverseSortedPositions) {
    for (int position : reverseSortedPositions) mListAdapter.remove(position);
    app.setUpdatedList();
  }

  public boolean hasSelection() {
    return mMultiChoiceModeListener.hasActionMode();
  }

  public void clearSelection() {
    mMultiChoiceModeListener.finishActionMode();
  }

  public void updateList() {
    if (query != null) dh.getSearchResults(mList, query);
    else dh.getPostalList(mList, category);
  }

  public List<PostalItem> getList() { return mList; }
  public int getListSize() { return mList.size(); }
  public void setCategory(int category) { this.category = category; }
  public void setQuery(String query) { this.query = query; }
  public int getCategory() { return category; }
  public String getQuery() { return query; }
  public boolean shouldDeleteItems() { return (query != null) || (category == Category.ARCHIVED); }

  public void onRefreshStarted() {
    if (!CarteiroApplication.state.syncing) {
      Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
      List<String> cods = new ArrayList<>();
      for (PostalItem pi : mList) {
        cods.add(pi.getCod());
      }
      intent.putExtra("cods", cods.toArray(new String[cods.size()]));
      activity.startService(intent);
    }
  }
  public void setRefreshing() { mSwipeRefreshLayout.setRefreshing(true); }
  public void onRefreshComplete() { mSwipeRefreshLayout.setRefreshing(false); }

  public void refreshList(boolean propagate) {
    mUndoAdapter.removePendingItem();
    updateList();
    mListAdapter.notifyDataSetChanged();

    if (propagate) {
      if (activity instanceof MainActivity) {
        ((MainActivity) activity).refreshList();
      } else {
        app.setUpdatedList();
      }
    }

    // TODO Handle possible selection change while CAB is active
  }


  private final class MultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    private ActionMode mActionMode;
    private Map<Integer, Boolean> mCollectiveActionMap;

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
      PostalItem pi = mListAdapter.getItem(position);

      // Add or remove item from selected list
      if (checked) mSelectedList.add(pi);
      else mSelectedList.remove(pi);

      // Invalidate CAB to refresh available actions
      mActionMode.invalidate();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      mActionMode = mode;
      mCollectiveActionMap = new HashMap<>();

      mActionMode.getMenuInflater().inflate(R.menu.postal_list_context, menu);

      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      final int selectionSize = mSelectedList.size();
      final boolean isSingleSelection = selectionSize == 1;

      // Show or hide actions depending on selection size
      menu.findItem(R.id.place_opt).setVisible(isSingleSelection);
      menu.findItem(R.id.rename_opt).setVisible(isSingleSelection);
      menu.findItem(R.id.websro_opt).setVisible(isSingleSelection);

      // Disable refresh action if sync is in progress
      if (CarteiroApplication.state.syncing) {
        menu.findItem(R.id.refresh_opt).setEnabled(false);
      }

      // Determine if all selected items are favorites and archived
      boolean areAllFavorites = true, areAllArchived = true;
      for (PostalItem pi : mSelectedList) {
        if (!pi.isFav()) areAllFavorites = false;
        if (!pi.isArchived()) areAllArchived = false;
        if (!areAllFavorites && !areAllArchived) break;
      }

      // Update favorite and archive actions depending on the selected items
      MenuItem favAction = menu.findItem(R.id.fav_opt)
        .setIcon(areAllFavorites ? R.drawable.ic_menu_star_on : R.drawable.ic_menu_star_off)
        .setTitle(areAllFavorites ? R.string.opt_unmark_as_fav : R.string.opt_mark_as_fav);
      mCollectiveActionMap.put(favAction.getItemId(), areAllFavorites);
      MenuItem archiveAction = menu.findItem(R.id.archive_opt)
        .setIcon(areAllArchived ? R.drawable.ic_menu_unarchive : R.drawable.ic_menu_archive)
        .setTitle(getString(areAllArchived ? R.string.opt_unarchive_item : R.string.opt_archive_item, getString(R.string.category_all)));
      mCollectiveActionMap.put(archiveAction.getItemId(), areAllArchived);

      // Update CAB title with selection size if there is any
      if (selectionSize > 0) mActionMode.setTitle(String.valueOf(mSelectedList.size()));

      return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
      final PostalItem firstItem = mSelectedList.get(0);
      final int selectionSize = mSelectedList.size();
      final boolean isSingleSelection = selectionSize == 1;
      final int actionId = item.getItemId();

      /* Multiple items actions */
      switch (actionId) {
        case R.id.refresh_opt:
          if (!CarteiroApplication.state.syncing) {
            String[] cods = new String[selectionSize];
            for (int i = 0; i < selectionSize; i++) cods[i] = mSelectedList.get(i).getCod();
            Intent refresh = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class).putExtra("cods", cods);
            activity.startService(refresh);
          }
          return true;

        case R.id.archive_opt:
          // Archive selected items or move to all depending on their collective archived state
          boolean areAllArchived = mCollectiveActionMap.get(actionId);
          for (PostalItem pi : mSelectedList) {
            String cod = pi.getCod();
            if (areAllArchived) dh.unarchivePostalItem(cod);
            else dh.archivePostalItem(cod);
          }

          // Get the selected item positions and animate out of view
          SparseBooleanArray checkedItemPositions = getListView().getCheckedItemPositions();
          List<Integer> positionsToDismiss = new ArrayList<>();
          try {
            for (int i=0, length=getListView().getCount(); i<length; i++) {
              if (checkedItemPositions.get(i)) positionsToDismiss.add(i);
            }
          } catch (IndexOutOfBoundsException e) {}
          mDismissAdapter.animateDismiss(positionsToDismiss);

          int messageRes = isSingleSelection
            ? (areAllArchived ? R.string.toast_item_unarchived : R.string.toast_item_archived)
            : (areAllArchived ? R.string.toast_items_unarchived : R.string.toast_items_archived);
          UIUtils.showToast(activity, getString(messageRes, isSingleSelection ? firstItem.getSafeDesc() : selectionSize, getString(R.string.category_all)));

          clearSelection();
          return true;

        case R.id.delete_opt:
          PostalItemDialogFragment.newInstance(R.id.delete_opt, mSelectedList).show(getFragmentManager(), PostalItemDialogFragment.TAG);
          return true;

        case R.id.fav_opt:
          // Mark or unmark selected items as favorites depending on their collective favorite state
          boolean areAllFavorites = mCollectiveActionMap.get(actionId);
          for (PostalItem pi : mSelectedList) {
            String cod = pi.getCod();
            if (areAllFavorites) dh.unfavPostalItem(cod);
            else dh.favPostalItem(cod);
            pi.setFav(!areAllFavorites);
          }

          // Update item list and available actions
          mListAdapter.notifyDataSetChanged();
          mActionMode.invalidate();

          app.setUpdatedList();
          return true;

        case R.id.share_opt:
          Intent shareIntent = PostalUtils.getShareIntent(activity, mSelectedList);
          if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
          return true;
      }

      /* Single item actions */
      switch (actionId) {
        case R.id.place_opt:
          try {
            UIUtils.locateItem(activity, firstItem);
          } catch (Exception e) {
            UIUtils.showToast(activity, e.getMessage());
          }
          return true;

        case R.id.rename_opt:
          PostalItemDialogFragment.newInstance(R.id.rename_opt, mSelectedList).show(getFragmentManager(), PostalItemDialogFragment.TAG);
          return true;

        case R.id.websro_opt:
          Intent intent = new Intent(activity, RecordActivity.class).putExtra("postalItem", firstItem).setAction("webSRO");
          startActivity(intent);
          return true;
      }

      return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      mCollectiveActionMap.clear();
      mSelectedList.clear();
      mActionMode = null;
    }

    public boolean hasActionMode() {
      return mActionMode != null;
    }

    public void finishActionMode() {
      if (this.hasActionMode()) mActionMode.finish();
    }
  }
}
