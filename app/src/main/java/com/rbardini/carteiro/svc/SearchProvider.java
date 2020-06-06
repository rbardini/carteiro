package com.rbardini.carteiro.svc;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

import com.rbardini.carteiro.db.DatabaseHelper;

import androidx.annotation.NonNull;

public class SearchProvider extends ContentProvider {
  public static final String AUTHORITY = "com.rbardini.carteiro.svc.SearchProvider";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/activity_search");

  private DatabaseHelper dh;

  @Override
  public boolean onCreate() {
    dh = DatabaseHelper.getInstance(getContext());
    return true;
  }

  @Override
  public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return dh.getSearchSuggestions(selectionArgs[0]);
  }

  @Override
  public Uri insert(@NonNull Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getType(@NonNull Uri uri) {
    return null;
  }
}
