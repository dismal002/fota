package com.foss.fota;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import androidx.core.view.PointerIconCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.foss.fota.MaterialDialog;
import com.foss.fota.update.Status;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* JADX INFO: loaded from: classes.dex */
public class SdcardUpdateActivity extends BaseActivity {
    private static ExecutorService executorService;
    Button installButton;
    TextView webView;
    private String selectedFile;
    private MaterialDialog materialDialog;
    private Handler handler = new Handler() { // from class: com.foss.fota.SdcardUpdateActivity.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            if (!Thread.currentThread().isInterrupted()) {
                switch (message.what) {
                    case -2:
                        if (SdcardUpdateActivity.this.materialDialog != null) {
                            SdcardUpdateActivity.this.materialDialog.dismiss();
                        }
                        SdcardUpdateActivity.this.materialDialog = new MaterialDialog.Builder(SdcardUpdateActivity.this).b(R.string.sdCard_upgrade_copy_file_fail).a(R.string.sdCard_upgrade_copy_file_fail_prompt).selectedFile(R.string.btn_ok).c();
                        break;
                    case -1:
                        SdcardUpdateActivity.this.materialDialog = new MaterialDialog.Builder(SdcardUpdateActivity.this).b(R.string.sdCard_upgrade_find_update_file_fail).a(R.string.sdCard_upgrade_update_file_fail_prompt).selectedFile(R.string.btn_ok).c();
                        break;
                    case 2:
                        Trace.d("SdcardUpdateActivity:", "LocalSdUpdate = " + SdcardUpdateActivity.this.selectedFile);
                        File file = new File(SdcardUpdateActivity.this.selectedFile);
                        if (file.exists()) {
                            Trace.d("SdcardUpdateActivity:", "LocalSdUpdate:: selectFile.exists true");
                        }
                        if (file.exists()) {
                            Trace.d("SdcardUpdateActivity:", "LocalSdUpdate:: selectFile.exists true");
                            if (!file.getName().equals("LocalSdUpdate.zip")) {
                                File file2 = new File(file.getParent() + "/LocalSdUpdate.zip");
                                if (Build.VERSION.SDK_INT < 23 && file.renameTo(file2)) {
                                    Trace.d("SdcardUpdateActivity:", "rename to " + file2);
                                    SdcardUpdateActivity.this.selectedFile = file.getPath();
                                }
                            }
                        }
                        int i = Build.VERSION.SDK_INT;
                        Trace.d("SdcardUpdateActivity:", "sdkVer = " + i);
                        if (i >= 23 && (!StorageUtil.isOnRemovableStorage(SdcardUpdateActivity.this, SdcardUpdateActivity.this.selectedFile) || SdcardUpdateActivity.this.selectedFile.contains("/emulated/0"))) {
                        try {
                            if (SdcardUpdateActivity.this.isEnoughSpace(new File(SdcardUpdateActivity.this.selectedFile).length(), SdcardUpdateActivity.this.getSdcardPath(SdcardUpdateActivity.this.selectedFile))) {
                                Trace.d("SdcardUpdateActivity:", "23, copy to android/data/...");
                                SdcardUpdateActivity.this.showLoadingDialog();
                                if (SdcardUpdateActivity.this.selectedFile.contains("/emulated/0")) {
                                    SdcardUpdateActivity.this.handler.sendMessageDelayed(SdcardUpdateActivity.this.handler.obtainMessage(11), 100L);
                                } else {
                                    SdcardUpdateActivity.this.handler.sendMessageDelayed(SdcardUpdateActivity.this.handler.obtainMessage(12), 100L);
                                }
                            } else {
                                return;
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                        }
                        } else if (i < 21 || !StorageUtil.isOnRemovableStorage(SdcardUpdateActivity.this, SdcardUpdateActivity.this.selectedFile)) {
                            SdcardUpdateActivity.this.doUpdate();
                        } else {
                            try {
                                if (SdcardUpdateActivity.this.isEnoughSpace(new File(SdcardUpdateActivity.this.selectedFile).length(), SdcardUpdateActivity.this.getSdcardPath(SdcardUpdateActivity.this.selectedFile))) {
                                    Trace.d("SdcardUpdateActivity:", "updatePackage, copy to data");
                                    SdcardUpdateActivity.this.showLoadingDialog();
                                    SdcardUpdateActivity.this.handler.sendMessageDelayed(SdcardUpdateActivity.this.handler.obtainMessage(11), 100L);
                                } else {
                                    return;
                                }
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        }
                        break;
                    case 11:
                        Trace.d("SdcardUpdateActivity:", "LocalSdUpdate::COPY_FILE1_TO_DATA !");
                        SdcardUpdateActivity.executorService.execute(new Runnable() { // from class: com.foss.fota.SdcardUpdateActivity.1.1
                            @Override // java.lang.Runnable
                            public void run() {
                                try {
                                    new File(SdcardUpdateActivity.this.getFilesDir() + "/fossfota").mkdirs();
                                } catch (Exception selectedFile) {
                                }
                                if (!SdcardUpdateActivity.this.copyFile(SdcardUpdateActivity.this.selectedFile, com.foss.fota.MaterialDialog.Constants.g, false)) {
                                    SdcardUpdateActivity.this.handler.sendMessage(SdcardUpdateActivity.this.handler.obtainMessage(-2));
                                    return;
                                }
                                SdcardUpdateActivity.this.selectedFile = com.foss.fota.MaterialDialog.Constants.g;
                                SdcardUpdateActivity.this.handler.sendMessage(SdcardUpdateActivity.this.handler.obtainMessage(15));
                            }
                        });
                        break;
                    case 12:
                        Trace.d("SdcardUpdateActivity:", "LocalSdUpdate::COPY_FILE23_TO_DATA !");
                        SdcardUpdateActivity.executorService.execute(new Runnable() { // from class: com.foss.fota.SdcardUpdateActivity.1.2
                            @Override // java.lang.Runnable
                            public void run() {
                                String str = StorageUtil.b((Context) SdcardUpdateActivity.this, true) + com.foss.fota.MaterialDialog.Constants.h;
                                Trace.d("SdcardUpdateActivity:", "LocalSdUpdate::path = " + str);
                                if (SdcardUpdateActivity.this.copyFile(SdcardUpdateActivity.this.selectedFile, str, false)) {
                                    SdcardUpdateActivity.this.selectedFile = str;
                                    SdcardUpdateActivity.this.handler.sendMessage(SdcardUpdateActivity.this.handler.obtainMessage(15));
                                } else {
                                    SdcardUpdateActivity.this.handler.sendMessage(SdcardUpdateActivity.this.handler.obtainMessage(-2));
                                }
                            }
                        });
                        break;
                    case 15:
                        File file3 = new File("/data/media/0/fossfota/update.zip");
                        File file4 = new File("/data/media/fossfota/update.zip");
                        new File(com.foss.fota.MaterialDialog.Constants.g);
                        if (file3.exists()) {
                            file3.delete();
                        }
                        if (file4.exists()) {
                            file4.delete();
                        }
                        SdcardUpdateActivity.this.doUpdate();
                        break;
                }
            }
            super.handleMessage(message);
        }
    };

    @Override // com.foss.fota.BaseActivity
    protected void initData() {
        setContentView(R.layout.activity_sdcard_update);
        executorService = Executors.newFixedThreadPool(3);
        this.selectedFile = getIntent().getExtras().getString("selected_file");
        Trace.d("SdcardUpdateActivity:", "selected_file = " + this.selectedFile);
        initViews();
    }

    public void onBack(View view) {
        finish();
    }

    @Override // com.foss.fota.BaseActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        executorService.shutdownNow();
        finish();
    }

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
        this.handler.removeCallbacksAndMessages(null);
        if (this.materialDialog != null) {
            this.materialDialog.cancel();
        }
    }

    private void initViews() {
        ((TextView) findViewById(R.id.sdcard_update_file_name)).setText(getString(R.string.selected_update_zip) + new File(this.selectedFile).getName());
        this.webView = (TextView) findViewById(R.id.sdcard_update_webview);
        this.webView.setText(Html.fromHtml(getString(R.string.sdCard_update_tips_content)));
        this.installButton = (Button) findViewById(R.id.sdcard_update_install);
        this.installButton.setTag(Integer.valueOf(R.id.sdcard_update_install));
        this.installButton.setOnClickListener(this);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showLoadingDialog() {
        this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_loading_copy_file, false).a(false).c();
    }

    public boolean copyFile(String str, String str2, Boolean bool) {
        Exception exc;
        int i;
        int i2;
        Trace.d("copy, oldPath = " + str);
        Trace.d("copy, newPath = " + str2);
        try {
            File file = new File(str2);
            File file2 = new File(str);
            if (file.exists()) {
                file.delete();
            } else {
                Trace.d("copy, newPath = " + str2 + " is not exist!");
            }
            if (file2.exists()) {
                FileInputStream fileInputStream = new FileInputStream(str);
                FileOutputStream fileOutputStream = new FileOutputStream(str2);
                byte[] bArr = new byte[32768];
                int i3 = 0;
                while (true) {
                    int i4 = fileInputStream.read(bArr);
                    if (i4 == -1) {
                        i2 = 0;
                        break;
                    }
                    if (Thread.currentThread().isInterrupted()) {
                        i2 = 1;
                        break;
                    }
                    i3 += i4;
                    System.out.println(i3);
                    fileOutputStream.write(bArr, 0, i4);
                }
                try {
                    fileInputStream.close();
                    fileOutputStream.close();
                    if (i2 == 1) {
                        return false;
                    }
                    try {
                        if (bool.booleanValue()) {
                            Trace.d("copy success to delete" + file2.delete());
                        }
                        i = 2;
                    } catch (Exception selectedFile) {
                        exc = selectedFile;
                        i = 2;
                        exc.printStackTrace();
                        Trace.d("copy, Exception" + exc.toString());
                    }
                } catch (Exception e2) {
                    i = i2;
                    exc = e2;
                }
            } else {
                Trace.d("copy, oldPath = " + str + " is not exist!");
                i = 0;
            }
        } catch (Exception e3) {
            exc = e3;
            i = 0;
        }
        Trace.d("copy, isOk " + i);
        return i == 2;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void doUpdate() {
        StorageUtil.a(this);
        Trace.d("SdcardUpdateActivity:", "doUpdate:: selected_file " + this.selectedFile);
        PreferencesUtils.putInt((Context) this, "ota_update_status", 0);
        PreferencesUtils.putBoolean((Context) this, "ota_update_local", true);
        PreferencesUtils.putString(this, "ota_update_local_path", this.selectedFile);
        com.foss.fota.update.install.Install.doInstall(this, this.selectedFile);
    }

    private void checkUpdateCondition() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (!com.foss.fota.update.install.Install.checkBattery(this, 30)) {
            this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_prompt_base, true).positiveText(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.SdcardUpdateActivity.2
                @Override
                public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                    aVar.cancel();
                }
            }).a(false).show();
            ImageView imageView = (ImageView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_icon);
            TextView textView = (TextView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_content);
            imageView.setBackgroundResource(R.mipmap.ota_battery);
            try {
                textView.setText(getString(R.string.ota_battery_low, new Object[]{30, 30}));
            } catch (Exception selectedFile) {
            }
            this.materialDialog.show();
            return;
        }
        if (com.foss.fota.update.install.Install.isAbUpdate() && !com.foss.fota.update.install.Install.isInstallAllowed(this)) {
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).message("ab-" + getString(R.string.not_support_version)).positiveText(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.SdcardUpdateActivity.3
                @Override
                public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                    aVar.cancel();
                }
            }).a(false).show();
        } else {
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).message(R.string.sdcard_update_prompt).positiveText(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.SdcardUpdateActivity.4
                @Override
                public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                    com.foss.fota.update.Notice.cancelUpdate(MyApplication.getInstance());
                    EventBus.getDefault().post(new com.foss.fota.update.EventMessage(100, PointerIconCompat.TYPE_CELL, 0L, 0L, null));
                    if (com.foss.fota.utils.DeviceUtil.getInstance().isAbUpdate()) {
                        PreferencesUtils.putInt(MyApplication.getInstance(), "ota_update_status", 5);
                    }
                    SdcardUpdateActivity.this.startUpdateProcess();
                }
            }).materialDialog(R.string.btn_cancel).a(false).c();
        }
    }

    private boolean isSdcardMounted() {
        try {
            return StorageUtil.getStorageState(this, getSdcardPath(this.selectedFile)).equals("mounted");
        } catch (Exception selectedFile) {
            Trace.selectedFile("SdcardUpdateActivity:", "getSdcardAvailable error " + selectedFile.toString());
            selectedFile.printStackTrace();
            return false;
        }
    }

    private boolean isSdcardRootZip(String str) {
        if (str == null) {
            return false;
        }
        String parent = new File(str).getParent();
        if (Build.VERSION.SDK_INT >= 21) {
            String strB = StorageUtil.b((Context) this, true);
            String strB2 = StorageUtil.b((Context) this, false);
            Trace.d("SdcardUpdateActivity:", "isSdcardRootZip,outSdcard=" + strB + " ,innerSdcard=" + strB2);
            boolean z = !TextUtils.isEmpty(strB) && parent.equals(strB);
            if (!TextUtils.isEmpty(strB2) && parent.equals(strB2)) {
                z = true;
            }
            return z;
        }
        List<StorageUtil.StorageVolumeInfo> listB = StorageUtil.getStatus();
        if (listB == null) {
            return false;
        }
        for (int i = 0; i < listB.size(); i++) {
            StorageUtil.StorageVolumeInfo aVar = listB.get(i);
            if (aVar != null && parent.equals(aVar.path)) {
                return true;
            }
        }
        return false;
    }

    @Override // com.foss.fota.BaseActivity
    public void widgetClick(View view) {
        if (((Integer) view.getTag()).intValue() == R.id.sdcard_update_install) {
            Trace.d("SdcardUpdateActivity:", "onClick, install now");
            checkUpdateCondition();
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startUpdateProcess() {
        if (!com.foss.fota.update.install.Install.checkFile(this)) {
            this.materialDialog = new MaterialDialog.Builder(this).b(R.string.not_support_fota_title).a(R.string.not_support_version).selectedFile(R.string.btn_ok).c();
            return;
        }
        File file = new File(this.selectedFile);
        if (!file.exists()) {
            this.materialDialog = new MaterialDialog.Builder(this).b(R.string.sdCard_upgrade_find_update_file_fail).a(R.string.sdCard_upgrade_copy_file_fail_prompt).selectedFile(R.string.btn_ok).c();
            return;
        }
        if (!isSdcardMounted()) {
            this.materialDialog = new MaterialDialog.Builder(this).b(R.string.not_support_fota_title).a(R.string.unmount_sdcard).selectedFile(R.string.btn_ok).c();
            return;
        }
        if (isSdcardRootZip(this.selectedFile)) {
            File file2 = new File(file.getParent() + "/LocalSdUpdate.zip");
            if (Build.VERSION.SDK_INT < 23 && file.renameTo(file2)) {
                this.selectedFile = file2.getAbsolutePath();
            }
            this.handler.sendEmptyMessage(2);
            return;
        }
        if (isEnoughSpace(file.length(), getSdcardPath(this.selectedFile))) {
            executorService.execute(new Runnable() { // from class: com.foss.fota.SdcardUpdateActivity.5
                @Override // java.lang.Runnable
                public void run() {
                    Trace.d("SdcardUpdateActivity:", "sdCardUpdate, copy to ota_root file");
                    if (Build.VERSION.SDK_INT < 23) {
                        String str = SdcardUpdateActivity.this.getSdcardPath(SdcardUpdateActivity.this.selectedFile) + "/LocalSdUpdate.zip";
                        if (SdcardUpdateActivity.this.copyFile(SdcardUpdateActivity.this.selectedFile, str, true)) {
                            SdcardUpdateActivity.this.selectedFile = str;
                        }
                    }
                    SdcardUpdateActivity.this.handler.sendEmptyMessage(2);
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getSdcardPath(String str) {
        if (str != null) {
            String strB = StorageUtil.b(this, str);
            if (!TextUtils.isEmpty(strB)) {
                return strB;
            }
            List<StorageUtil.StorageVolumeInfo> listB = StorageUtil.getStatus();
            if (listB != null) {
                int i = 0;
                while (true) {
                    int i2 = i;
                    if (i2 < listB.size()) {
                        StorageUtil.StorageVolumeInfo aVar = listB.get(i2);
                        if (aVar == null || !str.startsWith(aVar.path)) {
                            i = i2 + 1;
                        } else {
                            return aVar.path;
                        }
                    } else {
                        return str;
                    }
                }
            } else {
                return str;
            }
        } else {
            return str;
        }
    }

    private boolean checkSdcardSpaceNeeded(long j, String str) {
        try {
            StatFs statFs = new StatFs(str);
            long blockSize = ((long) statFs.getBlockSize()) * ((long) statFs.getAvailableBlocks());
            Trace.d("SdcardUpdateActivity:", "checkSdcardSpaceNeeded totalSize = " + blockSize);
            if (blockSize <= j) {
                return false;
            }
            Trace.selectedFile("SdcardUpdateActivity:", "checkSdcardSpaceNeeded true, totalSize = " + blockSize);
            return true;
        } catch (Exception selectedFile) {
            Trace.selectedFile("SdcardUpdateActivity:", "checkSdcardSpaceNeeded false, card mount error");
            return false;
        }
    }

    public void onEventMainThread(com.foss.fota.update.EventMessage bVar) {
        switch (bVar.c()) {
            case 300:
                cancelDownload(bVar);
                break;
        }
    }

    private void cancelDownload(com.foss.fota.update.EventMessage bVar) {
        if (this.materialDialog != null) {
            this.materialDialog.cancel();
        }
        finish();
    }

    public boolean isEnoughSpace(long j, String str) {
        if (checkSdcardSpaceNeeded(j, str)) {
            return true;
        }
        this.materialDialog = new MaterialDialog.Builder(this).b(R.string.sdCard_upgrade_memory_space_not_enough).a(R.string.sdcard_crash_or_unmount).selectedFile(R.string.btn_ok).c();
        Trace.d("SdcardUpdateActivity:", "isEnough false");
        return false;
    }

    // Compatibility aliases
    public boolean a(long j, String str) {
        return isEnoughSpace(j, str);
    }

    public String b(String str) {
        return getSdcardPath(str);
    }

    public boolean a(String src, String dest, boolean overwrite) {
        return copyFile(src, dest, overwrite);
    }
}
