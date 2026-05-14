package com.foss.fota;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

/* JADX INFO: loaded from: classes.dex */
public class MyContentProvider extends ContentProvider {
    private UriMatcher a;

    @Override // android.content.ContentProvider
    public boolean onCreate() {
        this.a = new UriMatcher(-1);
        this.a.addURI("com.foss.fota.MyContentProvider", "reject_status", 1);
        return false;
    }

    @Override // android.content.ContentProvider
    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        return null;
    }

    @Override // android.content.ContentProvider
    public String getType(Uri uri) {
        return null;
    }

    @Override // android.content.ContentProvider
    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    @Override // android.content.ContentProvider
    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    @Override // android.content.ContentProvider
    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        switch (this.a.match(uri)) {
            case 1:
                MyApplication.setRejectStatus(contentValues.getAsBoolean("reject_status").booleanValue());
                MyApplication.updatePrivacyStatus();
                break;
        }
        return 0;
    }
}
