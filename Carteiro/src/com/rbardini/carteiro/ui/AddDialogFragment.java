package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.R;

public class AddDialogFragment extends DialogFragment {
  public static final String TAG = "AddDialogFragment";

  public static final int NOT_FOUND = 1;
  public static final int NET_ERROR = 2;
  public static final int DELIVERED_ITEM = 3;
  public static final int RETURNED_ITEM = 4;

  public interface OnAddDialogActionListener {
    public void onConfirmAddPostalItem(PostalItem pi);
        public void onCancelAddPostalItem(PostalItem pi);
    }

  private OnAddDialogActionListener listener;

  public static AddDialogFragment newInstance(int id, PostalItem pi) {
    AddDialogFragment f = new AddDialogFragment();
    Bundle args = new Bundle();
    args.putInt("id", id);
    args.putSerializable("postalItem", pi);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);

    try {
      listener = (OnAddDialogActionListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnAddDialogActionListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int id = getArguments().getInt("id");
    final PostalItem pi = (PostalItem) getArguments().getSerializable("postalItem");
    final FragmentActivity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    switch(id) {
      case NOT_FOUND:
      case NET_ERROR:
        boolean notFound = id == NOT_FOUND;
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(notFound ? R.string.title_alert_not_found : R.string.title_alert_net_error);
        builder.setMessage(notFound ? String.format(getString(R.string.msg_alert_not_found), pi.getCod()) : getString(R.string.msg_alert_net_error));
        builder.setPositiveButton(R.string.add_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            listener.onConfirmAddPostalItem(pi);
          }
        });
        builder.setNegativeButton(R.string.negative_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            listener.onCancelAddPostalItem(pi);
          }
        });
        break;

        case DELIVERED_ITEM:
        case RETURNED_ITEM:
          boolean delivered = id == DELIVERED_ITEM;
          builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setTitle(delivered ? R.string.title_alert_delivered_item : R.string.title_alert_returned_item);
        builder.setMessage(delivered ? R.string.msg_alert_delivered_item : R.string.msg_alert_returned_item);
        builder.setPositiveButton(R.string.add_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            listener.onConfirmAddPostalItem(pi);
          }
        });
        builder.setNegativeButton(R.string.negative_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int which) {
            listener.onCancelAddPostalItem(pi);
          }
        });
          break;
    }

    return builder.create();
  }
}
