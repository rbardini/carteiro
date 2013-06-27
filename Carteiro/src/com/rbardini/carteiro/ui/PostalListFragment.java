package com.rbardini.carteiro.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.content.Intent;
import android.net.Uri;
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
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncService;
import com.rbardini.carteiro.util.PostalUtils;
import com.rbardini.carteiro.util.UIUtils;

public class PostalListFragment extends ListFragment {
  private CarteiroApplication app;
  private FragmentActivity activity;
  private Handler handler;
  private DatabaseHelper dh;

  private static PostalItem pi;
  private int position;
  private String query;

  private List<PostalItem> list;
  private PostalItemListAdapter listAdapter;
  private PullToRefreshListView listView;

  public static PostalListFragment newInstance(int position) {
    PostalListFragment f = new PostalListFragment();
    Bundle args = new Bundle();
    args.putInt("position", position);
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.postal_list, container, false);

    return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    activity = getActivity();
    app = (CarteiroApplication) activity.getApplication();
    handler = new Handler();
    handler.postDelayed(new Runnable() {
      @Override
      public void run() {
        listAdapter.notifyDataSetChanged();
        handler.postDelayed(this, DateUtils.MINUTE_IN_MILLIS);
      }
    }, DateUtils.MINUTE_IN_MILLIS);
    dh = app.getDatabaseHelper();

    pi = null;
    setPosition(getArguments().getInt("position"));
    setQuery(getArguments().getString("query"));

    list = new ArrayList<PostalItem>();
    updateList();
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
  }

  @Override
  public void onListItemClick(ListView l, View v, int position, long id) {
    super.onListItemClick(l, v, position, id);

    pi = list.get(position-1);
    Intent intent = new Intent(activity, RecordActivity.class);
    intent.putExtra("postalItem", pi);
    startActivity(intent);
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
              Intent refresh = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
              refresh.putExtra("cods", new String[] {pi.getCod()});
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
        if (pi.getLoc() == null) {
          UIUtils.showToast(activity, getString(R.string.text_unknown_location));
        } else {
          Intent place = new Intent(Intent.ACTION_VIEW, Uri.parse(PostalUtils.getLocation(pi, true)));
          try {
            startActivity(place);
          } catch (Exception e) {
            UIUtils.showToast(activity, e.getMessage());
          }
        }
        return true;

      case R.id.share_opt:
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_status_subject));
        share.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.share_status_text),
            pi.getFullDesc(), pi.getStatus().toLowerCase(Locale.getDefault()), UIUtils.getRelativeTime(pi.getDate())));
        startActivity(Intent.createChooser(share, getString(R.string.share_title)));
        return true;

      case R.id.websro_opt:
        UIUtils.openURL(activity, String.format(getString(R.string.websro_url), pi.getCod()));
        return true;

      case R.id.delete_opt:
        PostalItemDialogFragment.newInstance(R.id.delete_opt, pi).show(getFragmentManager(), PostalItemDialogFragment.TAG);
        return true;

      default:
        return super.onContextItemSelected(item);
    }
  }

  public void updateList() {
    if (query != null) {
      dh.getSearchResults(list, query);
    } else {
      dh.getPostalList(list, MainPagerAdapter.category[position]);
    }
  }

  public List<PostalItem> getList() { return list; }
  public void setPosition(int position) { this.position = position; }
  public void setQuery(String query) { this.query = query; }

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
