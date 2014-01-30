package com.rbardini.carteiro.ui;

import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.text.format.DateUtils;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.UIUtils;

public class PostalListFragment extends ListFragment {
  private CarteiroApplication app;
  private FragmentActivity activity;
  private Handler handler;
  private DatabaseHelper dh;

  private static PostalItem pi;
  private int category;
  private String query;

  private List<PostalItem> list;
  private PostalItemListAdapter listAdapter;
  private PullToRefreshListView listView;

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

    list = new ArrayList<PostalItem>();
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

    listAdapter = new PostalItemListAdapter(activity, list, app.getUpdatedCods());
    setListAdapter(listAdapter);
    listView = (PullToRefreshListView) getView().findViewById(R.id.pull_to_refresh_listview);
    registerForContextMenu(listView.getRefreshableView());
    listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
      @Override
      public void onRefresh(PullToRefreshBase<ListView> refreshView) {
        if (!CarteiroApplication.state.syncing) {
          Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
          List<String> cods = new ArrayList<String>();
          for (PostalItem pi : list) {
            cods.add(pi.getCod());
          }
          intent.putExtra("cods", cods.toArray(new String[] {}));
          activity.startService(intent);
        }
      }
    });

    handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        listAdapter.notifyDataSetChanged();
        handler.postDelayed(this, DateUtils.MINUTE_IN_MILLIS);
      }
    }, DateUtils.MINUTE_IN_MILLIS);

    if (CarteiroApplication.state.syncing) setRefreshing();
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    pi = list.get(position-1);
    Intent intent = new Intent(activity, RecordActivity.class).putExtra("postalItem", pi);
    startActivity(intent);

    v.setBackgroundColor(Color.TRANSPARENT);
  }

  @Override
  public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
    super.onCreateContextMenu(menu, v, menuInfo);
    activity.getMenuInflater().inflate(R.menu.postal_list_context, menu);

    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
    pi = list.get(info.position-1);

    menu.setHeaderTitle(pi.getSafeDesc());
    if (CarteiroApplication.state.syncing) {
      menu.findItem(R.id.refresh_opt).setEnabled(false);
    }
    menu.findItem(R.id.fav_opt).setTitle(
      String.format(getString(R.string.opt_toggle_fav), getString(pi.isFav() ? R.string.label_unmark_as : R.string.label_mark_as))
    );
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

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, pi).show(getFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  public void updateList() {
    if (query != null) dh.getSearchResults(list, query);
    else dh.getPostalList(list, category);
  }

  public List<PostalItem> getList() { return list; }
  public int getListSize() { return list.size(); }
  public void setCategory(int category) { this.category = category; }
  public void setQuery(String query) { this.query = query; }
  public int getCategory() { return category; }
  public String getQuery() { return query; }

  public void setRefreshing() { listView.setRefreshing(); }
  public void onRefreshComplete() { listView.onRefreshComplete(); }

  public void refreshList(boolean propagate) {
    updateList();
    listAdapter.notifyDataSetChanged();

    if (propagate) {
      if (activity instanceof MainActivity) {
        ((MainActivity) activity).refreshList();
      } else {
        app.setUpdatedList();
      }
    }
  }
}
