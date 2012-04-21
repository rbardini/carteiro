package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.R;

public class PostalItemDialogFragment extends DialogFragment {
  public static final String TAG = "PostalItemDialogFragment";

  public interface OnPostalItemChangeListener {
    public void onRenamePostalItem(String desc, PostalItem pi);
    public void onDeletePostalItem(PostalItem pi);
  }

  private OnPostalItemChangeListener listener;

  public static PostalItemDialogFragment newInstance(int id, PostalItem pi) {
    PostalItemDialogFragment f = new PostalItemDialogFragment();
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
      listener = (OnPostalItemChangeListener) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnPostalItemChangeListener");
    }
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int id = getArguments().getInt("id");
    final PostalItem pi  = (PostalItem) getArguments().getSerializable("postalItem");
    final FragmentActivity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    switch(id) {
      case R.id.rename_opt:
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.item_desc_dialog, (ViewGroup) activity.findViewById(R.id.layout_root));

        final EditText itemDesc = (EditText) layout.findViewById(R.id.item_desc_fld);
        itemDesc.setText(pi.getDesc());

        builder.setView(layout);
        builder.setTitle(pi.getCod());
        builder.setPositiveButton(R.string.rename_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            String desc = itemDesc.getText().toString();
            listener.onRenamePostalItem(desc, pi);
          }
        });
        builder.setNegativeButton(R.string.negative_btn, null);
        break;

      case R.id.delete_opt:
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(String.format(getString(R.string.title_alert_delete), pi.getSafeDesc()));
        builder.setMessage(R.string.msg_alert_delete);
        builder.setPositiveButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
          @Override
          public void onClick(DialogInterface dialog, int id) {
            listener.onDeletePostalItem(pi);
          }
        });
        builder.setNegativeButton(R.string.negative_btn, null);
        break;
    }

    return builder.create();
  }
}
