package com.foss.fota.update.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.foss.fota.utils.MD5;

public class UpdateDBHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "ota.db";

    public static class a {
        public static final String a = "report";
        public static final String b = "CREATE TABLE IF NOT EXISTS " + a
                + " (id INTEGER PRIMARY KEY AUTOINCREMENT, type TEXT, time LONG, result TEXT )";
    }

    public UpdateDBHelper(Context context) {
        super(context, DB_NAME, (SQLiteDatabase.CursorFactory) null, 4);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onCreate(SQLiteDatabase sQLiteDatabase) {
        sQLiteDatabase.beginTransaction();
        try {
            sQLiteDatabase.execSQL(UpdateDBHelper.a.b);
            sQLiteDatabase.setTransactionSuccessful();
        } finally {
            sQLiteDatabase.endTransaction();
        }
    }

    @Override // android.database.sqlite.SQLiteOpenHelper
    public void onUpgrade(SQLiteDatabase sQLiteDatabase, int i, int i2) {
        onCreate(sQLiteDatabase);
    }

    @Override // android.database.sqlite.SQLiteOpenHelper, java.lang.AutoCloseable
    public synchronized void close() {
        super.close();
    }
}
