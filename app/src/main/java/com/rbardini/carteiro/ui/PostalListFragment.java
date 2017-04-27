package com.rbardini.carteiro.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.nhaarman.listviewanimations.itemmanipulation.AnimateDismissAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.OnDismissCallback;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalItemRecord;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostalListFragment extends PostalFragment implements ContextualSwipeUndoAdapter.DeleteItemCallback, ContextualSwipeUndoAdapter.OnSwipeCallback, OnDismissCallback {
  public interface OnPostalListActionListener {
    void onPostalListAttached(PostalListFragment f);
  }

  private OnPostalListActionListener mListener;

  private int category;
  private String query;

  private MultiChoiceModeListener mMultiChoiceModeListener;
  private ArrayList<PostalItem> mList;
  private ArrayList<PostalItem> mSelectedList;
  private PostalItemListAdapter mListAdapter;
  private AnimateDismissAdapter mDismissAdapter;
  private ContextualSwipeUndoAdapter mUndoAdapter;

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
  public void onAttach(Context context) {
    super.onAttach(context);

    try {
      mListener = (OnPostalListActionListener) context;

    } catch (ClassCastException e) {
      throw new ClassCastException(context.toString() + " must implement OnPostalListActionListener");
    }
  }

  @Override @SuppressWarnings("unchecked")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

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
    return inflater.inflate(R.layout.list_postal, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    ListView listView = getListView();

    mListAdapter = new PostalItemListAdapter(mActivity, mList, app.getUpdatedCods(), listView);
    mMultiChoiceModeListener = new PostalListFragment.MultiChoiceModeListener();
    listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
    listView.setMultiChoiceModeListener(mMultiChoiceModeListener);

    mDismissAdapter = new AnimateDismissAdapter(mListAdapter, this);
    mDismissAdapter.setAbsListView(listView);

    mUndoAdapter = new ContextualSwipeUndoAdapter(mListAdapter, shouldDeleteItems() ? R.layout.undo_delete_row : R.layout.undo_archive_row, R.id.undo_button, this, this);
    mUndoAdapter.setAbsListView(listView);
    listView.setAdapter(mUndoAdapter);

    TextView emptyText = (TextView) listView.getEmptyView().findViewById(R.id.empty_text);
    if (query == null) {
      String title = getString(Category.getTitle(category));
      emptyText.setText(Html.fromHtml(getString(R.string.text_empty_list_name, title)));
    } else {
      emptyText.setText(R.string.text_empty_list_found);
    }

    if (CarteiroApplication.state.syncing) setRefreshing();

    mListener.onPostalListAttached(this);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (app.hasUpdate()) refreshList();
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
    Intent intent = new Intent(mActivity, RecordActivity.class).putExtra("postalItem", pi);
    startActivity(intent);
  }

  @Override
  public void deleteItem(int position) {
    PostalItemRecord pir = new PostalItemRecord(mList.get(position));

    if (shouldDeleteItems()) {
      pir.deleteFrom(dh);

    } else {
      pir.archiveTo(dh);
    }

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

  @Override
  public void onRefresh() {
    if (!CarteiroApplication.state.syncing) {
      Intent intent = new Intent(Intent.ACTION_SYNC, null, mActivity, SyncService.class);
      List<String> cods = new ArrayList<>();
      for (PostalItem pi : mList) {
        cods.add(pi.getCod());
      }
      intent.putExtra("cods", cods.toArray(new String[cods.size()]));
      mActivity.startService(intent);
    }
  }

  @Override
  public void refreshList() {
    mUndoAdapter.removePendingItem();
    updateList();
    mListAdapter.notifyDataSetChanged();

    // TODO Handle possible selection change while CAB is active
  }

  @Override
  public void clearSelection() {
    mMultiChoiceModeListener.finishActionMode();
  }

  public void setQuery(String query) {
    this.query = query;
  }

  public void setCategory(int category) {
    this.category = category;
  }

  public int getCategory() {
    return category;
  }

  public List<PostalItem> getList() {
    return mList;
  }

  public int getListSize() {
    return mList.size();
  }

  public boolean hasSelection() {
    return mMultiChoiceModeListener.hasActionMode();
  }

  public boolean shouldDeleteItems() {
    return (query != null) || (category == Category.ARCHIVED);
  }

  public void updateList() {
    if (query != null) dh.getSearchResults(mList, query);
    else dh.getPostalList(mList, category);
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

      // Force list view refresh to update checked state
      mListAdapter.notifyDataSetChanged();
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
        .setIcon(areAllFavorites ? R.drawable.ic_star_white_24dp : R.drawable.ic_star_border_white_24dp)
        .setTitle(areAllFavorites ? R.string.opt_unmark_as_fav : R.string.opt_mark_as_fav);
      mCollectiveActionMap.put(favAction.getItemId(), areAllFavorites);
      MenuItem archiveAction = menu.findItem(R.id.archive_opt)
        .setIcon(areAllArchived ? R.drawable.ic_unarchive_white_24dp : R.drawable.ic_archive_white_24dp)
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
            Intent refresh = new Intent(Intent.ACTION_SYNC, null, mActivity, SyncService.class).putExtra("cods", cods);
            mActivity.startService(refresh);
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
          UIUtils.showToast(mActivity, getString(messageRes, isSingleSelection ? firstItem.getSafeDesc() : selectionSize, getString(R.string.category_all)));

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
          Intent shareIntent = PostalUtils.getShareIntent(mActivity, mSelectedList);
          if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
          return true;
      }

      /* Single item actions */
      switch (actionId) {
        case R.id.place_opt:
          try {
            UIUtils.locateItem(mActivity, firstItem);
          } catch (Exception e) {
            UIUtils.showToast(mActivity, e.getMessage());
          }
          return true;

        case R.id.rename_opt:
          PostalItemDialogFragment.newInstance(R.id.rename_opt, mSelectedList).show(getFragmentManager(), PostalItemDialogFragment.TAG);
          return true;

        case R.id.websro_opt:
          Intent intent = new Intent(mActivity, RecordActivity.class).putExtra("postalItem", firstItem).setAction("webSRO");
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
