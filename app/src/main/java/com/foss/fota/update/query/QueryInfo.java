package com.foss.fota.update.query;

import android.content.Context;
import android.text.TextUtils;
import com.foss.fota.update.model.PolicyModel;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.JsonTools;
import com.foss.fota.utils.NotifyManager;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import java.util.HashMap;
import java.util.List;

/* JADX INFO: compiled from: QueryInfo.java */
/* JADX INFO: loaded from: classes.dex */
public class QueryInfo {
    private static QueryInfo instance = null;
    private static final String VERSION_JSON_FILENAME = "version.json";
    private HashMap<String, PolicyModel> policies = new HashMap<>();
    private VersionModel versionModel;
    private Context context;

    private QueryInfo(Context context) {
        this.context = context.getApplicationContext();
        this.versionModel = (VersionModel) JsonTools.a(FileUtil.a(context, VERSION_JSON_FILENAME), VersionModel.class);
        if (this.versionModel != null) {
            updatePolicies(this.versionModel.getPolicy());
        }
    }

    public static QueryInfo getInstance(Context context) {
        if (instance == null) {
            synchronized (QueryInfo.class) {
                if (instance == null) {
                    instance = new QueryInfo(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    public VersionModel getVersionModel() {
        return this.versionModel;
    }

    public void setVersionModel(Context context, VersionModel versionModel) {
        synchronized (this) {
            if (versionModel != null) {
                this.versionModel = versionModel;
                updatePolicies(this.versionModel.getPolicy());
                updateDownloadPath(context);
            }
        }
    }

    /* JADX WARN: Finally extract failed */
    public void reset(Context context) {
        synchronized (this) {
            try {
                try {
                    Trace.d("");
                    StorageUtil.f(this.context, StorageUtil.f(this.context));
                    PreferencesUtils.putString(this.context, "update_package_path", "");
                    NotifyManager.getInstance(context).a(context, com.foss.fota.R.string.appbar_scrolling_view_behavior);
                    FileUtil.f(context.getFilesDir().getPath() + "/" + VERSION_JSON_FILENAME);
                    this.versionModel = null;
                    this.policies.clear();
                    try {
                        FileUtil.f(context.getFilesDir().getPath() + "/" + VERSION_JSON_FILENAME);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    PreferencesUtils.putInt(context, "ota_update_status", 0);
                } catch (Exception e2) {
                    e2.printStackTrace();
                    try {
                        FileUtil.f(context.getFilesDir().getPath() + "/" + VERSION_JSON_FILENAME);
                    } catch (Exception e3) {
                        e3.printStackTrace();
                    }
                    PreferencesUtils.putInt(context, "ota_update_status", 0);
                }
            } catch (Throwable th) {
                try {
                    FileUtil.f(context.getFilesDir().getPath() + "/" + VERSION_JSON_FILENAME);
                } catch (Exception e4) {
                    e4.printStackTrace();
                }
                PreferencesUtils.putInt(context, "ota_update_status", 0);
                throw th;
            }
        }
    }

    public void init(Context context) {
        reset(context);
    }

    public void getPolicyArray(Context context) {
        reset(context);
    }

    public <T> T getPolicy(String str, Class<T> cls) {
        synchronized (this) {
            PolicyModel policyModel = this.policies.get(str);
            if (cls.equals(String.class)) {
                return cls.cast(policyModel != null ? policyModel.getValue() : null);
            }
            if (cls.equals(Integer.class)) {
                try {
                    return cls.cast(policyModel != null ? Integer.valueOf(policyModel.getValue()) : Integer.valueOf(0));
                } catch (Exception ignored) {
                    return cls.cast(Integer.valueOf(0));
                }
            }
            if (cls.equals(Boolean.class)) {
                try {
                    Boolean value = policyModel != null ? Boolean.valueOf(1 == Integer.parseInt(policyModel.getValue())) : Boolean.FALSE;
                    return cls.cast(value);
                } catch (Exception ignored) {
                    return cls.cast(Boolean.FALSE);
                }
            }
            return null;
        }
    }

    public String[] getPolicyArray(String str) {
        String[] strArrSplit;
        synchronized (this) {
            PolicyModel policyModel = this.policies.get(str);
            strArrSplit = (policyModel == null || policyModel.getValue() == null) ? null : policyModel.getValue().split("#");
        }
        return strArrSplit;
    }

    // Compatibility stub for ParserVersion.
    public void saveVersionJson(Context context, String versionJson) {
        try {
            FileUtil.writeFileToInternal(context, VERSION_JSON_FILENAME, versionJson);
        } catch (Exception ignored) {
        }
    }

    private void updatePolicies(List<PolicyModel> list) {
        if (list != null) {
            this.policies.clear();
            for (PolicyModel policyModel : list) {
                this.policies.put(policyModel.getKey(), policyModel);
            }
            PreferencesUtils.a(this.context, "ota_install_result_pop", ((Boolean) getPolicy("install_result_pop", Boolean.class)).booleanValue());
        }
    }

    private void updateDownloadPath(Context context) {
        try {
            String str = (String) getPolicy("download_path", String.class);
            if (!TextUtils.isEmpty(str) && str.contains("#")) {
                PreferencesUtils.putString(context, "ota_download_path", str);
                String[] strArrSplit = str.split("#");
                if (strArrSplit.length == 3) {
                    StorageUtil.a(strArrSplit[0], strArrSplit[1], strArrSplit[2]);
                }
            }
        } catch (Exception e) {
        }
    }
}
