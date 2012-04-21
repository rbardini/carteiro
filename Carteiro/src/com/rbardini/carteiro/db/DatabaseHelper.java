package com.rbardini.carteiro.db;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.rbardini.carteiro.PostalItem;
import com.rbardini.carteiro.PostalRecord;
import com.rbardini.carteiro.svc.BackupManagerWrapper;
import com.rbardini.carteiro.util.PostalUtils.Category;
import com.rbardini.carteiro.util.PostalUtils.Status;

public class DatabaseHelper {
  public static final String TAG = "CarteiroDatabase";
  public static final String DB_NAME = "carteiro.db";
  public static final int DB_VERSION = 1;

  public static final DateFormat iso8601 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

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
  private static SQLiteDatabase db;
  private static BackupManagerWrapper bm;
  private static boolean backupAvailable;

  private DatabaseHelper(Context context) {
    db = (new CarteiroDatabase(context)).getWritableDatabase();
    try {
      BackupManagerWrapper.checkAvailable();
      bm = new BackupManagerWrapper(context);
      backupAvailable = true;
    } catch (Throwable t) {
      backupAvailable = false;
    }
  }

  public static synchronized DatabaseHelper getInstance(Context context) {
    if (instance == null) {
      instance = new DatabaseHelper(context);
    }
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

  public boolean insertPostalItem(PostalItem pi) {
    ContentValues cv = new ContentValues();
    cv.put("cod", pi.getCod().toUpperCase());
    cv.put("desc", pi.getDesc());
    cv.put("fav", pi.isFav() ? 1 : 0);

    try {
      db.insertOrThrow(POSTAL_ITEM_TABLE, null, cv);
      if (backupAvailable) {
        bm.dataChanged();
      }
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  public boolean insertPostalRecord(PostalRecord pr) {
    ContentValues cv = new ContentValues();
    cv.put("cod", pr.getCod().toUpperCase());
    cv.put("pos", pr.getPos());
    if (pr.getDate() != null) { cv.put("date", iso8601.format(pr.getDate())); }
    cv.put("status", pr.getStatus());
    cv.put("loc", pr.getLoc());
    cv.put("info", pr.getInfo());

    try {
      db.insertOrThrow(POSTAL_RECORD_TABLE, null, cv);
      if (backupAvailable) {
        bm.dataChanged();
      }
      return true;
    } catch (SQLException e) {
      return false;
    }
  }

  public int renamePostalItem(String cod, String desc) {
    ContentValues cv = new ContentValues();
    cv.put("desc", desc);

    int rows = db.update(POSTAL_ITEM_TABLE, cv, "cod = ?", new String[] {cod});
    if (rows != 0 && backupAvailable) {
      bm.dataChanged();
    }

    return rows;
  }

  public void togglePostalItemFav(String cod) {
    db.execSQL("UPDATE "+POSTAL_ITEM_TABLE+" SET fav = NOT fav WHERE cod = ?", new Object[] {cod});
    if (backupAvailable) {
      bm.dataChanged();
    }
  }

  public int deletePostalItem(String cod) {
    int rows = db.delete(POSTAL_ITEM_TABLE, "cod=?", new String[] {cod});
    if (rows != 0 && backupAvailable) {
      bm.dataChanged();
    }

    return rows;
  }

  public int deleteAll(String table) {
    int rows = db.delete(table, null, null);
    if (rows != 0 && backupAvailable) {
      bm.dataChanged();
    }

    return rows;
  }

  public boolean isPostalItem(String cod) {
    Cursor c = db.query(POSTAL_ITEM_TABLE, new String[] {"cod"}, "cod = ?", new String[] {cod}, null, null, null);
    return c.moveToFirst();
  }

  public PostalItem getPostalItem(String cod) {
    PostalItem pi = null;
    Cursor c = db.query(POSTAL_LIST_VIEW, null, "cod = ?", new String[] {cod}, null, null, null);
    if (c.moveToFirst()) {
      try {
        pi = new PostalItem(c.getString(0), c.getString(1), iso8601.parse(c.getString(2)),
              c.getString(3), c.getString(4), c.getString(5), c.getInt(6)>0);
      } catch (ParseException e) {}
    }
    if (!c.isClosed()) {
      c.close();
    }
    return pi;
  }

  public String[] getPostalItemCodes(int flags) {
    List<String> cods = new ArrayList<String>();
    String selection = null;
    String[] selectionArgs = null;

    if (((flags & Category.FAVORITES) != 0) && (flags & Category.UNDELIVERED) != 0) {
      //selection = "fav = ? AND status != ?";
      selection = "fav = ? AND status NOT IN (?, ?, ?)";
      selectionArgs = new String[] {"1", Status.ENTREGUE, Status.ENTREGA_EFETUADA, Status.DISTRIBUIDO_AO_REMETENTE};
    } else {
      if ((flags & Category.FAVORITES) != 0) {
        selection = "fav = ?";
        selectionArgs = new String[] {"1"};
      } else if ((flags & Category.UNDELIVERED) != 0) {
        selection = "status NOT IN (?, ?, ?)";
        selectionArgs = new String[] {Status.ENTREGUE, Status.ENTREGA_EFETUADA, Status.DISTRIBUIDO_AO_REMETENTE};
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

    return cods.toArray(new String[] {});
  }

  public int getPostalItems(List<PostalItem> list, String selection, String[] selectionArgs) {
    list.clear();
    Cursor c = db.query(POSTAL_LIST_VIEW, null, selection, selectionArgs, null, null, null);
    if (c.moveToFirst()) {
      do {
        try {
          list.add(new PostalItem(c.getString(0), c.getString(1), iso8601.parse(c.getString(2)),
                      c.getString(3), c.getString(4), c.getString(5), c.getInt(6)>0));
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
    String selection = null;
    String[] selectionArgs = null;

    switch(category) {
      case Category.ALL:
        break;
      case Category.FAVORITES:
        selection = "fav = ?";
        selectionArgs = new String[] {"1"};
        break;
      default:
        selectionArgs = Category.getStatuses(category);
        if (selectionArgs != null) {
          String arguments = "?";
          for(int i=1; i<selectionArgs.length; i++) {
            arguments += ", ?";
          }
          selection = "status IN ("+arguments+")";
        }
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

    Cursor c = builder.query(db, columns, SUGGEST_TITLE+" LIKE ? OR "+SUGGEST_DESC+" LIKE ?",
        new String[] {"%"+query+"%", "%"+query+"%"}, null, null, null);
    return c;
  }

  public PostalRecord getLastPostalRecord(String cod) throws ParseException {
    PostalRecord pr = null;
    Cursor c = db.query(POSTAL_RECORD_TABLE, null, "cod = ?", new String[] {cod}, null, null, "pos DESC", "1");
    if (c.moveToFirst()) {
      pr = new PostalRecord(cod, c.getInt(1), iso8601.parse(c.getString(2)), c.getString(3), c.getString(4), c.getString(5));
    }
    if (!c.isClosed()) {
      c.close();
    }

    return pr;
  }

  public int getPostalRecords(List<PostalRecord> list, String cod) {
    list.clear();
    Cursor c = db.query(POSTAL_RECORD_TABLE, null, "cod = ?", new String[] {cod}, null, null, "pos DESC");
    if (c.moveToFirst()) {
      do {
        try {
          list.add(new PostalRecord(c.getString(0), c.getInt(1), iso8601.parse(c.getString(2)),
              c.getString(3), c.getString(4), c.getString(5)));
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

  private static HashMap<String, String> buildColumnMap() {
    HashMap<String, String> map = new HashMap<String, String>();
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
        columns = new ArrayList<String>(Arrays.asList(c.getColumnNames()));
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

  private class CarteiroDatabase extends SQLiteOpenHelper {
    public CarteiroDatabase(Context context) {
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("CREATE TABLE IF NOT EXISTS "+POSTAL_ITEM_TABLE+" ("
          +"cod TEXT NOT NULL PRIMARY KEY, "
          +"desc TEXT, "
          +"fav BOOLEAN, "
          +"added DATETIME NOT NULL DEFAULT (DATETIME('now','localtime')))"
      );

      db.execSQL("CREATE TABLE IF NOT EXISTS "+POSTAL_RECORD_TABLE+" ("
          +"cod TEXT NOT NULL, "
          +"pos INTEGER NOT NULL, "
          +"date DATETIME NOT NULL DEFAULT (DATETIME('now','localtime')), "
          +"status TEXT NOT NULL COLLATE NOCASE, "
          +"loc TEXT, "
          +"info TEXT, "
          +"PRIMARY KEY (cod, pos) ON CONFLICT REPLACE)"
      );

      db.execSQL("CREATE VIEW IF NOT EXISTS "+POSTAL_LIST_VIEW+" AS "
          +"SELECT pi.cod AS cod, pi.desc AS desc, pr.date AS date, pr.loc AS loc, pr.info AS info, pr.status AS status, pi.fav AS fav "
          +"FROM "+POSTAL_ITEM_TABLE+" pi JOIN "+POSTAL_RECORD_TABLE+" pr ON pi.cod = pr.cod "
          +"WHERE pr.pos = ("
          +  "SELECT MAX(pr2.pos) FROM "+POSTAL_RECORD_TABLE+" pr2 WHERE pr2.cod = pi.cod"
          +") "
          +"ORDER BY pr.date DESC"
      );

      db.execSQL("CREATE TRIGGER IF NOT EXISTS "+DEL_POSTAL_RECORDS_TRIGGER+" "
          +"AFTER DELETE ON "+POSTAL_ITEM_TABLE+" "
          +"BEGIN "
          +  "DELETE FROM "+POSTAL_RECORD_TABLE+" WHERE cod = OLD.cod; "
          +"END;"
      );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      List<String> postalItemColumns, postalRecordColumns;
      String projection;

      db.beginTransaction();
      Log.i(TAG, "Upgrading database from "+oldVersion+" to "+newVersion);

      // Create everything if run for the first time
      onCreate(db);

      // Grab existing column names from data tables in order to restore later
      postalItemColumns = DatabaseHelper.GetColumns(db, POSTAL_ITEM_TABLE);
      postalRecordColumns = DatabaseHelper.GetColumns(db, POSTAL_RECORD_TABLE);

      // Backup data tables
      db.execSQL("ALTER TABLE "+POSTAL_ITEM_TABLE+" RENAME TO temp_"+POSTAL_ITEM_TABLE);
      db.execSQL("ALTER TABLE "+POSTAL_RECORD_TABLE+" RENAME TO temp_"+POSTAL_RECORD_TABLE);

      // Drop everything that can be recreated without loss
      db.execSQL("DROP VIEW IF EXISTS "+POSTAL_LIST_VIEW);
      db.execSQL("DROP TRIGGER IF EXISTS "+DEL_POSTAL_RECORDS_TRIGGER);

      // Upgrade database
      onCreate(db);

      // Get the intersection with the new columns, now taken from the upgraded tables
      postalItemColumns.retainAll(DatabaseHelper.GetColumns(db, POSTAL_ITEM_TABLE));
      postalRecordColumns.retainAll(DatabaseHelper.GetColumns(db, POSTAL_RECORD_TABLE));

      // Restore data from backup tables
      projection = TextUtils.join(", ", postalItemColumns);
      db.execSQL("INSERT INTO "+POSTAL_ITEM_TABLE+" ("+projection+") SELECT "+projection+" from temp_"+POSTAL_ITEM_TABLE);
      projection = TextUtils.join(", ", postalRecordColumns);
      db.execSQL("INSERT INTO "+POSTAL_RECORD_TABLE+" ("+projection+") SELECT "+projection+" from temp_"+POSTAL_RECORD_TABLE);

      // Drop backup tables
      db.execSQL("DROP TABLE IF EXISTS temp_"+POSTAL_ITEM_TABLE);
      db.execSQL("DROP TABLE IF EXISTS temp_"+POSTAL_RECORD_TABLE);

      db.setTransactionSuccessful();
      db.endTransaction();
    }
  }
}
