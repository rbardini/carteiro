package com.rbardini.carteiro.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.util.SparseBooleanArray;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.TextView;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncTask;
import com.rbardini.carteiro.ui.swipedismiss.SwipeDismissHandler;
import com.rbardini.carteiro.ui.swipedismiss.SwipeDismissListener;
import com.rbardini.carteiro.ui.swipedismiss.SwipeableRecyclerView;
import com.rbardini.carteiro.ui.transition.RoundIconTransition;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static com.rbardini.carteiro.ui.RecordActivity.ACTION_ARCHIVE;
import static com.rbardini.carteiro.ui.RecordActivity.ACTION_DELETE;

public class ShipmentListFragment extends ShipmentFragment implements SwipeDismissListener {
  interface OnPostalListActionListener {
    void onPostalListAttached(ShipmentListFragment f);
  }

  private static final String CATEGORY_KEY = "category";
  private static final String QUERY_KEY = "query";
  private static final String ACTION_KEY = "action";
  private static final String ACTION_SHIPMENT_KEY = "actionShipment";
  private static final String SHIPMENTS_KEY = "shipments";
  private static final String SELECTED_SHIPMENTS_KEY = "selectedShipments";

  private OnPostalListActionListener mListener;

  private int category;
  private String query;

  private MultiChoiceModeListener mMultiChoiceModeListener;
  private ArrayList<Shipment> mList;
  private ArrayList<Shipment> mSelectedList;
  private ShipmentListAdapter mListAdapter;
  private SwipeDismissHandler mSwipeDismissHandler;

  public static ShipmentListFragment newInstance(int category, String action, Shipment actionShipment) {
    ShipmentListFragment f = new ShipmentListFragment();
    Bundle args = new Bundle();
    args.putInt(CATEGORY_KEY, category);
    args.putString(ACTION_KEY, action);
    args.putSerializable(ACTION_SHIPMENT_KEY, actionShipment);
    f.setArguments(args);

    return f;
  }

  public static ShipmentListFragment newInstance(String query) {
    ShipmentListFragment f = new ShipmentListFragment();
    Bundle args = new Bundle();
    args.putString(QUERY_KEY, query);
    f.setArguments(args);

    return f;
  }

  @Override @SuppressWarnings("unchecked")
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    try {
      mListener = (OnPostalListActionListener) mActivity;
    } catch (ClassCastException e) {
      throw new ClassCastException(mActivity.toString() + " must implement OnPostalListActionListener");
    }

    Bundle arguments = getArguments();
    setCategory(arguments.getInt(CATEGORY_KEY));
    setQuery(arguments.getString(QUERY_KEY));

    // Restore postal and selected lists
    if (savedInstanceState != null) {
      mList = (ArrayList<Shipment>) savedInstanceState.getSerializable(SHIPMENTS_KEY);
      mSelectedList = (ArrayList<Shipment>) savedInstanceState.getSerializable(SELECTED_SHIPMENTS_KEY);

    } else {
      mList = new ArrayList<>();
      mSelectedList = new ArrayList<>();

      updateList();
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.list_shipment, container, false);
    LinearLayoutManager layoutManager = new LinearLayoutManager(mActivity);
    boolean shouldDeleteItems = shouldDeleteItems();

    View emptyView = view.findViewById(android.R.id.empty);
    TextView emptyText = emptyView.findViewById(R.id.empty_text);
    if (query == null) {
      String title = getString(Category.getTitle(category));
      emptyText.setText(Html.fromHtml(getString(R.string.text_empty_list_name, title)));
    } else {
      emptyText.setText(R.string.text_empty_list_found);
    }

    mListAdapter = new ShipmentListAdapter(mActivity, mList, mSelectedList, this);
    mSwipeDismissHandler = new SwipeDismissHandler(view, new Handler(),
        shouldDeleteItems ? R.string.toast_deleted_count_single : R.string.toast_archived_count_single,
        shouldDeleteItems ? R.string.toast_deleted_count_multiple : R.string.toast_archived_count_multiple,
        R.string.undo_btn, mListAdapter, this);
    mMultiChoiceModeListener = new ShipmentListFragment.MultiChoiceModeListener();

    if (!mSelectedList.isEmpty()) mActivity.startActionMode(mMultiChoiceModeListener);

    SwipeableRecyclerView recyclerView = view.findViewById(android.R.id.list);
    recyclerView.setLayoutManager(layoutManager);
    recyclerView.setAdapter(mListAdapter);
    recyclerView.setSwipeListener(mSwipeDismissHandler);
    recyclerView.setEmptyView(emptyView);
    recyclerView.setLeaveBehindColor(ContextCompat.getColor(mActivity, shouldDeleteItems ? R.color.error_background : R.color.success_background));
    recyclerView.setLeaveBehindPadding(getResources().getDimensionPixelSize(R.dimen.icon));
    recyclerView.setLeaveBehindIcon(shouldDeleteItems ? R.drawable.ic_delete_white_24dp : R.drawable.ic_archive_white_24dp);
    recyclerView.addItemDecoration(new InsetDividerItemDecoration(
      recyclerView.getContext(),
      layoutManager.getOrientation(),
      getResources().getDimensionPixelSize(R.dimen.keyline_3),
      0
    ));

    mListener.onPostalListAttached(this);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshList();

    final Bundle arguments = getArguments();
    final String action = arguments.getString(ACTION_KEY);
    final Shipment actionShipment = (Shipment) arguments.getSerializable(ACTION_SHIPMENT_KEY);

    arguments.remove(ACTION_KEY);
    arguments.remove(ACTION_SHIPMENT_KEY);

    if (action == null || actionShipment == null) return;

    switch (action) {
      case ACTION_ARCHIVE:
        archiveShipment(actionShipment);
        break;

      case ACTION_DELETE:
        deleteShipment(actionShipment);
        break;
    }
  }

  @Override
  public void onPause() {
    mSwipeDismissHandler.finish();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable(SHIPMENTS_KEY, mList);
    outState.putSerializable(SELECTED_SHIPMENTS_KEY, mSelectedList);
  }

  @Override
  public void onRefresh() {
    mSwipeDismissHandler.finish();

    if (CarteiroApplication.syncing || mList.isEmpty()) {
      updateRefreshStatus();

    } else {
      SyncTask.run(app, mList);
    }
  }

  @Override
  public void refreshList() {
    mSwipeDismissHandler.finish();

    updateList();
    mListAdapter.notifyDataSetChanged();

    // TODO Handle possible selection change while CAB is active
  }

  @Override
  public void clearSelection() {
    mMultiChoiceModeListener.finishActionMode();
  }

  @Override
  public void onItemClicked(@NonNull Object item) {
    RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) item;
    Shipment shipment = mListAdapter.getItem(viewHolder.getAdapterPosition());

    if (mMultiChoiceModeListener.hasActionMode()) {
      onItemSelected(item);
      return;
    }

    View icon = viewHolder.itemView.findViewById(R.id.img_postal_status);
    Intent intent = new Intent(mActivity, RecordActivity.class).putExtra(RecordActivity.EXTRA_SHIPMENT, shipment);

    RoundIconTransition.addExtras(intent, (int) icon.getTag(R.id.shipment_color), (int) icon.getTag(R.id.shipment_icon), 2);
    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(mActivity, icon, getString(R.string.transition_record));

    startActivity(intent, options.toBundle());
  }

  @Override
  public void onItemSelected(@NonNull Object item) {
    RecyclerView.ViewHolder viewHolder = (RecyclerView.ViewHolder) item;
    Shipment shipment = mListAdapter.getItem(viewHolder.getAdapterPosition());

    selectShipment(shipment);
  }

  @Override
  public void onItemsDismissed(@NonNull Map<Integer, Object> items) {
    List<Shipment> shipments = new ArrayList<>();

    for (Map.Entry<Integer, Object> entry : items.entrySet()) {
      shipments.add((Shipment) entry.getValue());
    }

    if (shouldDeleteItems()) {
      dh.deleteShipments(shipments);

    } else {
      dh.archiveShipments(shipments);
    }
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

  public List<Shipment> getList() {
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
    else dh.getShallowShipments(mList, category);
  }

  private void selectShipment(Shipment shipment) {
    if (!mMultiChoiceModeListener.hasActionMode()) {
      mActivity.startActionMode(mMultiChoiceModeListener);
    }

    mMultiChoiceModeListener.toggleItemCheckedState(shipment);
  }

  private void archiveShipment(Shipment shipment) {
    selectShipment(shipment);
    mMultiChoiceModeListener.archiveItems(false);
  }

  private void deleteShipment(Shipment shipment) {
    selectShipment(shipment);
    mMultiChoiceModeListener.deleteItems();
  }

  private List<Shipment> buildShipmentListFromMap(Map<Integer, Object> items) {
    List<Shipment> shipments = new ArrayList<>();

    for (Map.Entry<Integer, Object> entry : items.entrySet()) {
      shipments.add((Shipment) entry.getValue());
    }

    return shipments;
  }

  private final class MultiChoiceModeListener implements AbsListView.MultiChoiceModeListener {
    private ActionMode mActionMode;
    private SparseBooleanArray mCollectiveActionMap;

    @Override
    public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
      Shipment shipment = mList.get(position);
      setItemCheckedState(shipment, checked);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
      mActionMode = mode;
      mCollectiveActionMap = new SparseBooleanArray();

      mActionMode.getMenuInflater().inflate(R.menu.postal_list_actions, menu);

      return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
      final int selectionSize = mSelectedList.size();
      final boolean isSingleSelection = selectionSize == 1;
      final boolean areAllSelected = selectionSize == mList.size();

      // Show or hide actions depending on selection size
      menu.findItem(R.id.place_opt).setVisible(isSingleSelection);
      menu.findItem(R.id.rename_opt).setVisible(isSingleSelection);
      menu.findItem(R.id.sro_opt).setVisible(isSingleSelection);

      // Disable refresh action if sync is in progress
      if (CarteiroApplication.syncing) {
        menu.findItem(R.id.refresh_opt).setEnabled(false);
      }

      // Determine if all selected items are favorites and archived
      boolean areAllFavorites = true, areAllArchived = true;
      for (Shipment shipment : mSelectedList) {
        if (!shipment.isFavorite()) areAllFavorites = false;
        if (!shipment.isArchived()) areAllArchived = false;
        if (!areAllFavorites && !areAllArchived) break;
      }

      // Update select all, favorite and archive actions depending on the selected items
      MenuItem selectAllAction = menu.findItem(R.id.select_all_opt)
        .setIcon(areAllSelected ? R.drawable.ic_deselect_all_white_24dp : R.drawable.ic_select_all_white_24dp)
        .setTitle(areAllSelected ? R.string.opt_deselect_all : R.string.opt_select_all);
      mCollectiveActionMap.put(selectAllAction.getItemId(), areAllSelected);
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
      final int actionId = item.getItemId();

      // Multiple items actions
      switch (actionId) {
        case R.id.select_all_opt:
          final boolean areAllSelected = mCollectiveActionMap.get(actionId);
          toggleAllItemsCheckedState(!areAllSelected);
          return true;

        case R.id.refresh_opt:
          if (!CarteiroApplication.syncing) {
            SyncTask.run(app, mSelectedList);
          }
          return true;

        case R.id.archive_opt:
          // Archive selected items or move to all depending on their collective archived state
          final boolean areAllArchived = mCollectiveActionMap.get(actionId);
          archiveItems(areAllArchived);
          return true;

        case R.id.delete_opt:
          deleteItems();
          return true;

        case R.id.fav_opt:
          // Mark or unmark selected items as favorites depending on their collective favorite state
          final boolean areAllFavorites = mCollectiveActionMap.get(actionId);
          favoriteItems(areAllFavorites);
          return true;

        case R.id.share_opt:
          Intent shareIntent = PostalUtils.getShareIntent(mActivity, mSelectedList);
          if (shareIntent != null) startActivity(Intent.createChooser(shareIntent, getString(R.string.title_send_list)));
          return true;
      }

      // Single item actions
      switch (actionId) {
        case R.id.place_opt:
          try {
            UIUtils.locateItem(mActivity, mSelectedList.get(0));
          } catch (Exception e) {
            UIUtils.showToast(mActivity, e.getMessage());
          }
          return true;

        case R.id.rename_opt:
          ShipmentRenameDialogFragment
            .newInstance(mSelectedList.get(0))
            .show(getFragmentManager(), ShipmentRenameDialogFragment.TAG);
          return true;

        case R.id.sro_opt:
          Intent intent = new Intent(mActivity, RecordActivity.class)
            .putExtra(RecordActivity.EXTRA_SHIPMENT, mSelectedList.get(0))
            .setAction(RecordActivity.ACTION_SRO);
          startActivity(intent);
          return true;
      }

      return false;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
      mActionMode = null;

      mCollectiveActionMap.clear();
      mSelectedList.clear();
      mListAdapter.notifyDataSetChanged();
    }

    public void setItemCheckedState(Shipment shipment, boolean checked) {
      if (checked) {
        mSelectedList.add(shipment);

      } else {
        mSelectedList.remove(shipment);
      }

      if (mSelectedList.isEmpty()) {
        finishActionMode();

      } else {
        mActionMode.invalidate();
        mListAdapter.notifyDataSetChanged();
      }
    }

    public void toggleItemCheckedState(Shipment shipment) {
      setItemCheckedState(shipment, !mSelectedList.contains(shipment));
    }

    public void toggleAllItemsCheckedState(boolean checked) {
      if (!checked) {
        finishActionMode();
        return;
      }

      mSelectedList.clear();
      mSelectedList.addAll(mList);

      mActionMode.invalidate();
      mListAdapter.notifyDataSetChanged();
    }

    public boolean hasActionMode() {
      return mActionMode != null;
    }

    public void finishActionMode() {
      if (this.hasActionMode()) mActionMode.finish();
    }

    public void archiveItems(final boolean areAllArchived) {
      new SwipeDismissHandler(getView(), new Handler(),
          areAllArchived ? R.string.toast_unarchived_count_single : R.string.toast_archived_count_single,
          areAllArchived ? R.string.toast_unarchived_count_multiple : R.string.toast_archived_count_multiple,
          R.string.undo_btn, mListAdapter, new SwipeDismissListener() {
        @Override
        public void onItemClicked(@NonNull Object item) {}

        @Override
        public void onItemSelected(@NonNull Object item) {}

        @Override
        public void onItemsDismissed(@NonNull Map<Integer, Object> items) {
          List<Shipment> shipments = buildShipmentListFromMap(items);
          if (areAllArchived) dh.unarchiveShipments(shipments); else dh.archiveShipments(shipments);
        }
      }).dismiss(mListAdapter.getItemPositions(mSelectedList));
      clearSelection();
    }

    public void deleteItems() {
      new SwipeDismissHandler(getView(), new Handler(),
          R.string.toast_deleted_count_single, R.string.toast_deleted_count_multiple,
          R.string.undo_btn, mListAdapter, new SwipeDismissListener() {
        @Override
        public void onItemClicked(@NonNull Object item) {}

        @Override
        public void onItemSelected(@NonNull Object item) {}

        @Override
        public void onItemsDismissed(@NonNull Map<Integer, Object> items) {
          List<Shipment> shipments = buildShipmentListFromMap(items);
          dh.deleteShipments(shipments);
        }
      }).dismiss(mListAdapter.getItemPositions(mSelectedList));
      clearSelection();
    }

    public void favoriteItems(final boolean areAllFavorites) {
      for (Shipment shipment : mSelectedList) {
        String cod = shipment.getNumber();
        if (areAllFavorites) dh.unfavPostalItem(cod);
        else dh.favPostalItem(cod);
        shipment.setFavorite(!areAllFavorites);
      }

      // Update item list and available actions
      mListAdapter.notifyDataSetChanged();
      mActionMode.invalidate();
    }
  }
}
