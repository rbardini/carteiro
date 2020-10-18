package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ShipmentRenameDialogFragment extends AppCompatDialogFragment {
  public static final String TAG = "ShipmentRenameDialogFragment";
  private static final String SHIPMENT_KEY = "shipment";

  interface OnShipmentRenameListener {
    void onRenameShipment(String desc, Shipment shipment);
  }

  private OnShipmentRenameListener listener;

  public static ShipmentRenameDialogFragment newInstance(Shipment shipment) {
    ShipmentRenameDialogFragment f = new ShipmentRenameDialogFragment();
    Bundle args = new Bundle();
    args.putSerializable(SHIPMENT_KEY, shipment);
    f.setArguments(args);

    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Activity activity = getActivity();

    try {
      listener = (OnShipmentRenameListener) activity;

    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnShipmentRenameListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final Shipment shipment = (Shipment) getArguments().getSerializable(SHIPMENT_KEY);
    final Activity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = inflater.inflate(R.layout.dialog_rename, null);

    final EditText itemDesc = layout.findViewById(R.id.item_desc_fld);
    itemDesc.setText(shipment.getName());
    itemDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        onRenameShipment(itemDesc, shipment);
        dismiss();
        return true;
      }
    });

    builder
      .setView(layout)
      .setTitle(getString(R.string.title_alert_rename, shipment.getNumber()))
      .setPositiveButton(R.string.rename_btn, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int id) {
          onRenameShipment(itemDesc, shipment);
        }
      })
      .setNegativeButton(R.string.negative_btn, null);

    return builder.create();
  }

  private void onRenameShipment(EditText itemDesc, Shipment shipment) {
    String desc = itemDesc.getText().toString().trim();
    if (desc.equals("")) desc = null;
    listener.onRenameShipment(desc, shipment);
  }
}
