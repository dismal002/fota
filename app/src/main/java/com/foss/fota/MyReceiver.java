package com.foss.fota;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.text.TextUtils;
import com.foss.fota.JobServiceUtil.MyIntentJobService;
import com.foss.fota.JobServiceUtil.TaskIntentJobService;
import com.foss.fota.update.request.RequestBase;
import com.foss.fota.update.request.RequestManager;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.IntentUtil;
import com.foss.fota.utils.NetWorkUtil;

public class MyReceiver extends BroadcastReceiver implements RequestBase.RequestCallback {

    @Override // android.content.BroadcastReceiver
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Trace.d("action = " + action);
        if (TextUtils.isEmpty(action)) {
            action = "android.net.conn.CONNECTIVITY_CHANGE";
        }
        
        if ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) && MyApplication.isNoReport() && NetWorkUtil.isConnected(context)) {
            RequestManager.executeRequest(context, this);
        }
        
        if ("android.intent.action.BOOT_COMPLETED".equals(action) || 
            "android.intent.action.ACTION_POWER_DISCONNECTED".equals(action) || 
            "android.intent.action.DATE_CHANGED".equals(action) || 
            ("android.net.conn.CONNECTIVITY_CHANGE".equals(action) && NetWorkUtil.isConnected(context))) {
            
            if (Build.VERSION.SDK_INT >= 26) {
                IntentUtil.a(context, 2, MyIntentJobService.class, action);
            } else {
                Intent intent2 = new Intent(context, MyIntentService.class);
                intent2.setAction(action);
                context.startService(intent2);
            }
            return;
        }
        
        if ("com.foss.fota.alarm".equals(action)) { // Assumed for com.foss.fota.MaterialDialog.a.l
            int taskId = intent.getIntExtra("task", Integer.MAX_VALUE);
            Trace.d("task to custom service, taskId=" + taskId);
            if (Build.VERSION.SDK_INT >= 26) {
                IntentUtil.a(context, 3, TaskIntentJobService.class, taskId, 0, "");
            } else {
                TaskIntentService.a(context, taskId, 0, "");
            }
            return;
        }
        
        if ("android.intent.action.MEDIA_REMOVED".equals(action) || "android.intent.action.MEDIA_BAD_REMOVAL".equals(action)) {
            Trace.d("Media removed");
            return;
        }
        
        if ("android.intent.action.MEDIA_MOUNTED".equals(action)) {
            Trace.d("Media mounted");
        }
    }

    @Override // com.foss.fota.update.request.RequestBase.RequestCallback
    public void onSuccess(String content) throws Exception {
        MyApplication.setNoReport(false);
        Trace.d("Request success: " + content);
    }
}
