package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.SyncService;

import java.util.ArrayList;
import java.util.List;

public class PostalRecordFragment extends ListFragment {
  public static final String TAG = "PostalRecordFragment";

  private Activity activity;
  private DatabaseHelper dh;

  private static PostalItem pi;

  private List<PostalRecord> mList;
  private PostalRecordListAdapter mListAdapter;
  private SwipeRefreshLayout mSwipeRefreshLayout;

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

    mList = new ArrayList<PostalRecord>();
    mListAdapter = new PostalRecordListAdapter(activity, mList);
    setListAdapter(mListAdapter);

    updateList();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.record_list, container, false);
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    activity = getActivity();

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
  }

  public void setPostalItem(PostalItem newPostalItem) { pi = newPostalItem; }

  public void onRefreshStarted() {
    if (!CarteiroApplication.state.syncing) {
      Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
      intent.putExtra("cods", new String[] {pi.getCod()});
      activity.startService(intent);
    }
  }
  public void setRefreshing() { mSwipeRefreshLayout.setRefreshing(true); }
  public void onRefreshComplete() { mSwipeRefreshLayout.setRefreshing(false); }

  public void updateList() {
    dh.getPostalRecords(mList, pi.getCod());
  }

  public void refreshList() {
    updateList();
    mListAdapter.notifyDataSetChanged();
  }
}
