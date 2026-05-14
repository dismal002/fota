package com.foss.fota.utils;

import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;
import com.foss.fota.MyApplication;
import java.lang.reflect.Method;
import java.util.Locale;

public class DeviceUtil {
    private static DeviceUtil instance;
    private String project;
    private String version;
    private String mid;
    private String model;
    private String device;
    private String product;
    private String manufacturer;
    private String brand;

    private DeviceUtil() {
        init();
    }

    public static final long DAY_UNIT = 24 * 60 * 60 * 1000L;
    public static final long INTERVAL_UNIT = 60 * 1000L;

    public static DeviceUtil getInstance() {
        if (instance == null) {
            synchronized (DeviceUtil.class) {
                if (instance == null) {
                    instance = new DeviceUtil();
                }
            }
        }
        return instance;
    }

    private void init() {
        this.project = getSystemProperty("ro.fota.oem", Build.PRODUCT);
        this.version = getSystemProperty("ro.fota.version", Build.ID);
        this.model = getSystemProperty("ro.product.model", Build.MODEL);
        this.device = getSystemProperty("ro.product.device", Build.DEVICE);
        this.product = getSystemProperty("ro.product.name", Build.PRODUCT);
        this.manufacturer = getSystemProperty("ro.product.manufacturer", Build.MANUFACTURER);
        this.brand = getSystemProperty("ro.product.brand", Build.BRAND);
    }

    private String getSystemProperty(String key, String defaultValue) {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method get = systemProperties.getMethod("get", String.class, String.class);
            return (String) get.invoke(null, key, defaultValue);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public String getProject() { return project; }
    public String getVersion() { return version; }
    public String getModel() { return model; }
    public String getDevice() { return device; }
    public String getProduct() { return product; }
    public String getManufacturer() { return manufacturer; }
    public String getBrand() { return brand; }

    public int getSdkLevel() { return Build.VERSION.SDK_INT; }
    public String getSdkRelease() { return Build.VERSION.RELEASE; }
    public String getVersionName() { return version; }
    public String getFingerprint() { return Build.FINGERPRINT; }
    public String getLocale() { 
        String locale = getSystemProperty("ro.fota.language", "");
        if (TextUtils.isEmpty(locale)) {
            locale = Locale.getDefault().toString();
        }
        return locale;
    }

    public String getResolution(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels + "#" + context.getResources().getDisplayMetrics().heightPixels;
    }

    public String getPlatform() { return getSystemProperty("ro.fota.platform", "unknownPlatform"); }
    public String getFinalizingPro() { return getSystemProperty("ro.fota.finalizing.pro", ""); }
    public String getDeviceType() { return getSystemProperty("ro.fota.type", "phone"); }
    public String getUpdateCycle() { return getSystemProperty("ro.fota.cycle", "1"); }
    public String getDisplay() { return getSystemProperty("ro.fota.display", "0"); }
    public String getVersionDisplay() { return getSystemProperty("ro.fota.version.display", ""); }
    public boolean isFmSuccessEnabled() { return "1".equals(getSystemProperty("ro.fota.fmsuccess", "0")); }
    public boolean isGoogleOta() { return "1".equals(getSystemProperty("ro.fota.googleota", "0")); }

    public boolean isOldReboot() {
        return PackageUtils.d(MyApplication.getInstance(), "com.foss.fota.sysoper");
    }

    public int getBuildVersion() {
        return PackageUtils.a(MyApplication.getInstance());
    }

    public boolean isScreenOn(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (Build.VERSION.SDK_INT >= 20) {
            return pm.isInteractive();
        }
        return pm.isScreenOn();
    }

    public boolean isAbUpdate() {
        try {
            Class<?> systemProperties = Class.forName("android.os.SystemProperties");
            Method getBoolean = systemProperties.getMethod("getBoolean", String.class, boolean.class);
            return (boolean) getBoolean.invoke(null, "ro.build.ab_update", false);
        } catch (Exception e) {
            return com.foss.fota.update.install.Install.isAbUpdate();
        }
    }

    public boolean isAutoWifiEnabled() { return "1".equals(getSystemProperty("ro.fota.auto.wifi", "1")); }
    public boolean isWifiOnlyEnabled() { return "1".equals(getSystemProperty("ro.fota.wifi.only", "0")); }
    
    public long getActivateTime() { 
        String activate = getSystemProperty("ro.fota.activate", "15");
        try {
            long mins = Long.parseLong(activate);
            if (mins > 1 && mins < 1440) return mins * 60 * 1000;
        } catch (Exception e) {}
        return 900000; 
    }

    public boolean isWifiAutoEnabled() { return isAutoWifiEnabled(); }
    public String getImei1() { return ""; }
    public String getImei2() { return ""; }
    public String getDeviceIdFromSystem() { return ""; }
    public String getSerial() { return android.os.Build.SERIAL; }
    public String getPreferredIdType() { return ""; }
    public boolean isLocalUpdateEnabled() { return true; }
    public boolean isExitEnabled() { return true; }

    // Compatibility aliases
    public String g() { return getProject(); }
    public String h() { return getVersionName(); }
    public String i() { return getLocale(); }
    public String j() { return getFingerprint(); }
    public String d() { return String.valueOf(getSdkLevel()); }
    public String e() { return getSdkRelease(); }
    public String a() { return getPlatform(); }
    public String b() { return getFinalizingPro(); }
    public String f() { return getDeviceType(); }

    public String getDeviceInfoExt() {
        String model = getSystemProperty("ro.product.model", Build.MODEL).replaceAll("_", "\\$");
        String brand = getSystemProperty("ro.product.brand", Build.BRAND).replaceAll("_", "\\$");
        String name = getSystemProperty("ro.product.name", Build.PRODUCT).replaceAll("_", "\\$");
        String device = getSystemProperty("ro.product.device", Build.DEVICE).replaceAll("_", "\\$");
        String board = getSystemProperty("ro.product.board", "unknown").replaceAll("_", "\\$");
        String manufacturer = getSystemProperty("ro.product.manufacturer", Build.MANUFACTURER).replaceAll("_", "\\$");
        String platform = getSystemProperty("ro.board.platform", "unknown").replaceAll("_", "\\$");
        String buildProduct = getSystemProperty("ro.build.product", Build.PRODUCT).replaceAll("_", "\\$");
        
        return model + "_" + brand + "_" + name + "_" + device + "_" + board + "_" + manufacturer + "_" + platform + "_" + buildProduct;
    }
}
