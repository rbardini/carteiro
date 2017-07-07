package com.rbardini.carteiro.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rbardini.carteiro.CarteiroApplication;
import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;
import com.rbardini.carteiro.svc.SyncService;

import java.util.ArrayList;

public class RecordFragment extends ShipmentFragment {
  public static final String TAG = "RecordFragment";

  private Shipment mShipment;
  private ShipmentAdapter mListAdapter;

  public static RecordFragment newInstance(Shipment shipment) {
    RecordFragment f = new RecordFragment();
    Bundle args = new Bundle();
    args.putSerializable("shipment", shipment);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setRetainInstance(true);

    mShipment = (Shipment) getArguments().getSerializable("shipment");
    mListAdapter = new ShipmentAdapter(mActivity, mShipment);
    setListAdapter(mListAdapter);

    updateList();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.list_record, container, false);
  }

  @Override
  public void onRefresh() {
    if (!CarteiroApplication.syncing) {
      ArrayList<Shipment> shipments = new ArrayList<>();
      shipments.add(mShipment);

      Intent intent = new Intent(Intent.ACTION_SYNC, null, mActivity, SyncService.class).putExtra("shipments", shipments);
      mActivity.startService(intent);
    }
  }

  @Override
  public void refreshList() {
    updateList();
    mListAdapter.notifyDataSetChanged();
  }

  public void setShipment(Shipment shipment) {
    mShipment = shipment;
  }

  public void updateList() {
    mShipment.loadRecords(dh);
  }
}
