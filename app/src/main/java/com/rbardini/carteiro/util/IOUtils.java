package com.rbardini.carteiro.util;

import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class IOUtils {
  public static final DateFormat SAFE_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss", Locale.getDefault());
  private static final String DOCUMENTS_DIR = "Documents";
  private static final String APP_DIR = "Carteiro";

  public static boolean isExternalStorageWritable() {
    String storageState = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(storageState);
  }

  public static boolean isExternalStorageReadable() {
    String storageState = Environment.getExternalStorageState();
    return (Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState));
  }

  public static File getExternalStoragePublicAppDocumentsDirectory() {
    File documentsDir = new File(Environment.getExternalStoragePublicDirectory(DOCUMENTS_DIR), APP_DIR);
    if (!documentsDir.isDirectory()) documentsDir.mkdirs();

    return documentsDir;
  }

  public static File createFile(File destDir, String fileName) {
    return new File(destDir, fileName);
  }

  public static void copyFile(FileInputStream fromFile, FileOutputStream toFile) throws IOException {
    FileChannel fromChannel = null, toChannel = null;

    try {
      fromChannel = fromFile.getChannel();
      toChannel = toFile.getChannel();
      fromChannel.transferTo(0, fromChannel.size(), toChannel);
    } finally {
      try {
        if (fromChannel != null) fromChannel.close();
      } finally {
        if (toChannel != null) toChannel.close();
      }
    }
  }
}
