package com.rbardini.carteiro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.SyncService;

import java.util.ArrayList;
import java.util.List;

public class PostalRecordFragment extends PostalFragment {
  public static final String TAG = "PostalRecordFragment";

  private static PostalItem pi;

  private List<PostalRecord> mList;
  private PostalRecordListAdapter mListAdapter;

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

    pi = (PostalItem) getArguments().getSerializable("postalItem");

    mList = new ArrayList<>();
    mListAdapter = new PostalRecordListAdapter(mActivity, mList);
    setListAdapter(mListAdapter);

    updateList();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.record_list, container, false);
  }

  @Override
  public void onRefresh() {
    if (!CarteiroApplication.state.syncing) {
      Intent intent = new Intent(Intent.ACTION_SYNC, null, mActivity, SyncService.class);
      intent.putExtra("cods", new String[] {pi.getCod()});
      mActivity.startService(intent);
    }
  }

  @Override
  public void refreshList() {
    updateList();
    mListAdapter.notifyDataSetChanged();
  }

  public void setPostalItem(PostalItem newPostalItem) {
    pi = newPostalItem;
  }

  public void updateList() {
    dh.getPostalRecords(mList, pi.getCod());
  }
}
