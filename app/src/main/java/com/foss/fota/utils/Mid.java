package com.foss.fota.utils;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.regex.Pattern;
import org.apache.commons.compress.utils.CharsetNames;

/* JADX INFO: compiled from: Mid.java */
/* JADX INFO: loaded from: classes.dex */
public class Mid {
    public static final String a = "http://www.baidu.com";
    public static final String b = "http://www.google.com";
    public static final String c = "http://www.foss.com";
    public static int d = 0;
    private static Mid instance;
    private Context context;

    private Mid(Context context) {
        this.context = context.getApplicationContext();
    }

    public static Mid getInstance(Context context) {
        if (instance == null) {
            instance = new Mid(context);
        }
        return instance;
    }

    public String getMid() {
        return a(this.context);
    }

    public static String a(Context context) {
        String strB;
        synchronized (Mid.class) {
            strB = PreferencesUtils.b(context, "mid", "");
            String strF = f(context);
            Trace.d("mid", "getSyncMid, mid = " + strB + " sd_mid = " + strF);
            if (TextUtils.isEmpty(strB) || "0".equals(strB)) {
                strB = (TextUtils.isEmpty(strF) || "0".equals(strF)) ? "" : strF;
                PreferencesUtils.putString(context, "mid", strB);
            } else if (!strB.equals(strF)) {
                b(context, strB);
            }
        }
        return strB;
    }

    private static String f(Context context) {
        return b(context);
    }

    private static void b(Context context, String str) {
        a(context, str);
    }

    public static void a(Context context, String str) {
        Trace.d("mid", "writeMID, mid = " + str);
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(new File(getMidDir(context) + ".srcMid"));
            fileOutputStream.write(str.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String b(Context context) {
        String str;
        Exception e;
        File file = new File(getMidDir(context) + "/.srcMid");
        if (!file.exists()) {
            Trace.d("mid", "readMID, mid file do not exist");
            return "";
        }
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            byte[] bArr = new byte[fileInputStream.available()];
            fileInputStream.read(bArr);
            str = new String(bArr, CharsetNames.UTF_8);
            try {
                fileInputStream.close();
                return str;
            } catch (Exception e2) {
                e = e2;
                Trace.d("mid", "readMID, Exception:" + e);
                e.printStackTrace();
                return str;
            }
        } catch (Exception e3) {
            str = "";
        }
        return str;
    }

    private static String getMidDir(Context context) {
        try {
            File base = new File(context.getFilesDir(), "fossfota");
            if (!base.exists()) {
                //noinspection ResultOfMethodCallIgnored
                base.mkdirs();
            }
            return base.getAbsolutePath();
        } catch (Exception e) {
            return "/data/data/" + context.getPackageName() + "/files/fossfota";
        }
    }

    private static String b(String str) {
        String str2;
        Exception e;
        try {
            URLConnection uRLConnectionOpenConnection = new URL(str).openConnection();
            uRLConnectionOpenConnection.setReadTimeout(15000);
            uRLConnectionOpenConnection.setConnectTimeout(15000);
            uRLConnectionOpenConnection.setDoInput(true);
            uRLConnectionOpenConnection.setDoInput(true);
            uRLConnectionOpenConnection.connect();
            str2 = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date(uRLConnectionOpenConnection.getDate()));
        } catch (Exception e2) {
            str2 = "";
            e = e2;
        }
        try {
            Trace.d("getSyncNetworkTime date = " + str2);
        } catch (Exception e3) {
            e = e3;
            Trace.d("getSyncNetworkTime e = " + e.toString());
        }
        return str2;
    }

    private static String a(String[] strArr) {
        String strB = "";
        for (String str : strArr) {
            strB = b(str);
            if (!TextUtils.isEmpty(strB)) {
                break;
            }
        }
        return strB;
    }

    private static String c(String str) {
        Random random = new Random();
        String str2 = "" + (random.nextInt(9000) + 1000);
        String str3 = "";
        char[] charArray = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toCharArray();
        for (int i = 0; i < 2; i++) {
            str3 = str3 + charArray[random.nextInt(charArray.length)] + "";
        }
        String str4 = str + str3 + str2;
        Trace.d("generateMidByDate, mid = " + str4);
        return str4;
    }

    public static boolean c(Context context) {
        String strB = PreferencesUtils.b(context, "mid", "");
        String strF = f(context);
        d = 0;
        if (!TextUtils.isEmpty(strB) || !TextUtils.isEmpty(strF)) {
            return true;
        }
        d = 1;
        return false;
    }

    private static boolean g(Context context) {
        String strC;
        String strA = "";
        int iB = PreferencesUtils.b(context, "sync_time_fail_count", 0);
        if (iB < 5) {
            strA = a(new String[]{a, b, c});
            if (TextUtils.isEmpty(strA) || a(strA)) {
                PreferencesUtils.putInt(context, "sync_time_fail_count", iB + 1);
                return false;
            }
        }
        if (strA.isEmpty()) {
            strA = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US).format(new Date());
        }
        if (strA.isEmpty()) {
            strC = "";
        } else {
            strC = c(strA);
        }
        if (!strC.isEmpty()) {
            b(context, strC);
            PreferencesUtils.putString(context, "mid", strC);
            PreferencesUtils.putInt(context, "sync_time_fail_count", 0);
            d = 1;
            return true;
        }
        d = 0;
        return false;
    }

    public static boolean a(String str) {
        return str.length() < 4 || !Pattern.compile("^\\d{4}$").matcher(str.substring(0, 4)).matches() || Integer.parseInt(str.substring(0, 4)) + (-2016) < 0;
    }

    public static boolean d(Context context) {
        return c(context) || g(context);
    }

    public static void handlePostDownloadTask(Context context) {
        if (d == 1) {
            File file = new File(getMidDir(context) + ".srcMid");
            if (file.exists()) {
                file.delete();
            }
        }
    }
}
