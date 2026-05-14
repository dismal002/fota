package com.foss.fota.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import java.io.FileReader;
import java.io.Reader;
import java.io.LineNumberReader;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

/* JADX INFO: compiled from: DeviceInfoProvider.java */
/* JADX INFO: loaded from: classes.dex */
public class DeviceInfoProvider {
    private static DeviceInfoProvider instance = null;
    private TelephonyManager telephonyManager;
    private boolean isPrivacyMode = false;

    private DeviceInfoProvider(Context context) {
        try {
            String imei1 = "";
            String imei2 = "";
            if (!this.isPrivacyMode) {
                if (!DeviceUtil.getInstance().getImei1().isEmpty()) {
                    imei1 = DeviceUtil.getInstance().getImei1();
                } else if (!DeviceUtil.getInstance().getDeviceIdFromSystem().isEmpty()) {
                    imei1 = DeviceUtil.getInstance().getDeviceIdFromSystem();
                }
                if (!DeviceUtil.getInstance().getImei2().isEmpty()) {
                    imei2 = DeviceUtil.getInstance().getImei2();
                } else if (!DeviceUtil.getInstance().getDeviceIdFromSystem().isEmpty()) {
                    imei2 = DeviceUtil.getInstance().getDeviceIdFromSystem();
                }
            }
            initImei(context, imei1, imei2);
            this.telephonyManager = (TelephonyManager) context.getSystemService("phone");
        } catch (Exception e) {
            Trace.d(e.getMessage());
        }
    }

    public static DeviceInfoProvider getInstance(Context context) {
        if (instance == null) {
            synchronized (DeviceInfoProvider.class) {
                if (instance == null) {
                    instance = new DeviceInfoProvider(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public static DeviceInfoProvider a(Context context) {
        return getInstance(context);
    }

    public static void initImei(Context context, String imei1, String imei2) {
        String savedImei1 = PreferencesUtils.getString(context, "device_imei", "");
        String savedImei2 = PreferencesUtils.getString(context, "device_minor_imei", "");
        String[] imeis = {imei1, imei2};
        if (TextUtils.isEmpty(imei2)) {
            if (!TextUtils.isEmpty(imei1)) {
                PreferencesUtils.putString(context, "device_imei", imei1);
                return;
            }
            return;
        }
        if (TextUtils.isEmpty(savedImei1) && TextUtils.isEmpty(savedImei2)) {
            long max = getMaxImei(imeis);
            long min = getMinImei(imeis);
            PreferencesUtils.putString(context, "device_imei", getImeiByValue(imeis, max));
            PreferencesUtils.putString(context, "device_minor_imei", getImeiByValue(imeis, min));
            return;
        }
        long val1 = !TextUtils.isEmpty(savedImei1) ? Long.parseLong(StringUtils.stripNonDigits(savedImei1)) : 0L;
        long val2 = !TextUtils.isEmpty(savedImei2) ? Long.parseLong(StringUtils.stripNonDigits(savedImei2)) : 0L;
        long maxNew = getMaxImei(imeis);
        long minNew = getMinImei(imeis);
        if (Math.abs(val1 - maxNew) <= 0 || Math.abs(val2 - minNew) <= 0) {
            return;
        }
        if (maxNew != 0) {
            PreferencesUtils.putString(context, "device_imei", getImeiByValue(imeis, maxNew));
        }
        if (minNew != 0) {
            PreferencesUtils.putString(context, "device_minor_imei", getImeiByValue(imeis, minNew));
        }
    }

    public static String getImeiByValue(String[] imeis, long value) {
        for (String imei : imeis) {
            try {
                if (value == Long.parseLong(StringUtils.stripNonDigits(imei))) {
                    return imei;
                }
            } catch (Exception e) {
            }
        }
        return null;
    }

    public static long getMinImei(String[] imeis) {
        long min = 0;
        boolean first = true;
        for (String imei : imeis) {
            try {
                long val = Long.parseLong(StringUtils.stripNonDigits(imei));
                if (first) {
                    min = val;
                    first = false;
                } else {
                    min = Math.min(min, val);
                }
            } catch (Exception e) {
            }
        }
        return min;
    }

    public static long getMaxImei(String[] imeis) {
        long max = 0;
        boolean first = true;
        for (String imei : imeis) {
            try {
                long val = Long.parseLong(StringUtils.stripNonDigits(imei));
                if (first) {
                    max = val;
                    first = false;
                } else {
                    max = Math.max(max, val);
                }
            } catch (Exception e) {
            }
        }
        return max;
    }

    public String getImei1(Context context) {
        return PreferencesUtils.getString(context, "device_imei", "");
    }

    public String getImei2(Context context) {
        String imei2 = PreferencesUtils.getString(context, "device_minor_imei", "");
        if (imei2.equalsIgnoreCase(PreferencesUtils.getString(context, "device_imei", ""))) {
            return "";
        }
        return imei2;
    }

    public String getSerialNumber(Context context) {
        return (this.isPrivacyMode || Build.VERSION.SDK_INT <= 23) ? Build.SERIAL : DeviceUtil.getInstance().getSerial();
    }

    public String getDeviceId(Context context) {
        String deviceId;
        String type = DeviceUtil.getInstance().getDeviceType();
        if ("pad".equalsIgnoreCase(type) || "tv".equalsIgnoreCase(type) || "box".equalsIgnoreCase(type)) {
            deviceId = getSerialNumber(context);
        } else if ("phone".equalsIgnoreCase(type) || "pad_phone".equalsIgnoreCase(type)) {
            deviceId = getImei1(context);
        } else {
            deviceId = getSerialNumber(context);
        }
        String preferredIdType = DeviceUtil.getInstance().getPreferredIdType();
        if (!TextUtils.isEmpty(preferredIdType)) {
            if ("sn".equals(preferredIdType)) {
                return getSerialNumber(context);
            }
            if ("mac".equals(preferredIdType)) {
                return getMacAddress(context);
            }
            if ("imei".equals(preferredIdType)) {
                return getImei1(context);
            }
        }
        return deviceId;
    }

    public String getSimOperatorName(Context context) {
        return this.telephonyManager.getSimOperatorName();
    }

    public String getSimOperator() {
        return this.telephonyManager.getSimOperator();
    }
    
    public String getOperator1(Context context) {
        return getSimOperator();
    }

    public String getOperator2(Context context) {
        return "";
    }
    
    public String getGid1(Context context) { return ""; }
    public String getGid2(Context context) { return ""; }
    public String getSpn1(Context context) { return ""; }
    public String getSpn2(Context context) { return ""; }

    public int getApnType(Context context) {
        try {
            NetworkInfo info = ((ConnectivityManager) context.getSystemService("connectivity")).getActiveNetworkInfo();
            if (info == null) {
                return -1;
            }
            if (info.getType() == 0) {
                return this.telephonyManager.getNetworkType();
            }
            return info.getType() == 1 ? -2 : -1;
        } catch (Exception e) {
            Trace.d("exception = " + e.toString());
            return -1;
        }
    }

    public String getMacAddress(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= 23) {
                return getWlan0MacAddress();
            }
            android.net.wifi.WifiManager wifi = (android.net.wifi.WifiManager) context.getSystemService("wifi");
            return wifi.getConnectionInfo().getMacAddress();
        } catch (Exception e) {
            return "ff:ff:ff:ff:ff:ff";
        }
    }

    private String getWlan0MacAddress() {
        String address = "";
        try {
            Process process = Runtime.getRuntime().exec("cat /sys/class/net/wlan0/address ");
            LineNumberReader reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));
            String line = reader.readLine();
            if (line != null) {
                address = line.trim();
            }
        } catch (Exception e) {
        }
        if (TextUtils.isEmpty(address)) {
            try {
                address = readFile("/sys/class/net/eth0/address").toUpperCase().substring(0, 17);
            } catch (Exception e) {
            }
        }
        if (TextUtils.isEmpty(address)) {
            try {
                for (NetworkInterface nif : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                    if (nif.getName().equalsIgnoreCase("wlan0")) {
                        byte[] mac = nif.getHardwareAddress();
                        if (mac != null) {
                            StringBuilder sb = new StringBuilder();
                            for (byte b : mac) {
                                sb.append(String.format("%02X:", b));
                            }
                            if (sb.length() > 0) {
                                sb.deleteCharAt(sb.length() - 1);
                            }
                            address = sb.toString();
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
        return TextUtils.isEmpty(address) ? "ff:ff:ff:ff:ff:ff" : address;
    }

    private String readFile(String path) throws Exception {
        FileReader fr = new FileReader(path);
        String content = readAll(fr);
        fr.close();
        return content;
    }

    private String readAll(Reader reader) throws Exception {
        StringBuilder sb = new StringBuilder();
        char[] buf = new char[4096];
        int len;
        while ((len = reader.read(buf)) >= 0) {
            sb.append(buf, 0, len);
        }
        return sb.toString();
    }
    public String getStatus() {
        return getGid1(null);
    }

    public String d() {
        return getSpn1(null);
    }
}
