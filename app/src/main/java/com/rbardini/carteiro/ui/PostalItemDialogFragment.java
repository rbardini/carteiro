package com.rbardini.carteiro.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
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
import com.rbardini.carteiro.model.PostalItem;

import java.util.ArrayList;

public class PostalItemDialogFragment extends DialogFragment {
  public static final String TAG = "PostalItemDialogFragment";

  interface OnPostalItemChangeListener {
    void onRenamePostalItem(String desc, PostalItem pi);
    void onDeletePostalItems(ArrayList<PostalItem> piList);
  }

  private OnPostalItemChangeListener listener;

  public static PostalItemDialogFragment newInstance(int id, ArrayList<PostalItem> piList) {
    PostalItemDialogFragment f = new PostalItemDialogFragment();
    Bundle args = new Bundle();
    args.putInt("id", id);
    args.putSerializable("postalList", piList);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Activity activity = getActivity();

    try {
      listener = (OnPostalItemChangeListener) activity;

    } catch (ClassCastException e) {
      throw new ClassCastException(activity.toString() + " must implement OnPostalItemChangeListener");
    }
  }

  @Override @SuppressWarnings("unchecked")
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int id = getArguments().getInt("id");
    final ArrayList<PostalItem> piList  = (ArrayList<PostalItem>) getArguments().getSerializable("postalList");
    final PostalItem pi = piList.get(0);
    final Activity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(activity);

    switch(id) {
      case R.id.rename_opt:
        LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.dialog_rename, (ViewGroup) activity.findViewById(R.id.layout_root));

        final EditText itemDesc = (EditText) layout.findViewById(R.id.item_desc_fld);
        itemDesc.setText(pi.getDesc());
        itemDesc.setOnEditorActionListener(new TextView.OnEditorActionListener() {
          @Override
          public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            onRenamePostemItem(itemDesc, pi);
            dismiss();
            return true;
          }
        });

        builder
          .setView(layout)
          .setTitle(getString(R.string.title_alert_rename, pi.getCod()))
          .setPositiveButton(R.string.rename_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              onRenamePostemItem(itemDesc, pi);
            }
          })
          .setNegativeButton(R.string.negative_btn, null);
        break;

      case R.id.delete_opt:
        int listSize = piList.size();

        builder
          .setTitle(getResources().getQuantityString(R.plurals.title_alert_delete, listSize, pi.getSafeDesc()))
          .setMessage(getResources().getQuantityString(R.plurals.msg_alert_delete, listSize))
          .setPositiveButton(R.string.delete_btn, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
              listener.onDeletePostalItems(piList);
            }
          })
          .setNegativeButton(R.string.negative_btn, null);
        break;
    }

    return builder.create();
  }

  private void onRenamePostemItem(EditText itemDesc, PostalItem pi) {
    String desc = itemDesc.getText().toString().trim();
    if (desc.equals("")) desc = null;
    listener.onRenamePostalItem(desc, pi);
  }
}
