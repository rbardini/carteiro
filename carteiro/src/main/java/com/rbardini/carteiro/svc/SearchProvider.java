package com.rbardini.carteiro.svc;

import com.rbardini.carteiro.db.DatabaseHelper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;

public class SearchProvider extends ContentProvider {
  public static final String AUTHORITY = "com.rbardini.carteiro.SearchProvider";
  public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/activity_search");

  private DatabaseHelper dh;

  @Override
  public boolean onCreate() {
    dh = DatabaseHelper.getInstance(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return dh.getSearchSuggestions(selectionArgs[0]);
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }
}
