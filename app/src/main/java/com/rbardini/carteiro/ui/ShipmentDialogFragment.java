package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.model.Shipment;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

public class ShipmentDialogFragment extends AppCompatDialogFragment {
  public static final String TAG = "ShipmentDialogFragment";

  interface OnShipmentChangeListener {
    void onRenameShipment(String desc, Shipment shipment);
    void onDeleteShipments(ArrayList<Shipment> shipments);
  }

  private OnShipmentChangeListener listener;

  public static ShipmentDialogFragment newInstance(int id, ArrayList<Shipment> shipments) {
    ShipmentDialogFragment f = new ShipmentDialogFragment();
    Bundle args = new Bundle();
    args.putInt("id", id);
    args.putSerializable("shipments", shipments);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Activity activity = getActivity();

    try {
      listener = (OnShipmentChangeListener) activity;

    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnShipmentChangeListener");
    }
  }

  @Override @SuppressWarnings("unchecked")
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int id = getArguments().getInt("id");
    final ArrayList<Shipment> shipments = (ArrayList<Shipment>) getArguments().getSerializable("shipments");
    final Shipment shipment = shipments.get(0);
    final Activity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    switch(id) {
      case R.id.rename_opt:
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_rename, (ViewGroup) activity.findViewById(R.id.layout_root));

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
        break;

      case R.id.delete_opt:
        int listSize = shipment.size();

        builder
          .setTitle(getResources().getQuantityString(R.plurals.title_alert_delete, listSize, shipment.getDescription()))
          .setMessage(getResources().getQuantityString(R.plurals.msg_alert_delete, listSize))
          .setPositiveButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              listener.onDeleteShipments(shipments);
            }
          })
          .setNegativeButton(R.string.negative_btn, null);
        break;
    }

    return builder.create();
  }

  private void onRenameShipment(EditText itemDesc, Shipment shipment) {
    String desc = itemDesc.getText().toString().trim();
    if (desc.equals("")) desc = null;
    listener.onRenameShipment(desc, shipment);
  }
}
