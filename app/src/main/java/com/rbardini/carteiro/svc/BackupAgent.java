package com.rbardini.carteiro.svc;

import android.app.backup.BackupAgentHelper;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.app.backup.FileBackupHelper;
import android.app.backup.FullBackupDataOutput;
import android.app.backup.SharedPreferencesBackupHelper;
import android.os.ParcelFileDescriptor;
import android.preference.PreferenceManager;
import android.util.Log;

import com.rbardini.carteiro.R;
import com.rbardini.carteiro.db.DatabaseHelper;

import java.io.File;
import java.io.IOException;

public class BackupAgent extends BackupAgentHelper {
  private static final String TAG = "BackupAgent";
  private static final String PREFS_KEY = "prefs";
  private static final String DB_KEY = "db";

  @Override
  public void onCreate() {
    SharedPreferencesBackupHelper prefsHelper = new SharedPreferencesBackupHelper(this, getPackageName()+"_preferences");
    addHelper(PREFS_KEY, prefsHelper);

    FileBackupHelper fileHelper = new FileBackupHelper(this, "../databases/"+DatabaseHelper.DB_NAME);
    addHelper(DB_KEY, fileHelper);
  }

  @Override
  public void onFullBackup(FullBackupDataOutput data) throws IOException {
    Log.i(TAG, "Backing up data to Android Auto Backup");
    super.onFullBackup(data);

    this.updateLastBackupTimestamp();
  }

  @Override
  public void onRestoreFile(ParcelFileDescriptor data, long size, File destination, int type, long mode, long mtime) throws IOException {
    Log.i(TAG, "Restoring backup from Android Auto Backup");
    super.onRestoreFile(data, size, destination, type, mode, mtime);
  }

  @Override
  public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data, ParcelFileDescriptor newState) throws IOException {
    Log.i(TAG, "Backing up data to Android Backup Service");
    super.onBackup(oldState, data, newState);

    this.updateLastBackupTimestamp();
  }

  @Override
  public void onRestore(BackupDataInput data, int appVersionCode, ParcelFileDescriptor newState) throws IOException {
    Log.i(TAG, "Restoring backup from Android Backup Service");
    super.onRestore(data, appVersionCode, newState);
  }

  private void updateLastBackupTimestamp() {
    PreferenceManager.getDefaultSharedPreferences(this)
      .edit()
      .putLong(getString(R.string.pref_key_last_backup), System.currentTimeMillis())
      .apply();
  }
}
