package com.rbardini.carteiro.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.util.UIUtils;

public class MainDialogFragment extends DialogFragment {
  public static final String TAG = "MainDialogFragment";

  public static MainDialogFragment newInstance(int id, boolean retry) {
    MainDialogFragment f = new MainDialogFragment();
    Bundle args = new Bundle();
    args.putInt("id", id);
    args.putBoolean("retry", retry);
    f.setArguments(args);
    return f;
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    final int id = getArguments().getInt("id");
    final boolean retry = getArguments().getBoolean("retry");
    final FragmentActivity activity = getActivity();
    final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

    switch(id) {
      case R.id.license_opt:
        builder.setIcon(android.R.drawable.ic_dialog_alert);
        builder.setTitle(R.string.unlicensed_dialog_title);
              builder.setMessage(retry ? R.string.unlicensed_dialog_retry_body : R.string.unlicensed_dialog_body);
              builder.setPositiveButton(retry ? R.string.retry_btn: R.string.buy_btn, new DialogInterface.OnClickListener() {
                  @Override
          public void onClick(DialogInterface dialog, int which) {
                      if (retry) {
                          ((MainActivity) activity).checkLicense();
                      } else {
                        UIUtils.openMarket(activity);
                      }
                  }
              });
              builder.setNegativeButton(R.string.continue_btn, null);
              break;
    }

    return builder.create();
  }
}
