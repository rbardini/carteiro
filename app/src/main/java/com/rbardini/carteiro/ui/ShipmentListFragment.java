package com.rbardini.carteiro.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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

public class ShipmentListFragment extends ShipmentFragment implements SwipeDismissListener {
  interface OnPostalListActionListener {
    void onPostalListAttached(ShipmentListFragment f);
  }

  private OnPostalListActionListener mListener;

  private int category;
  private String query;

  private MultiChoiceModeListener mMultiChoiceModeListener;
  private ArrayList<Shipment> mList;
  private ArrayList<Shipment> mSelectedList;
  private ShipmentListAdapter mListAdapter;
  private SwipeDismissHandler mSwipeDismissHandler;

  public static ShipmentListFragment newInstance(int category) {
    ShipmentListFragment f = new ShipmentListFragment();
    Bundle args = new Bundle();
    args.putInt("category", category);
    f.setArguments(args);

    return f;
  }

  public static ShipmentListFragment newInstance(String query) {
    ShipmentListFragment f = new ShipmentListFragment();
    Bundle args = new Bundle();
    args.putString("query", query);
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
    setCategory(arguments.getInt("category"));
    setQuery(arguments.getString("query"));

    // Restore postal and selected lists
    if (savedInstanceState != null) {
      mList = (ArrayList<Shipment>) savedInstanceState.getSerializable("shipments");
      mSelectedList = (ArrayList<Shipment>) savedInstanceState.getSerializable("selectedShipments");

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
    recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), layoutManager.getOrientation()));

    mListener.onPostalListAttached(this);

    return view;
  }

  @Override
  public void onResume() {
    super.onResume();
    refreshList();
  }

  @Override
  public void onPause() {
    mSwipeDismissHandler.finish();
    super.onPause();
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putSerializable("shipments", mList);
    outState.putSerializable("selectedShipments", mSelectedList);
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

    if (!mMultiChoiceModeListener.hasActionMode()) {
      mActivity.startActionMode(mMultiChoiceModeListener);
    }

    mMultiChoiceModeListener.toggleItemCheckedState(shipment);
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

      mActionMode.getMenuInflater().inflate(R.menu.postal_list_context, menu);

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

          return true;

        case R.id.delete_opt:
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

          return true;

        case R.id.fav_opt:
          // Mark or unmark selected items as favorites depending on their collective favorite state
          boolean areAllFavorites = mCollectiveActionMap.get(actionId);

          for (Shipment shipment : mSelectedList) {
            String cod = shipment.getNumber();
            if (areAllFavorites) dh.unfavPostalItem(cod);
            else dh.favPostalItem(cod);
            shipment.setFavorite(!areAllFavorites);
          }

          // Update item list and available actions
          mListAdapter.notifyDataSetChanged();
          mActionMode.invalidate();

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
          ShipmentDialogFragment
            .newInstance(R.id.rename_opt, mSelectedList)
            .show(getFragmentManager(), ShipmentDialogFragment.TAG);
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
  }
}
