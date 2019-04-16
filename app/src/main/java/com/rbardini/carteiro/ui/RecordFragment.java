package com.rbardini.carteiro.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncTask;

import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class RecordFragment extends ShipmentFragment {
  public static final String TAG = "RecordFragment";

  private static final String SHIPMENT_KEY = "shipment";

  private Shipment mShipment;
  private ShipmentAdapter mListAdapter;
  private RecyclerView mRecyclerView;

  public static RecordFragment newInstance(Shipment shipment) {
    RecordFragment f = new RecordFragment();
    Bundle args = new Bundle();
    args.putSerializable(SHIPMENT_KEY, shipment);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);

    mShipment = (Shipment) getArguments().getSerializable(SHIPMENT_KEY);

    updateList();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    final FragmentActivity activity = getActivity();

    View view = inflater.inflate(R.layout.list_record, container, false);
    LinearLayoutManager layoutManager = new LinearLayoutManager(activity);

    mListAdapter = new ShipmentAdapter(activity, mShipment);
    mRecyclerView = view.findViewById(android.R.id.list);
    mRecyclerView.setLayoutManager(layoutManager);
    mRecyclerView.setAdapter(mListAdapter);
    mRecyclerView.getViewTreeObserver().addOnPreDrawListener(
      new ViewTreeObserver.OnPreDrawListener() {
        @Override
        public boolean onPreDraw() {
          mRecyclerView.getViewTreeObserver().removeOnPreDrawListener(this);
          activity.supportStartPostponedEnterTransition();
          return true;
        }
      }
    );

    return view;
  }

  @Override
  public void onRefresh() {
    if (!CarteiroApplication.syncing) {
      SyncTask.run(app, mShipment);
    }
  }

  @Override
  public void refreshList() {
    updateList();
    if (mListAdapter != null) mListAdapter.notifyDataSetChanged();
  }

  public void setShipment(Shipment shipment) {
    mShipment = shipment;
  }

  public void updateList() {
    mShipment.loadRecords(dh);
  }
}
