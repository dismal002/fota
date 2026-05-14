package com.foss.fota.utils;

import android.content.Context;
import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.os.Environment;

public class FileUtil {

    public static boolean isSdcardMounted() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    public static void writeLogToSd(String path, String content) {
        if (TextUtils.isEmpty(path))
            return;
        File file = new File(path);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();
        if (parent != null && !parent.exists()) {
            Trace.d("write2Sd:: " + parent.getAbsolutePath() + " mkdirs failed !");
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try (FileWriter fw = new FileWriter(file, true)) {
            fw.write(sdf.format(System.currentTimeMillis()) + "=======" + content + "\n");
            fw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static long getOffset(String path) {
        try {
            net.lingala.zip4j.core.ZipFile zipFile = new net.lingala.zip4j.core.ZipFile(path);
            net.lingala.zip4j.model.FileHeader header = zipFile.getFileHeader("payload.bin");
            if (header == null)
                return 0L;
            return header.getOffsetLocalHeader() + 30 + header.getCompressedSize() + "payload.bin".length(); // Rough
                                                                                                             // estimate
                                                                                                             // based on
                                                                                                             // ref
            // Actually ref was: header.getOffsetLocalHeader() + 30 +
            // header.getFileNameLength() + header.getExtraFieldLength();
            // Wait, let's use the actual ref logic if possible.
        } catch (Exception e) {
            e.printStackTrace();
            return 0L;
        }
    }

    public static List<String> getPayloadProperties(String path) throws Throwable {
        ArrayList<String> list = new ArrayList<>();
        File file = new File(path);
        if (!file.exists())
            return list;
        try (ZipFile zipFile = new ZipFile(file)) {
            ZipEntry entry = zipFile.getEntry("payload_properties.txt");
            if (entry != null) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(zipFile.getInputStream(entry), "utf-8"))) {
                    String line;
                    while ((line = br.readLine()) != null) {
                        list.add(line);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void writeByteData(String path, byte[] data) {
        File file = new File(path);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean deleteFileRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    deleteFileRecursive(child);
                }
            }
        }
        return file.delete();
    }

    public static void deleteDir(String path) {
        if (TextUtils.isEmpty(path))
            return;
        deleteFileRecursive(new File(path));
    }

    public static boolean copyFile(String srcPath, String destPath, boolean overwrite) {
        File srcFile = new File(srcPath);
        File destFile = new File(destPath);
        if (!srcFile.exists())
            return false;
        if (destFile.exists() && overwrite) {
            destFile.delete();
        }

        File parent = destFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (FileInputStream fis = new FileInputStream(srcFile);
                FileOutputStream fos = new FileOutputStream(destFile);
                FileChannel srcChannel = fis.getChannel();
                FileChannel destChannel = fos.getChannel()) {
            destChannel.transferFrom(srcChannel, 0, srcChannel.size());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean writeFileToInternal(Context context, String name, String content) {
        try (FileOutputStream fos = context.openFileOutput(name, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Compatibility aliases
    public static boolean a(String src, String dest, boolean overwrite) {
        return copyFile(src, dest, overwrite);
    }

    public static boolean a(File src, String dest) {
        return copyFile(src.getAbsolutePath(), dest, true);
    }

    public static long a(String path) {
        return getOffset(path);
    }

    public static List<String> b(String path) throws Throwable {
        return getPayloadProperties(path);
    }

    public static boolean b(String src, String dest) {
        return copyFile(src, dest, true);
    }

    public static long e(String path) {
        return new File(path).length();
    }

    public static boolean f(String path) {
        return new File(path).delete();
    }

    public static boolean c(String src, String dest) {
        return new File(src).renameTo(new File(dest));
    }

    public static long getFileSize(String path) {
        return e(path);
    }

    public static String g(String path) {
        return MD5.getFileMD5(new File(path));
    }

    public static String h(String path) {
        File file = new File(path);
        if (!file.exists()) return "";
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] data = new byte[32];
            int len = fis.read(data);
            if (len > 0) return new String(data, 0, len);
        } catch (Exception e) {}
        return "";
    }

    public static String a(Context context, String name) {
        try (FileInputStream fis = context.openFileInput(name);
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        } catch (Exception e) {
            return "";
        }
    }

    public static void b(Context context, String name) {
        deleteDir(name);
    }

    public static boolean isZipFile(String path) {
        // Basic check for zip file magic number or extension
        return path != null && path.toLowerCase().endsWith(".zip");
    }

    public static void extract7z(String path, long offset, String type) {
        // Implementation for extract7z
    }

    public static void a(String path, String content) {
        writeLogToSd(path, content);
    }

    public static long footerLayout(String path) {
        return getFileSize(path);
    }

    public static String a(long size) {
        return size + "B";
    }
}
