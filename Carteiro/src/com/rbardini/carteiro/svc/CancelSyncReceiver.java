package com.rbardini.carteiro.svc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class CancelSyncReceiver extends BroadcastReceiver {

  @Override
  public void onReceive(Context context, Intent intent) {
    SyncService.cancelSync();
  }

}
