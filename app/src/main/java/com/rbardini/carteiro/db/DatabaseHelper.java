package com.rbardini.carteiro.db;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.util.Log;

import com.rbardini.carteiro.model.PostalItem;
import com.rbardini.carteiro.model.PostalRecord;
import com.rbardini.carteiro.svc.BackupManagerWrapper;
import com.rbardini.carteiro.util.IOUtils;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DatabaseHelper {
  private static final String TAG = "CarteiroDatabase";

  public static final String DB_NAME = "carteiro.db";
  public static final int DB_VERSION = 3;

  private static final DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

  private static final String POSTAL_ITEM_TABLE = "Postal_Item";
  private static final String POSTAL_RECORD_TABLE = "Postal_Record";
  private static final String POSTAL_LIST_VIEW = "Postal_List";
  private static final String DEL_POSTAL_RECORDS_TRIGGER = "Del_Postal_Records";

  private static final String SUGGEST_ID = BaseColumns._ID;
  private static final String SUGGEST_TITLE = SearchManager.SUGGEST_COLUMN_TEXT_1;
  private static final String SUGGEST_DESC = SearchManager.SUGGEST_COLUMN_TEXT_2;
  private static final String SUGGEST_DATA = SearchManager.SUGGEST_COLUMN_INTENT_DATA;
  private static final Map<String, String> SuggestMap = buildColumnMap();

  private static DatabaseHelper instance;
  private static SQLiteOpenHelper oh;
  private static SQLiteDatabase db;
  private static BackupManagerWrapper bm;
  private static boolean backupAvailable;

  private DatabaseHelper(Context context) {
    oh = new SQLiteOpenHelper(context);
    db = oh.getWritableDatabase();
    try {
      BackupManagerWrapper.checkAvailable();
      bm = new BackupManagerWrapper(context);
      backupAvailable = true;
    } catch (Throwable t) {
      backupAvailable = false;
    }
  }

  public static synchronized DatabaseHelper getInstance(Context context) {
    if (instance == null) instance = new DatabaseHelper(context);
    return instance;
  }

  public void beginTransaction() {
    db.beginTransaction();
  }

  public boolean inTransaction() {
    return db.inTransaction();
  }

  public void endTransaction() {
    db.endTransaction();
  }

  public void setTransactionSuccessful() {
    db.setTransactionSuccessful();
  }

  public File exportDatabase(Context context, File destFile) throws IOException {
    oh.close();

    if (!IOUtils.isExternalStorageWritable()) return null;

    File currentDb = context.getDatabasePath(DB_NAME);
    IOUtils.copyFile(new FileInputStream(currentDb), new FileOutputStream(destFile));

    db = oh.getWritableDatabase();

    return destFile;
  }

  public File importDatabase(Context context, File database) throws IOException {
    oh.close();

    if (!IOUtils.isExternalStorageReadable()) return null;

    File currentDb = context.getDatabasePath(DB_NAME);
    IOUtils.copyFile(new FileInputStream(database), new FileOutputStream(currentDb));

    db = oh.getWritableDatabase();

    notifyDatabaseChanged();

    return database;
  }

  public boolean insertPostalItem(PostalItem pi) {
    ContentValues cv = new ContentValues();
    cv.put("cod", pi.getCod().toUpperCase(Locale.getDefault()));
    cv.put("desc", pi.getDesc());
    cv.put("fav", pi.isFav() ? 1 : 0);

    try {
      db.insertOrThrow(POSTAL_ITEM_TABLE, null, cv);
      notifyDatabaseChanged();
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean insertPostalRecord(PostalRecord pr, int pos) {
    ContentValues cv = new ContentValues();
    cv.put("cod", pr.getCod().toUpperCase(Locale.getDefault()));
    cv.put("pos", pos);
    if (pr.getDate() != null) { cv.put("date", iso8601.format(pr.getDate())); }
    cv.put("status", pr.getStatus());
    cv.put("loc", pr.getLoc());
    cv.put("info", pr.getInfo());

    try {
      db.insertOrThrow(POSTAL_RECORD_TABLE, null, cv);
      notifyDatabaseChanged();
      return true;

    } catch (SQLException e) {
      return false;
    }
  }

  public boolean insertPostalRecords(List<PostalRecord> prList) {
    for (int i = 0, length = prList.size(); i < length; i++) {
      if (!insertPostalRecord(prList.get(i), i)) {
        return false;
      }
    }

    return true;
  }

  public int renamePostalItem(String cod, String desc) {
    ContentValues cv = new ContentValues();
    if (desc == null) cv.putNull("desc");
    else cv.put("desc", desc);

    int rows = db.update(POSTAL_ITEM_TABLE, cv, "cod = ?", new String[] {cod});
    if (rows != 0) notifyDatabaseChanged();

    return rows;
  }

  public void setPostalItemFav(String cod, int fav) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET fav = ? WHERE cod = ?", new Object[] {fav, cod});
    notifyDatabaseChanged();
  }

  public void togglePostalItemFav(String cod) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET fav = NOT fav WHERE cod = ?", new Object[] {cod});
    notifyDatabaseChanged();
  }

  public void favPostalItem(String cod) {
    setPostalItemFav(cod, 1);
  }

  public void unfavPostalItem(String cod) {
    setPostalItemFav(cod, 0);
  }

  public void setPostalItemArchived(String cod, int archived) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET archived = ? WHERE cod = ?", new Object[] {archived, cod});
    notifyDatabaseChanged();
  }

  public void togglePostalItemArchived(String cod) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET archived = NOT archived WHERE cod = ?", new Object[] {cod});
    notifyDatabaseChanged();
  }

  public void setPostalItemUnread(String cod, int unread) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET unread = ? WHERE cod = ?", new Object[] {unread, cod});
    notifyDatabaseChanged();
  }

  public void archivePostalItem(String cod) {
    setPostalItemArchived(cod, 1);
  }

  public void unarchivePostalItem(String cod) {
    setPostalItemArchived(cod, 0);
  }

  public int deletePostalItem(String cod) {
    int rows = db.delete(POSTAL_ITEM_TABLE, "cod = ?", new String[] {cod});
    if (rows != 0) notifyDatabaseChanged();

    return rows;
  }

  public int deletePostalRecords(String cod) {
    int rows = db.delete(POSTAL_RECORD_TABLE, "cod = ?", new String[] {cod});
    if (rows != 0) notifyDatabaseChanged();

    return rows;
  }

  public int deleteAll(String table) {
    int rows = db.delete(table, null, null);
    if (rows != 0) notifyDatabaseChanged();

    return rows;
  }

  public boolean isPostalItem(String cod) {
    return db.query(POSTAL_ITEM_TABLE, new String[] {"cod"}, "cod = ?", new String[] {cod}, null, null, null).moveToFirst();
  }

  public PostalItem getPostalItem(String cod) {
    PostalItem pi = null;
    Cursor c = db.query(POSTAL_LIST_VIEW, null, "cod = ?", new String[] {cod}, null, null, null);
    if (c.moveToFirst()) {
      try {
        pi = new PostalItem(c.getString(0), c.getString(1), iso8601.parse(c.getString(2)), c.getString(3),
            c.getString(4), c.getString(5), c.getInt(6)>0, c.getInt(7)>0, c.getInt(8)>0);
      } catch (ParseException e) {}
    }
    if (!c.isClosed()) {
      c.close();
    }
    return pi;
  }

  public String[] getPostalItemCodes(int flags) {
    List<String> cods = new ArrayList<>();
    String selection = "archived = ?";
    String[] selectionArgs = new String[] {"0"};

    if (((flags & Category.FAVORITES) != 0) && (flags & Category.UNDELIVERED) != 0) {
      selection += " AND fav = ? AND status NOT IN (?, ?)";
      selectionArgs = new String[] {"0", "1", Status.ENTREGA_EFETUADA, Status.DEVOLVIDO_AO_REMETENTE};
    } else {
      if ((flags & Category.FAVORITES) != 0) {
        selection += " AND fav = ?";
        selectionArgs = new String[] {"0", "1"};
      } else if ((flags & Category.UNDELIVERED) != 0) {
        selection += " AND status NOT IN (?, ?)";
        selectionArgs = new String[] {"0", Status.ENTREGA_EFETUADA, Status.DEVOLVIDO_AO_REMETENTE};
      }
    }

    Cursor c = db.query(POSTAL_LIST_VIEW, new String[] {"cod"}, selection, selectionArgs, null, null, null);
    if (c.moveToFirst()) {
      do {
        cods.add(c.getString(0));
      } while (c.moveToNext());
    }
    if (!c.isClosed()) {
      c.close();
    }

    return cods.toArray(new String[cods.size()]);
  }

  public int getPostalItems(List<PostalItem> list, String selection, String[] selectionArgs) {
    list.clear();
    Cursor c = db.query(POSTAL_LIST_VIEW, null, selection, selectionArgs, null, null, null);
    if (c.moveToFirst()) {
      do {
        try {
          list.add(new PostalItem(c.getString(0), c.getString(1), iso8601.parse(c.getString(2)), c.getString(3),
              c.getString(4), c.getString(5), c.getInt(6)>0, c.getInt(7)>0, c.getInt(8)>0));
        } catch (ParseException e) {
          Log.e(TAG, e.getMessage());
        }
      } while (c.moveToNext());
    }
    if (!c.isClosed()) {
      c.close();
    }

    return list.size();
  }

  public int getPostalList(List<PostalItem> list, int category) {
    String selection;
    String[] selectionArgs;

    switch (category) {
      case Category.ALL:
        selection = "archived = ?";
        selectionArgs = new String[] {"0"};
        break;
      case Category.FAVORITES:
        selection = "archived = ? AND fav = ?";
        selectionArgs = new String[] {"0", "1"};
        break;
      case Category.ARCHIVED:
        selection = "archived = ?";
        selectionArgs = new String[] {"1"};
        break;
      default:
        selection = "archived = ?";
        String[] statuses = Category.getStatuses(category);
        if (statuses != null) {
          selection += " AND status IN (";
          for (int i=0; i<statuses.length-1; i++) selection += "?, ";
          selection += "?)";
        }
        selectionArgs = new String[(statuses != null ? statuses.length : 0) + 1];
        selectionArgs[0] = "0";
        System.arraycopy(statuses, 0, selectionArgs, 1, statuses.length);
        break;
    }

    return getPostalItems(list, selection, selectionArgs);
  }

  public int getSearchResults(List<PostalItem> list, String query) {
    return getPostalItems(list, "cod LIKE ? OR desc LIKE ?", new String[] {"%"+query+"%", "%"+query+"%"});
  }

  public Cursor getSearchSuggestions(String query) {
    SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
    builder.setTables(POSTAL_LIST_VIEW);
    builder.setProjectionMap(SuggestMap);

    String[] columns = {SUGGEST_ID, SUGGEST_TITLE, SUGGEST_DESC, SUGGEST_DATA};

    return builder.query(db, columns, SUGGEST_TITLE+" LIKE ? OR "+SUGGEST_DESC+" LIKE ?",
        new String[] {"%"+query+"%", "%"+query+"%"}, null, null, null);
  }

  public PostalRecord getLastPostalRecord(String cod) throws ParseException {
    PostalRecord pr = null;
    Cursor c = db.query(POSTAL_RECORD_TABLE, null, "cod = ?", new String[] {cod}, null, null, "pos DESC", "1");
    if (c.moveToFirst()) {
      pr = new PostalRecord(cod, iso8601.parse(c.getString(2)), c.getString(3), c.getString(4), c.getString(5));
    }
    if (!c.isClosed()) {
      c.close();
    }

    return pr;
  }

  public PostalRecord getFirstPostalRecord(String cod) throws ParseException {
    PostalRecord pr = null;
    Cursor c = db.query(POSTAL_RECORD_TABLE, null, "cod = ?", new String[] {cod}, null, null, "pos ASC", "1");
    if (c.moveToFirst()) {
      pr = new PostalRecord(cod, iso8601.parse(c.getString(2)), c.getString(3), c.getString(4), c.getString(5));
    }
    if (!c.isClosed()) {
      c.close();
    }

    return pr;
  }

  public int getPostalRecords(List<PostalRecord> list, String cod) {
    list.clear();
    Cursor c = db.query(POSTAL_RECORD_TABLE, null, "cod = ?", new String[] {cod}, null, null, "pos ASC");
    if (c.moveToFirst()) {
      do {
        try {
          list.add(new PostalRecord(c.getString(0), iso8601.parse(c.getString(2)), c.getString(3), c.getString(4), c.getString(5)));
        } catch (ParseException e) {
          Log.e(TAG, e.getMessage());
        }
      } while (c.moveToNext());
    }
    if (!c.isClosed()) {
      c.close();
    }

    return list.size();
  }

  private void notifyDatabaseChanged() {
    if (backupAvailable) bm.dataChanged();
  }

  private static HashMap<String, String> buildColumnMap() {
    HashMap<String, String> map = new HashMap<>();
    map.put(SUGGEST_ID, "cod AS "+SUGGEST_ID);
    map.put(SUGGEST_TITLE, "cod AS "+SUGGEST_TITLE);
    map.put(SUGGEST_DESC, "desc AS "+SUGGEST_DESC);
    map.put(SUGGEST_DATA, "cod AS "+SUGGEST_DATA);

    return map;
  }

  private static List<String> GetColumns(SQLiteDatabase db, String tableName) {
    List<String> columns = null;
    Cursor c = null;

    try {
      c = db.query(tableName, null, null, null, null, null, null, "1");
      if (c != null) {
        columns = new ArrayList<>(Arrays.asList(c.getColumnNames()));
      }
    } catch (Exception e) {
      Log.v(tableName, e.getMessage(), e);
    } finally {
      if (c != null) {
        c.close();
      }
    }
    return columns;
  }

  private class SQLiteOpenHelper extends android.database.sqlite.SQLiteOpenHelper {
    public SQLiteOpenHelper(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL(Query.getCreate(POSTAL_ITEM_TABLE));
      db.execSQL(Query.getCreate(POSTAL_RECORD_TABLE));
      db.execSQL(Query.getCreate(POSTAL_LIST_VIEW));
      db.execSQL(Query.getCreate(DEL_POSTAL_RECORDS_TRIGGER));
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      Log.i(TAG, "Upgrading database from version "+oldVersion+" to version "+newVersion);

      switch (oldVersion) {
        case 1:
          db.execSQL("ALTER TABLE " + POSTAL_ITEM_TABLE + " ADD COLUMN archived BOOLEAN NOT NULL DEFAULT 0");
          db.execSQL(Query.getDrop(POSTAL_LIST_VIEW));
          db.execSQL(Query.getCreate(POSTAL_LIST_VIEW));

        case 2:
          db.execSQL("ALTER TABLE " + POSTAL_ITEM_TABLE + " ADD COLUMN unread BOOLEAN NOT NULL DEFAULT 0");
          db.execSQL(Query.getDrop(POSTAL_LIST_VIEW));
          db.execSQL(Query.getCreate(POSTAL_LIST_VIEW));
      }
    }
  }

  private static final class Query {
    private static final Map<String, String> CreateMap = buildCreateMap();
    private static final Map<String, String> DropMap = buildDropMap();

    private static Map<String, String> buildCreateMap() {
      Map<String, String> map = new HashMap<>();

      map.put(POSTAL_ITEM_TABLE, "CREATE TABLE IF NOT EXISTS " + POSTAL_ITEM_TABLE + " ("
          + "cod TEXT NOT NULL PRIMARY KEY, "
          + "desc TEXT, "
          + "fav BOOLEAN, "
          + "added DATETIME NOT NULL DEFAULT (DATETIME('now','localtime')), "
          + "archived BOOLEAN NOT NULL DEFAULT 0, "
          + "unread BOOLEAN NOT NULL DEFAULT 0)");

      map.put(POSTAL_RECORD_TABLE, "CREATE TABLE IF NOT EXISTS " + POSTAL_RECORD_TABLE + " ("
          + "cod TEXT NOT NULL, "
          + "pos INTEGER NOT NULL, "
          + "date DATETIME NOT NULL DEFAULT (DATETIME('now','localtime')), "
          + "status TEXT NOT NULL COLLATE NOCASE, "
          + "loc TEXT, "
          + "info TEXT, "
          + "PRIMARY KEY (cod, pos) ON CONFLICT REPLACE)");

      map.put(POSTAL_LIST_VIEW, "CREATE VIEW IF NOT EXISTS " + POSTAL_LIST_VIEW + " AS "
          + "SELECT pi.cod AS cod, pi.desc AS desc, pr.date AS date, pr.loc AS loc, pr.info AS info, pr.status AS status, pi.fav AS fav, pi.archived AS archived, pi.unread as unread "
          + "FROM " + POSTAL_ITEM_TABLE + " pi JOIN " + POSTAL_RECORD_TABLE + " pr ON pi.cod = pr.cod "
          + "WHERE pr.pos = ("
          +   "SELECT MAX(pr2.pos) FROM " + POSTAL_RECORD_TABLE + " pr2 WHERE pr2.cod = pi.cod"
          + ") "
          + "ORDER BY pr.date DESC");

      map.put(DEL_POSTAL_RECORDS_TRIGGER, "CREATE TRIGGER IF NOT EXISTS " + DEL_POSTAL_RECORDS_TRIGGER + " "
          + "AFTER DELETE ON " + POSTAL_ITEM_TABLE + " "
          + "BEGIN "
          +   "DELETE FROM " + POSTAL_RECORD_TABLE + " WHERE cod = OLD.cod; "
          + "END;");

      return map;
    }

    private static Map<String, String> buildDropMap() {
      Map<String, String> map = new HashMap<>();

      map.put(POSTAL_ITEM_TABLE, "DROP TABLE IF EXISTS " + POSTAL_ITEM_TABLE);
      map.put(POSTAL_RECORD_TABLE, "DROP TABLE IF EXISTS " + POSTAL_RECORD_TABLE);
      map.put(POSTAL_LIST_VIEW, "DROP VIEW IF EXISTS " + POSTAL_LIST_VIEW);
      map.put(DEL_POSTAL_RECORDS_TRIGGER, "DROP TRIGGER IF EXISTS " + DEL_POSTAL_RECORDS_TRIGGER);

      return map;
    }

    public static String getCreate(String entity) { return CreateMap.get(entity); }
    public static String getDrop(String entity) { return DropMap.get(entity); }
  }
}
