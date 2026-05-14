package com.foss.fota.update.request;

import android.content.Context;
import androidx.core.app.NotificationCompat;
import com.foss.fota.MyApplication;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.DeviceInfoProvider;
import com.foss.fota.utils.Mid;
import com.foss.fota.utils.PackageUtils;
import java.util.HashMap;
import java.util.Map;

/* JADX INFO: compiled from: RequestParam.java */
/* JADX INFO: loaded from: classes.dex */
public class RequestParam {
    private static final String TAG = RequestParam.class.getSimpleName();

    public static StringBuffer getBaseParams(Context context) {
        StringBuffer sb = new StringBuffer(1024);
        sb.append("device_type=").append(DeviceUtil.getInstance().getDeviceType())
          .append("&platform=").append(DeviceUtil.getInstance().getPlatform())
          .append("&project=").append(DeviceUtil.getInstance().getProject())
          .append("&version=").append(DeviceUtil.getInstance().getVersionName())
          .append("&devicesinfoExt=").append(DeviceUtil.getInstance().getDeviceInfoExt())
          .append("&swFingerprint=").append(DeviceUtil.getInstance().getFingerprint())
          .append("&sdk_level=").append(DeviceUtil.getInstance().getSdkLevel())
          .append("&sdk_release=").append(DeviceUtil.getInstance().getSdkRelease())
          .append("&mid=").append(Mid.getInstance(context).getMid())
          .append("&resolution=").append(DeviceUtil.getInstance().getResolution(context));
        return sb;
    }

    public static StringBuffer getRequestParams(Context context) {
        StringBuffer sb = getBaseParams(context);
        sb.append("&appVersion=").append(PackageUtils.getVersionName(context)).append(".0.1.001_2018-08-10 14:37")
          .append("&appCode=").append(PackageUtils.getVersionCode(context))
          .append("&local=").append(DeviceUtil.getInstance().getLocale())
          .append("&operator=").append(DeviceInfoProvider.getInstance(context).getOperator1(context))
          .append("&secondOperator=").append(DeviceInfoProvider.getInstance(context).getOperator2(context))
          .append("&connect_type=").append(DeviceInfoProvider.getInstance(context).getApnType(context))
          .append("&gid1=").append(DeviceInfoProvider.getInstance(context).getGid1(context))
          .append("&gid2=").append(DeviceInfoProvider.getInstance(context).getGid2(context))
          .append("&spn1=").append(DeviceInfoProvider.getInstance(context).getSpn1(context))
          .append("&spn2=").append(DeviceInfoProvider.getInstance(context).getSpn2(context))
          .append("&fotaSign=").append(PackageUtils.getSignature(context, context.getPackageName()))
          .append("&fotaDexHash=").append("&rebootDexHash=000").append("&rebootDexSize=0");
        
        if (MyApplication.isImeiSupported()) {
            sb.append("&imei1=").append(DeviceInfoProvider.getInstance(context).getImei1(context))
              .append("&imei2=").append(DeviceInfoProvider.getInstance(context).getImei2(context))
              .append("&mac=").append(DeviceInfoProvider.getInstance(context).getMacAddress(context));
        } else {
            sb.append("&imei1=").append(DeviceInfoProvider.getInstance(context).getImei1(context))
              .append("&imei2=").append(DeviceInfoProvider.getInstance(context).getImei2(context))
              .append("&mac=").append(DeviceInfoProvider.getInstance(context).getMacAddress(context));
        }
        
        Trace.d(TAG, "query params imei = " + DeviceInfoProvider.getInstance(context).getImei1(context));
        return sb;
    }

    public static Map<String, String> getParamMap(Context context) {
        HashMap<String, String> map = new HashMap<>();
        map.put("imei1", DeviceInfoProvider.getInstance(context).getImei1(context));
        map.put("imei2", DeviceInfoProvider.getInstance(context).getImei2(context));
        map.put("deviceType", DeviceUtil.getInstance().getDeviceType());
        map.put("connectType", String.valueOf(DeviceInfoProvider.getInstance(context).getApnType(context)));
        map.put("platform", DeviceUtil.getInstance().getPlatform());
        map.put("project", DeviceUtil.getInstance().getProject());
        map.put("version", DeviceUtil.getInstance().getVersionName());
        map.put("sdkLevel", String.valueOf(DeviceUtil.getInstance().getSdkLevel()));
        map.put("sdkRelease", DeviceUtil.getInstance().getSdkRelease());
        map.put("resolution", DeviceUtil.getInstance().getResolution(context));
        map.put("appVersion", PackageUtils.getVersionName(context) + ".0.1.001_2018-08-10 14:37");
        map.put("appCode", String.valueOf(PackageUtils.getVersionCode(context)));
        map.put("mac", DeviceInfoProvider.getInstance(context).getMacAddress(context));
        if (MyApplication.isRejectStatus()) {
            map.put(NotificationCompat.CATEGORY_STATUS, String.valueOf(true));
        }
        return map;
    }

    // Compatibility alias used by older call sites.
    public static java.io.Serializable autoCheckLayout(Context context) {
        return (java.io.Serializable) new java.util.HashMap<>(getParamMap(context));
    }
}
