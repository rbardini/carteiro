package com.rbardini.carteiro.svc;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import com.rbardini.carteiro.db.DatabaseHelper;

import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {
  public static final String TAG = "BackupAgent";
  public static final String PREFS_KEY = "prefs";
  public static final String DB_KEY = "db";

  @Override
  public void onCreate() {
    SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, getPackageName()+"_preferences");
    addHelper(PREFS_KEY, prefsHelper);

    FileBackupHelper fileHelper = new FileBackupHelper(this, "../databases/"+DatabaseHelper.DB_NAME);
    addHelper(DB_KEY, fileHelper);
  }

  @Override
  public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
    Log.i(TAG, "Backing up data to Android Backup Service");
    super.onBackup(oldState, data, newState);
  }

  @Override
  public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
    Log.i(TAG, "Restoring backup from Android Backup Service");
    super.onRestore(data, appVersionCode, newState);
  }
}
