package com.foss.fota.update.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import com.foss.fota.update.model.ReportModel;
import java.util.ArrayList;
import java.util.List;
import android.database.Cursor;

/* JADX INFO: compiled from: UpdateDBAdapter.java */
/* JADX INFO: loaded from: classes.dex */
public class UpdateDBAdapter {
    private static UpdateDBAdapter instance = null;
    private UpdateDBHelper dbHelper;
    private SQLiteDatabase db;

    private UpdateDBAdapter(Context context) {
        this.dbHelper = new UpdateDBHelper(context);
        this.db = this.dbHelper.getWritableDatabase();
    }

    public static UpdateDBAdapter getInstance(Context context) {
        if (instance == null) {
            synchronized (UpdateDBAdapter.class) {
                if (instance == null) {
                    instance = new UpdateDBAdapter(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public boolean insertReport(String type, String result) {
        synchronized (this) {
            try {
                if (!TextUtils.isEmpty(result) && !TextUtils.isEmpty(type)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("type", type);
                    contentValues.put("result", result);
                    contentValues.put("time", Long.valueOf(System.currentTimeMillis()));
                    return this.db.insert("report", null, contentValues) >= 0;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean clearReports(List<ReportModel.ReportResult> list) {
        synchronized (this) {
            try {
                // The original code seems to ignore the list and just clear everything
                this.db.delete("report", null, null);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public List<ReportModel.ReportResult> queryReports(int limit) {
        synchronized (this) {
            ArrayList<ReportModel.ReportResult> results = new ArrayList<>();
            int max = limit <= 0 ? 20 : limit;
            Cursor cursor = null;
            try {
                cursor = this.db.query("report", null, null, null, null, null, null, null);
                if (cursor != null && cursor.getCount() > 0) {
                    int count = 0;
                    while (cursor.moveToNext() && count < max) {
                        ReportModel.ReportResult report = new ReportModel.ReportResult();
                        report.action = cursor.getString(cursor.getColumnIndex("type"));
                        report.result = cursor.getString(cursor.getColumnIndex("result"));
                        report.time = cursor.getLong(cursor.getColumnIndex("time"));
                        results.add(report);
                        count++;
                    }
                }
                return results;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return null;
    }
}
