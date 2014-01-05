package com.rbardini.carteiro.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.svc.SyncService;

public class PostalRecordFragment extends ListFragment {
  public static final String TAG = "PostalRecordFragment";

  private FragmentActivity activity;
  private DatabaseHelper dh;

  private static PostalItem pi;

  private List<PostalRecord> list;
  private PostalRecordListAdapter listAdapter;
  private PullToRefreshListView listView;

  public static PostalRecordFragment newInstance(PostalItem pi) {
    PostalRecordFragment f = new PostalRecordFragment();
    Bundle args = new Bundle();
    args.putSerializable("postalItem", pi);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setRetainInstance(true);

    activity = getActivity();
    dh = ((CarteiroApplication) activity.getApplication()).getDatabaseHelper();

    pi = (PostalItem) getArguments().getSerializable("postalItem");
    list = new ArrayList<PostalRecord>();
    updateList();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.record_list, container, false);

    return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    listAdapter = new PostalRecordListAdapter(activity, list);
    setListAdapter(listAdapter);
    listView = (PullToRefreshListView) getView().findViewById(R.id.pull_to_refresh_listview);
        listView.setOnRefreshListener(new OnRefreshListener<ListView>() {
          @Override
          public void onRefresh(PullToRefreshBase<ListView> refreshView) {
            if (!CarteiroApplication.state.syncing) {
              Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
              intent.putExtra("cods", new String[] {pi.getCod()});
                  activity.startService(intent);
            }
          }
      });
  }

  public void setPostalItem(PostalItem newPostalItem) { pi = newPostalItem; }

  public void setRefreshing() { listView.setRefreshing(); }
  public void onRefreshComplete() { listView.onRefreshComplete(); }

  public void updateList() {
    dh.getPostalRecords(list, pi.getCod());
  }

  public void refreshList() {
    updateList();
    listAdapter.notifyDataSetChanged();
  }
}
