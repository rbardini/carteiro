package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter;
import com.nhaarman.listviewanimations.itemmanipulation.swipedismiss.contextualundo.ContextualUndoAdapter.DeleteItemCallback;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

import java.util.ArrayList;
import java.util.List;

public class PostalListFragment extends ListFragment implements DeleteItemCallback {
  private CarteiroApplication app;
  private Activity activity;
  private Handler handler;
  private DatabaseHelper dh;

  private static PostalItem pi;
  private int category;
  private String query;

  private List<PostalItem> mList;
  private PostalItemListAdapter mListAdapter;
  private ContextualUndoAdapter mUndoAdapter;
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
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    activity = getActivity();
    app = (CarteiroApplication) activity.getApplication();
    dh = app.getDatabaseHelper();

    pi = null;
    Bundle arguments = getArguments();
    setCategory(arguments.getInt("category"));
    setQuery(arguments.getString("query"));

    mList = new ArrayList<PostalItem>();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.postal_list, container, false);
    updateList();

    if (activity instanceof MainActivity) {
      activity.setTitle(Category.getTitle(category));
      ((MainActivity) activity).setDrawerCategoryChecked(category);
    }

    return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    mListAdapter = new PostalItemListAdapter(activity, mList, app.getUpdatedCods());
    registerForContextMenu(getListView());

    mUndoAdapter = new ContextualUndoAdapter(mListAdapter, shouldDeleteItems() ? R.layout.undo_delete_row : R.layout.undo_archive_row, R.id.undo_button, this);
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
  }

  @Override
  public void onPause() {
    super.onPause();
    mUndoAdapter.animateRemovePendingItem();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    pi = mList.get(position);
    Intent intent = new Intent(activity, RecordActivity.class).putExtra("postalItem", pi);
    startActivity(intent);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    activity.getMenuInflater().inflate(R.menu.postal_list_context, menu);

    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    pi = mList.get(info.position);

    menu.setHeaderTitle(pi.getSafeDesc());
    if (CarteiroApplication.state.syncing) {
      menu.findItem(R.id.refresh_opt).setEnabled(false);
    }
    menu.findItem(R.id.fav_opt).setTitle(getString(R.string.opt_toggle_fav, getString(pi.isFav() ? R.string.label_unmark_as : R.string.label_mark_as)));
    menu.findItem(R.id.archive_opt).setTitle(pi.isArchived() ? getString(R.string.opt_unarchive_item, getString(R.string.category_all))
                                                             : getString(R.string.opt_archive_item));
  }

  @Override
  public boolean onContextItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.refresh_opt:
        if (!CarteiroApplication.state.syncing) {
          Intent refresh = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class).putExtra("cods", new String[] {pi.getCod()});
          activity.startService(refresh);
        }
        return true;

      case R.id.rename_opt:
        PostalItemDialogFragment.newInstance(R.id.rename_opt, pi).show(getFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      case R.id.fav_opt:
        dh.togglePostalItemFav(pi.getCod());
        refreshList(true);
        return true;

      case R.id.place_opt:
        try {
          UIUtils.locateItem(activity, pi);
        } catch (Exception e) {
          UIUtils.showToast(activity, e.getMessage());
        }
        return true;

      case R.id.share_opt:
        UIUtils.shareItem(activity, pi);
        return true;

      case R.id.websro_opt:
        Intent intent = new Intent(activity, RecordActivity.class).putExtra("postalItem", pi).setAction("webSRO");
        startActivity(intent);
        return true;

      case R.id.archive_opt:
        dh.togglePostalItemArchived(pi.getCod());
        refreshList(true);
        UIUtils.showToast(activity, pi.toggleArchived() ? getString(R.string.toast_item_archived, pi.getSafeDesc())
                                                        : getString(R.string.toast_item_unarchived, pi.getSafeDesc(), getString(R.string.category_all)));
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, pi).show(getFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  @Override
  public void deleteItem(int position) {
    PostalItem pi = mList.get(position);

    if (shouldDeleteItems()) dh.deletePostalItem(pi.getCod());
    else dh.archivePostalItem(pi.getCod());

    mListAdapter.remove(position);
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
      List<String> cods = new ArrayList<String>();
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
  }
}
