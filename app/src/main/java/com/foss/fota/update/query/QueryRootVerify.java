package com.foss.fota.update.query;

import android.text.TextUtils;
import com.foss.fota.utils.RootErrJson;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.JsonTools;
import com.foss.fota.utils.RootCheck;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.CRC32;
import java.util.zip.CheckedInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/* JADX INFO: compiled from: QueryRootVerify.java */
/* JADX INFO: loaded from: classes.dex */
public class QueryRootVerify {
    public static String a(String str) {
        try {
            String strB = new com.foss.fota.utils.RootCheck().b(str);
            if (TextUtils.isEmpty(strB)) {
                return b(str);
            }
            return strB;
        } catch (Exception e) {
            return null;
        }
    }

    public static String b(String str) {
        try {
            return c(str);
        } catch (FileNotFoundException e) {
            Trace.e("QueryRootVerify", "checkUpdateFileResult, FileNotFoundException = " + e.toString());
            return null;
        } catch (IOException e2) {
            Trace.e("QueryRootVerify", "checkUpdateFileResult, IOException = " + e2.toString());
            return null;
        }
    }

    private static String c(String str) throws IOException {
        int length;
        int iIndexOf;
        new StringBuffer();
        RootErrJson rootErrJson = new RootErrJson();
        ArrayList arrayList = new ArrayList();
        Trace.d("QueryRootVerify", "start checkSourceFile");
        ZipInputStream zipInputStream = new ZipInputStream(new CheckedInputStream(new FileInputStream(str), new CRC32()));
        BufferedInputStream bufferedInputStream = new BufferedInputStream(zipInputStream);
        byte[] bArr = new byte[1024];
        while (true) {
            ZipEntry nextEntry = zipInputStream.getNextEntry();
            if (nextEntry == null) {
                break;
            }
            if (nextEntry.getName().equals("META-INF/com/google/android/updater-script")) {
                StringBuffer stringBuffer = new StringBuffer();
                for (int i = bufferedInputStream.read(bArr); i != -1; i = bufferedInputStream.read(bArr)) {
                    stringBuffer.append(new String(bArr, 0, i));
                }
                int length2 = 0;
                while (true) {
                    int iIndexOf2 = stringBuffer.indexOf("apply_patch_check(\"/system", length2);
                    if (iIndexOf2 < 0 || (iIndexOf = stringBuffer.indexOf("\")", (length = iIndexOf2 + "apply_patch_check(\"/system".length()))) < 0) {
                        break;
                    }
                    String str2 = new String(stringBuffer.substring(length, iIndexOf));
                    length2 = "\");".length() + iIndexOf;
                    int iIndexOf3 = str2.indexOf("\", \"", 0);
                    if (iIndexOf3 >= 0) {
                        String str3 = new String(str2.substring(0, iIndexOf3));
                        int length3 = iIndexOf3 + "\", \"".length();
                        int iIndexOf4 = str2.indexOf("\", \"", length3);
                        if (iIndexOf4 >= 0) {
                            String str4 = new String(str2.substring(length3, iIndexOf4));
                            String str5 = new String(str2.substring("\", \"".length() + iIndexOf4, str2.length()));
                            Trace.d("QueryRootVerify", "file path = " + str3 + "  sha1_str1 = " + str4 + "  sha1_str2 = " + str5);
                            try {
                                if (!a(str3, str4, str5)) {
                                    arrayList.add(str3.substring(str3.lastIndexOf("/") + 1));
                                }
                            } catch (Exception e) {
                                Trace.e("QueryRootVerify", "checkSourceFileInvaild, Exception = " + e.toString());
                            }
                        }
                    }
                }
            }
        }
        bufferedInputStream.close();
        if (arrayList.size() > 0) {
            rootErrJson.setModify(arrayList);
            return JsonTools.a(rootErrJson);
        }
        return null;
    }

    /* JADX WARN: Removed duplicated region for block: B:19:0x00db  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static boolean a(java.lang.String r6, java.lang.String r7, java.lang.String r8) throws java.io.IOException, java.lang.OutOfMemoryError {
        /*
            Method dump skipped, instruction units count: 264
            To view this dump change 'Code comments level' option to 'DEBUG'
        */
        throw new UnsupportedOperationException("Method not decompiled: com.foss.fota.update.query.QueryRootVerify.getInstance(java.lang.String, java.lang.String, java.lang.String):boolean");
    }

    private static String a(byte b) {
        char[] cArr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        return new String(new char[]{cArr[(b >>> 4) & 15], cArr[b & 15]});
    }

    public static String a(byte[] bArr) {
        String str = "";
        for (byte b : bArr) {
            str = str + a(b);
        }
        return str;
    }
}
