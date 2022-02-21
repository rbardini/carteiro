package com.rbardini.carteiro.util;

import android.os.Environment;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public final class IOUtils {
  public static final DateFormat SAFE_DATE_FORMAT = new SimpleDateFormat("yyyy.MM.dd-HH.mm.ss", Locale.getDefault());

  public static boolean isExternalStorageWritable() {
    String storageState = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(storageState);
  }

  public static boolean isExternalStorageReadable() {
    String storageState = Environment.getExternalStorageState();
    return Environment.MEDIA_MOUNTED.equals(storageState) || Environment.MEDIA_MOUNTED_READ_ONLY.equals(storageState);
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

  public static boolean isValidSQLite3File(FileInputStream stream) throws IOException {
    byte[] buffer = new byte[16];

    stream.read(buffer, 0, buffer.length);
    String header = new String(buffer, StandardCharsets.UTF_8);

    return header.equals("SQLite format 3\u0000");
  }
}
