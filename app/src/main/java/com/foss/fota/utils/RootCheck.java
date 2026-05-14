package com.foss.fota.utils;

import android.content.Context;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import com.foss.fota.update.report.ReportData;
import com.google.gson.Gson;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;

public class RootCheck {
    private List<String> addedFiles;
    private List<String> modifiedFiles;
    private List<String> deletedFiles;
    private Map<String, String> expectedFilesMap;
    private RootErrJson rootErrorJson;

    public static String getFileMd5(String path) {
        FileInputStream fis = null;
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(path);
            byte[] buffer = new byte[1048576];
            while (true) {
                int read = fis.read(buffer);
                if (read == -1) {
                    break;
                }
                digest.update(buffer, 0, read);
            }
            byte[] md5Bytes = digest.digest();
            if (md5Bytes == null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
            StringBuilder sb = new StringBuilder();
            for (byte b : md5Bytes) {
                String hex = Integer.toHexString(b & 255);
                if (hex.length() == 1) {
                    sb.append("0");
                }
                sb.append(hex);
            }
            String result = sb.toString();
            try {
                fis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return result;
        } catch (Exception e2) {
            Trace.d("getFileMD5, Exception " + e2.toString());
            e2.printStackTrace();
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e3) {
                    e3.printStackTrace();
                }
            }
            return null;
        } catch (Throwable th) {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e4) {
                    e4.printStackTrace();
                }
            }
            throw th;
        }
    }

    public String checkRoot(String path) throws Exception {
        this.expectedFilesMap = new HashMap();
        boolean isZip = FileUtil.isZipFile(path);
        Trace.d("isZip = " + isZip);
        if (isZip) {
            ZipFile zipFile = null;
            try {
                zipFile = new ZipFile(path);
                ZipEntry entry = zipFile.getEntry("RC/checkroot");
                if (entry != null) {
                    parseExpectedFiles(zipFile.getInputStream(entry));
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (zipFile != null) {
                    zipFile.close();
                }
            }
        } else {
            FileUtil.extract7z(path, 0L, "7z");
            SevenZFile sevenZFile = null;
            try {
                sevenZFile = new SevenZFile(new File(path));
                while (true) {
                    SevenZArchiveEntry entry = sevenZFile.getNextEntry();
                    if (entry == null) {
                        Trace.d("entry == null");
                        ReportData.reportAction(MyApplication.getInstance(), "cause_get_entry_failed");
                        break;
                    } else if (entry.getName().equals("RC/checkroot")) {
                        byte[] content = new byte[(int) entry.getSize()];
                        sevenZFile.read(content, 0, (int) entry.getSize());
                        parseExpectedFiles(new ByteArrayInputStream(content));
                        break;
                    }
                }
                FileUtil.extract7z(path, 0L, "8p");
            } catch (Exception e2) {
                Trace.d(e2.getMessage());
                ReportData.reportAction(MyApplication.getInstance(), "cause_unzip_failed");
            } finally {
                if (sevenZFile != null) {
                    sevenZFile.close();
                }
            }
        }

        if (this.expectedFilesMap.size() == 0) {
            return "";
        }

        this.rootErrorJson = new RootErrJson();
        this.addedFiles = new ArrayList();
        this.modifiedFiles = new ArrayList();
        this.deletedFiles = new ArrayList();

        try {
            scanDirectory("/system/bin");
            scanDirectory("/system/xbin");
        } catch (Throwable th) {
            th.printStackTrace();
        }

        if (this.expectedFilesMap.size() > 0) {
            Iterator<String> it = this.expectedFilesMap.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                String value = this.expectedFilesMap.get(key);
                if (value == null || !value.equals("ignore")) {
                    this.deletedFiles.add(key);
                    this.rootErrorJson.setDelete(this.deletedFiles);
                    if (this.deletedFiles.size() > 5) {
                        break;
                    }
                }
            }
        }

        int totalChanges = this.addedFiles.size() + this.modifiedFiles.size() + this.deletedFiles.size();
        if (totalChanges > 0) {
            Gson gson = new Gson();
            String json = gson.toJson(this.rootErrorJson);
            Trace.d("root gson " + json);
            return json;
        }

        this.addedFiles = null;
        this.modifiedFiles = null;
        this.deletedFiles = null;
        this.expectedFilesMap = null;
        return "";
    }

    public String b(String path) throws Exception {
        return checkRoot(path);
    }

    private void parseExpectedFiles(InputStream is) throws IOException {
        BufferedReader reader = null;
        BufferedInputStream bis = new BufferedInputStream(is);
        try {
            reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    break;
                }
                if (line.indexOf("\t") > -1) {
                    String[] parts = line.split("\t");
                    if (parts.length > 1) {
                        String path = "/system/" + parts[0];
                        try {
                            this.expectedFilesMap.put(path, parts[1]);
                            Trace.d("checkDevicesIsRoot " + path + " md5 " + parts[1]);
                        } catch (Exception e) {
                            this.expectedFilesMap.put(path, "");
                        }
                    }
                }
            }
        } finally {
            if (bis != null) {
                bis.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    private boolean scanDirectory(String dir) throws Throwable {
        File[] files = new File(dir).listFiles();
        if (files == null) {
            return false;
        }
        for (File file : files) {
            if (checkFile(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean checkFile(File file) throws Throwable {
        String path = file.getAbsolutePath();
        if (!this.expectedFilesMap.containsKey(path)) {
            Trace.d("path " + path + " is un exist");
            this.addedFiles.add(path);
            this.rootErrorJson.setAdd(this.addedFiles);
        } else {
            String expectedMd5 = this.expectedFilesMap.get(path);
            if ("ignore".equals(expectedMd5)) {
                this.expectedFilesMap.remove(path);
            } else if (file.isDirectory()) {
                Trace.d("path " + path + " is directory");
                try {
                    Thread.sleep(5L);
                    this.expectedFilesMap.remove(path);
                    scanDirectory(path);
                } catch (Exception e) {
                }
            } else if (file.isFile()) {
                try {
                    String actualMd5 = getFileMd5(path);
                    if (TextUtils.isEmpty(actualMd5) || expectedMd5.equals(actualMd5)) {
                        this.expectedFilesMap.remove(path);
                    } else {
                        this.modifiedFiles.add(path);
                        this.rootErrorJson.setModify(this.modifiedFiles);
                    }
                } catch (Exception e2) {
                    this.expectedFilesMap.remove(path);
                }
            } else {
                this.expectedFilesMap.remove(path);
            }
        }
        return this.addedFiles.size() + this.modifiedFiles.size() > 5;
    }
}
