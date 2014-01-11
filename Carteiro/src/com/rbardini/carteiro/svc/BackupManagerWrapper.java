package com.rbardini.carteiro.svc;

import android.annotation.TargetApi;
import android.app.backup.BackupManager;
import android.content.Context;
import android.util.Log;

@TargetApi(8)
public class BackupManagerWrapper {
  public static final String TAG = "BackupManagerWrapper";

  private static BackupManager instance;

  static {
    try {
      Class.forName("android.app.backup.BackupManager");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public static void checkAvailable() {}

  public BackupManagerWrapper(Context context) {
    if (instance == null) {
      instance = new BackupManager(context);
    }
  }

  public void dataChanged() {
    Log.i(TAG, "Notifying data change to Android Backup Service");
    instance.dataChanged();
  }

  public static void dataChanged(String packageName) {
    Log.i(TAG, "Notifying data change to Android Backup Service");
    BackupManager.dataChanged(packageName);
  }
}
