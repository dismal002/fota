package com.foss.fota.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import com.foss.fota.update.model.VersionModel;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/* JADX INFO: compiled from: NetWorkUtil.java */
/* JADX INFO: loaded from: classes.dex */
public class NetWorkUtil {
    public static boolean isConnected(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity"))
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static void checkHost(final Context context) {
        try {
            if (Trace.isLogEnabled()) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            VersionModel version = com.foss.fota.update.query.QueryInfo.getInstance(context)
                                    .getVersionModel();
                            if (version != null) {
                                logNetworkInfo(context);
                                String url = version.getDeltaurl();
                                String host = url.substring(url.indexOf("//") + 2);
                                if (host.contains("/")) {
                                    host = host.substring(0, host.indexOf("/"));
                                }
                                resolveHost(host);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean isWifi(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity"))
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 1;
    }

    public static boolean isWifiConnected(Context context) {
        return isWifi(context);
    }

    public static boolean isMobile(Context context) {
        NetworkInfo activeNetworkInfo = ((ConnectivityManager) context.getSystemService("connectivity"))
                .getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.getType() == 0;
    }

    public static int getNetworkType(Context context) {
        if (isWifi(context)) {
            return 101;
        }
        if (isMobile(context)) {
            return 102;
        }
        return 100;
    }

    public static void logNetworkInfo(Context context) {
        int type = getNetworkType(context);
        if (type == 102) {
            Trace.d("NetWorkUtil", "Network type : mobile. ip = " + getMobileIpAddress());
        } else if (type == 101) {
            Trace.d("NetWorkUtil", "Network type : wifi. ip = " + getWifiIpAddress(context));
        } else {
            Trace.d("NetWorkUtil", "Network type : other");
        }
    }

    public static String getMobileIpAddress() {
        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                Enumeration<InetAddress> inetAddresses = networkInterfaces.nextElement().getInetAddresses();
                while (inetAddresses.hasMoreElements()) {
                    InetAddress inetAddress = inetAddresses.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getWifiIpAddress(Context context) {
        int ipAddress = ((WifiManager) context.getSystemService("wifi")).getConnectionInfo().getIpAddress();
        String str = String.format("%d.%d.%d.%d", ipAddress & 255, (ipAddress >> 8) & 255, (ipAddress >> 16) & 255,
                (ipAddress >> 24) & 255);
        Trace.d("NetWorkUtil", "getLocalIpAddress() mobile net ipv4 = " + str);
        return str;
    }

    public static String resolveHost(String host) {
        try {
            InetAddress address = InetAddress.getByName(host);
            return address.getHostAddress();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Compatibility alias
    public static boolean isConnectedAlias(Context context) {
        return isConnected(context);
    }

    // Compatibility alias
    public static boolean a(Context context) {
        return isConnected(context);
    }

    public static boolean d(Context context) {
        return isMobile(context);
    }

    public static boolean isMobileConnected(Context context) {
        return isMobile(context);
    }

    public static void updateNetworkStatus(Context context) {
        logNetworkInfo(context);
        checkHost(context);
    }
}
