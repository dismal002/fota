package com.foss.fota.utils;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.storage.StorageManager;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.update.query.QueryInfo;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class StorageUtil {
    private static String upgradePath1 = null;
    private static String upgradePath2 = null;
    private static String upgradePath3 = null;
    private static double spaceMultiplier = 2.5d;

    public static void init(final Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                context.getExternalFilesDir(null);
            }
            new File(context.getFilesDir() + "/fossfota").mkdirs();
            File extFilesDir = context.getExternalFilesDir(null);
            if (extFilesDir != null) {
                new File(extFilesDir + "/fossfota").mkdirs();
                new File(extFilesDir + "/fota").mkdirs();
            }
            new File(getAndroidDataPath(context)).mkdirs();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    // Logic from Smali: StorageUtil.a(context) starts a thread to do something.
                    // Based on Java source, it was initDebugLog.
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isExternalStorageMounted(Context context) {
        String path;
        String savedPath = PreferencesUtils.getString(context, "ota_download_path", null);
        if (!TextUtils.isEmpty(savedPath) && savedPath.contains("#")) {
            String[] parts = savedPath.split("#");
            if (parts.length == 3) {
                setUpgradePaths(parts[0], parts[1], parts[2]);
            }
        }
        if (upgradePath1 != null && upgradePath1.length() > 0 && !upgradePath1.equals("null")) {
            path = getStorageState(context, upgradePath1);
        } else {
            path = getDownloadDirState(context);
        }
        Trace.d("externalStorageState = " + path);
        return "mounted".equals(path);
    }

    public static int checkStorage(Context context, long fileSize) {
        try {
            VersionModel version = QueryInfo.getInstance(context).getVersionModel();
            spaceMultiplier = (version != null ? version.getIsOldPkg() : 0) == 0 ? 2.5d : 1.0d;
            return getStorageStatus(context, (long) (spaceMultiplier * fileSize));
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static int getStorageStatus(Context context, long requiredSize) {
        long needed = getRequiredSpace(context, requiredSize);
        Trace.d("size = " + needed);
        if (needed == 0) {
            return 3; // Enough space
        }
        if (needed == -1) {
            return 1; // Card mount error or very low space
        }
        return 2; // Not enough space
    }

    private static long getRequiredSpace(Context context, long miniSize) {
        try {
            StatFs statFs = new StatFs(getDownloadDirFile(context).getPath());
            long available = (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
            File updateFile = new File(getUpdatePackagePath(context));
            if (updateFile.exists()) {
                miniSize -= updateFile.length();
            }
            Trace.d("totalSize = " + available + "; miniSize = " + miniSize);
            if (available < miniSize) {
                return miniSize - available;
            }
            return available > 10 + miniSize ? 0L : -1L;
        } catch (Exception e) {
            Trace.e("card mount error");
            return -1L;
        }
    }

    private static File getDownloadDirFile(Context context) {
        try {
            return new File(getDownloadPath(context, true));
        } catch (Throwable throwable) {
            return new File("/sdcard");
        }
    }

    private static String getDownloadDirState(Context context) {
        return getStorageState(context, getDownloadDirFile(context).toString());
    }

    public static String getStorageState(Context context, String path) {
        Trace.d("path = " + path);
        try {
            StatFs statFs = new StatFs(new File(path).getPath());
            if ((long) statFs.getBlockCount() * (long) statFs.getBlockSize() < 10485760) {
                return "removed";
            }
            if (path.contains(Environment.getExternalStorageDirectory().getAbsolutePath())) {
                Trace.d("Environment.getExternalStorageState = " + Environment.getExternalStorageState());
                return Environment.getExternalStorageState();
            }
            if (!path.startsWith("/data/data") && testStorageWrite(context, path)) {
                return "mounted";
            }
            return "mounted";
        } catch (Exception e) {
            Trace.d(e.toString());
            if (Build.VERSION.SDK_INT < 23) {
                return "removed";
            }
            return "unmounted";
        }
    }

    public static String getBestStorageRoot(Context context, boolean verbose) {
        String path;
        boolean p1Exists = upgradePath1 != null && upgradePath1.length() > 0 && !upgradePath1.equals("null");
        boolean p2Exists = upgradePath2 != null && upgradePath2.length() > 0 && !upgradePath2.equals("null");
        boolean p3Exists = upgradePath3 != null && upgradePath3.length() > 0 && !upgradePath3.equals("null");

        Trace.d("download_path_server : " + QueryInfo.getInstance(context).getPolicy("download_path_server", Integer.class));

        if (p1Exists && "mounted".equals(getStorageState(context, upgradePath1))) {
            path = upgradePath1;
        } else if (p2Exists && "mounted".equals(getStorageState(context, upgradePath2))) {
            path = upgradePath2;
        } else if (p3Exists && "mounted".equals(getStorageState(context, upgradePath3))) {
            path = upgradePath3;
        } else {
            path = getDefaultStorageRoot(context);
        }
        if (verbose) {
            Trace.d("sdcard = " + path);
        }
        return path;
    }

    public static String getDownloadPath(Context context, boolean verbose) {
        return getBestStorageRoot(context, verbose);
    }

    private static String getDefaultStorageRoot(Context context) {
        int pathType;
        if (Build.VERSION.SDK_INT >= 21) {
            if (isDoubleSdcard(context)) {
                return context.getFilesDir().toString();
            }
            try {
                pathType = ((Integer) QueryInfo.getInstance(context).getPolicy("download_path_server", Integer.class)).intValue();
            } catch (Exception e) {
                pathType = 0;
            }
            if (pathType == 1) {
                return getInternalStoragePath(context);
            }
            if (pathType == 2) {
                return context.getFilesDir().toString();
            }
            String internalPath = getInternalStoragePath(context);
            if (TextUtils.isEmpty(internalPath)) {
                return context.getFilesDir().toString();
            }
            return internalPath;
        }
        String extStorage = Environment.getExternalStorageDirectory().getAbsolutePath();
        List<StorageVolumeInfo> volumes = getVolumeList();
        if (volumes != null && volumes.size() > 0) {
            for (int i = 0; i < volumes.size(); i++) {
                StorageVolumeInfo info = volumes.get(i);
                if (info != null) {
                    String infoPath = info.path;
                    if (!infoPath.equals(extStorage) && "mounted".equals(getStorageState(context, infoPath)) && isStorageWritable(context, infoPath)) {
                        return infoPath;
                    }
                }
            }
        }
        return extStorage;
    }



    public static String getUpdateStatus(Context context) {
        return getUpdatePackageDir(context);
    }

    public static String getLogPath(Context context) {
        String path = getInternalFilesPath(context);
        if (path == null) {
            File extFiles = context.getExternalFilesDir(null);
            if (extFiles != null) {
                path = extFiles.getAbsolutePath();
            }
        }
        Trace.d("self catchLogPath : " + path);
        return path;
    }

    private static String getInternalFilesPath(Context context) {
        String subPath = "/Android/data/" + context.getPackageName() + "/files";
        String path = getVolumePath(context, false) + subPath;
        Trace.d("self innerPath : " + path);
        if (TextUtils.isEmpty(path) || !testStorageWrite(context, path)) {
            path = getVolumePath(context, true) + subPath;
            Trace.d("self outPath : " + path);
        }
        return path;
    }

    public static String getAndroidDataPath(Context context) {
        return "/sdcard/Android/";
    }

    public static File getLatestLogFile(Context context) {
        try {
            String logPath = "/Android/data/" + context.getPackageName() + "/errLog/last.log";
            String root1 = getBestStorageRoot(context, true);
            String root2 = getBestStorageRoot(context, false);
            File file1 = new File(root1 + logPath);
            if (TextUtils.isEmpty(root1) || !file1.exists()) {
                File file2 = new File(root2 + logPath);
                if (!TextUtils.isEmpty(root2)) {
                    if (file2.exists()) {
                        return file2;
                    }
                }
            } else {
                return file1;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File getLastLogFile(Context context) {
        return getLatestLogFile(context);
    }

    public static String getMatchingStoragePath(Context context, String path) {
        if (TextUtils.isEmpty(path)) return "";
        String extPath = getVolumePath(context, true);
        String intPath = getVolumePath(context, false);
        if (extPath != null && path.startsWith(extPath)) return extPath;
        if (intPath != null && path.startsWith(intPath)) return intPath;
        return "";
    }

    public static boolean testStorageWrite(Context context, String path) {
        try {
            if (TextUtils.isEmpty(path) || !hasEnoughSpace(context, path)) {
                Trace.d("invalid path " + path);
                return false;
            }
            File testDir = new File(path, "test");
            if (testDir.exists()) testDir.delete();
            if (testDir.mkdirs() || testDir.mkdir()) {
                File testFile = new File(testDir, "test.txt");
                if (testFile.createNewFile()) {
                    testFile.delete();
                    testDir.delete();
                    Trace.d("sdcard = " + path);
                    return true;
                }
            }
            Trace.d("mkdirs or createNewFile failed!!");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static boolean isStorageWritable(Context context, String path) {
        return testStorageWrite(context, path);
    }

    private static boolean hasEnoughSpace(Context context, String path) {
        try {
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) return false;
            StatFs statFs = new StatFs(file.getPath());
            long available = (long) statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
            VersionModel version = QueryInfo.getInstance(context).getVersionModel();
            long needed = version != null ? version.getFilesize() : 0L;
            Trace.d("availableBlock = " + available + "; needSize = " + needed);
            return needed == 0 || (double) available > (double) needed * spaceMultiplier;
        } catch (Exception e) {
            Trace.e(path + " card mount error");
            return false;
        }
    }

    public static void setUpgradePaths(String p1, String p2, String p3) {
        if (p1 != null && !p1.equals("null")) upgradePath1 = p1;
        if (p2 != null && !p2.equals("null")) upgradePath2 = p2;
        if (p3 != null && !p3.equals("null")) upgradePath3 = p3;
        Trace.d("upgradePath1 = " + upgradePath1 + ", upgradePath2 = " + upgradePath2 + ", upgradePath3 = " + upgradePath3);
    }

    public static boolean hasUpgradePaths() {
        return !TextUtils.isEmpty(upgradePath1) || !TextUtils.isEmpty(upgradePath2) || !TextUtils.isEmpty(upgradePath3);
    }

    public static String getUpdatePackageDir(Context context) {
        int status = PreferencesUtils.getInt(context, "ota_update_status", 0);
        String path = PreferencesUtils.getString(context, "update_package_path", "");
        try {
            if ((status == 4 || status == 6) && !TextUtils.isEmpty(path)) {
                return path;
            }
            return getBestStorageRoot(context, true) + "/fossfota";
        } catch (Throwable t) {
            return "/sdcard/fossfota";
        }
    }

    public static String getUpdatePackagePath(Context context) {
        String path = getUpdatePackageDir(context) + "/update.zip";
        Trace.d("filename = " + path);
        return path;
    }

    public static boolean deleteFileRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    if (!deleteFileRecursive(child)) return false;
                }
            }
        }
        return file.delete();
    }

    public static void deleteUpdatePackage(Context context) {
        try {
            String path = getUpdatePackageDir(context);
            Trace.d("filePath = " + path);
            deleteBinFiles(getBestStorageRoot(context, false));
            deleteFileRecursive(new File(path));
            clearDataPackage();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static void clearDataPackage() {
        try {
            File f1 = new File("/data/data/com.foss.fota/fossfota/update.zip");
            if (f1.exists()) f1.delete();
            File f2 = new File("/data/data/com.foss.fota/update.zip");
            if (f2.exists()) f2.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void deleteBinFiles(String path) {
        String[] bins = {"/modem.bin", "/nvitem.bin", "/dsp.bin", "/vmjaluna.bin"};
        for (String bin : bins) {
            File file = new File(path + bin);
            if (file.exists()) {
                Trace.d("delete " + file);
                file.delete();
            }
        }
    }

    public static String getVolumePath(Context context, boolean isRemovable) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        try {
            Method getVolumeList = sm.getClass().getMethod("getVolumeList");
            Object[] volumes = (Object[]) getVolumeList.invoke(sm);
            for (Object volume : volumes) {
                Method getPath = volume.getClass().getMethod("getPath");
                Method isRemovableMethod = volume.getClass().getMethod("isRemovable");
                String path = (String) getPath.invoke(volume);
                boolean removable = (Boolean) isRemovableMethod.invoke(volume);
                if (isRemovable == removable && !path.startsWith("/dev/null")) {
                    return path;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static List<StorageVolumeInfo> getVolumeList() {
        return getStorageVolumes();
    }

    public static List<StorageVolumeInfo> getStorageVolumes() {
        List<StorageVolumeInfo> volumes = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader("/proc/mounts"));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("vfat") || line.contains("/mnt")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length > 1) {
                        volumes.add(new StorageVolumeInfo(parts[1], false, false, 0));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (reader != null) try { reader.close(); } catch (IOException e) {}
        }
        return volumes;
    }

    private static String getInternalStoragePath(Context context) {
        String path = PreferencesUtils.getString(MyApplication.getInstance(), "CustomDtPath", "");
        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
            String volume = getVolumePath(context, true);
            if (volume != null) {
                if (Build.VERSION.SDK_INT >= 23) {
                    volume += "/Android/data/" + context.getPackageName() + "/files";
                }
                if (testStorageWrite(context, volume)) {
                    PreferencesUtils.putString(MyApplication.getInstance(), "CustomDtPath", volume);
                    return volume;
                }
            }
            return "";
        }
        return path;
    }

    public static boolean isDoubleSdcard(Context context) {
        StorageManager sm = (StorageManager) context.getSystemService(Context.STORAGE_SERVICE);
        if (sm == null) return false;
        try {
            Method getVolumes = sm.getClass().getMethod("getVolumes");
            List<?> volumes = (List<?>) getVolumes.invoke(sm);
            for (Object vol : volumes) {
                int type = (Integer) vol.getClass().getMethod("getType").invoke(vol);
                int state = (Integer) vol.getClass().getMethod("getState").invoke(vol);
                if (type == 0 && state == 2) { // PUBLIC && MOUNTED
                    Object disk = vol.getClass().getMethod("getDisk").invoke(vol);
                    if (disk != null) {
                        int count = disk.getClass().getField("volumeCount").getInt(disk);
                        if (count > 1) return true;
                    }
                }
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static class StorageVolumeInfo {
        public final String path;
        public final boolean isRemovable;
        public final boolean isEmulated;
        public final int status;

        public StorageVolumeInfo(String path, boolean isRemovable, boolean isEmulated, int status) {
            this.path = path;
            this.isRemovable = isRemovable;
            this.isEmulated = isEmulated;
            this.status = status;
        }
    }

    // ---- Readable helpers ----
    public static boolean isOnRemovableStorage(Context context, String path) {
        if (context == null || TextUtils.isEmpty(path)) return false;
        String removable = getVolumePath(context, true);
        return !TextUtils.isEmpty(removable) && path.startsWith(removable);
    }

    // ---- Compatibility aliases from older decompiled sources ----
    public static void a(Context context) { init(context); }

    public static void setDownloadPath(String p1, String p2, String p3) { setUpgradePaths(p1, p2, p3); }
    public static void setDownloadPath(Context context, String path) { PreferencesUtils.putString(context, "ota_download_path", path); }
    public static String getDownloadPath(Context context) { return getDownloadDirFile(context).getAbsolutePath(); }
    public static File getLogFile(Context context) { return getLatestLogFile(context); }
    public static String getUpdatePackageFile(Context context) { return getUpdatePackagePath(context); }
    public static String getUpdatePackageFile(Context context, boolean removable) { return getUpdatePackagePath(context); }

    public static void a(String p1, String p2, String p3) { setUpgradePaths(p1, p2, p3); }
    public static int a(Context context, long fileSize) { return checkStorage(context, fileSize); }
    public static String a(Context context, boolean removable) { return getBestStorageRoot(context, removable); }
    public static String a(Context context, String path) { return getStorageState(context, path); }

    public static int b(Context context, long requiredSize) { return getStorageStatus(context, requiredSize); }
    public static String b(Context context, boolean removable) { return getBestStorageRoot(context, removable); }
    public static String b(Context context, String path) { return getMatchingStoragePath(context, path); }

    public static List<StorageVolumeInfo> getStatus() { return getStorageVolumes(); }

    public static String popButton(Context context) { return getUpdatePackagePath(context); }

    // Older call sites expect f() to represent "current update package dir" and deletion helpers.
    public static String f(Context context) { return getUpdatePackageDir(context); }

    public static void f(Context context, String path) {
        try {
            if (!TextUtils.isEmpty(path)) {
                deleteFileRecursive(new File(path));
            }
        } catch (Exception ignored) {
        }
    }

    // Additional compatibility aliases
    public static void performStartupCheck(Context context) { init(context); }

    public static String g(Context context) { return getUpdatePackagePath(context); }

    public static void clearDownloadDir(Context context, String path) {
        if (TextUtils.isEmpty(path)) return;
        deleteFileRecursive(new File(path));
    }
    public static boolean isStorageSpaceEnough(Context context, long requiredSize) {
        return getStorageStatus(context, requiredSize) == 3;
    }

    public static void init(String path) {
        // No-op or log if needed
    }

    public static boolean hasExternalSdCard(Context context) {
        String path = getVolumePath(context, true);
        return !TextUtils.isEmpty(path) && !path.equals("null");
    }
}
