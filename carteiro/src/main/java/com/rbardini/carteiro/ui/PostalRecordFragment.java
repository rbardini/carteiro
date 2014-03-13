package com.rbardini.carteiro.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
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

import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.AbsDefaultHeaderTransformer;
import uk.co.senab.actionbarpulltorefresh.extras.actionbarsherlock.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.Options;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class PostalRecordFragment extends ListFragment implements OnRefreshListener {
  public static final String TAG = "PostalRecordFragment";

  private FragmentActivity activity;
  private DatabaseHelper dh;

  private static PostalItem pi;

  private List<PostalRecord> mList;
  private PostalRecordListAdapter mListAdapter;
  private PullToRefreshLayout mPullToRefreshLayout;

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
    View v = inflater.inflate(R.layout.record_list, container, false);

    return v;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    activity = getActivity();

    mPullToRefreshLayout = (PullToRefreshLayout) getView().findViewById(R.id.ptr_layout);
    ActionBarPullToRefresh
        .from(activity)
        .options(Options.create().headerTransformer(new CustomAbsDefaultHeaderTransformer()).build())
        .listener(this)
        .setup(mPullToRefreshLayout);
  }

  @Override
  public void onRefreshStarted(View view) {
    if (!CarteiroApplication.state.syncing) {
      Intent intent = new Intent(Intent.ACTION_SYNC, null, activity, SyncService.class);
      intent.putExtra("cods", new String[] {pi.getCod()});
      activity.startService(intent);
    }
  }

  public void setPostalItem(PostalItem newPostalItem) { pi = newPostalItem; }

  public void setRefreshing() { mPullToRefreshLayout.setRefreshing(true); }
  public void onRefreshComplete() { mPullToRefreshLayout.setRefreshComplete(); }

  public void updateList() {
    dh.getPostalRecords(mList, pi.getCod());
  }

  public void refreshList() {
    updateList();
    mListAdapter.notifyDataSetChanged();
  }

  private static class CustomAbsDefaultHeaderTransformer extends AbsDefaultHeaderTransformer {
    @Override
    protected int getActionBarSize(Context context) {
      return 0;
    }
  }
}
