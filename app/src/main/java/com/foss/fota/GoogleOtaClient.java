package com.foss.fota;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.Process;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import androidx.core.view.PointerIconCompat;
import android.text.Html;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.foss.fota.MaterialDialog;
import com.foss.fota.sysoper.Recovery;
import com.foss.fota.update.download.DownVersion;
import com.foss.fota.update.Status;
import com.foss.fota.update.model.LanguageModel;
import com.foss.fota.update.model.VersionModel;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceInfoProvider;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.utils.Mid;
import com.foss.fota.utils.NetWorkUtil;
import com.foss.fota.utils.PackageUtils;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.StorageUtil;
import com.foss.fota.view.FooterLayout;
import com.foss.fota.view.ProgressLayout;
import de.greenrobot.event.EventBus;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import org.apache.commons.compress.archivers.zip.UnixStat;
import com.foss.fota.update.query.QueryVersion;

public class GoogleOtaClient extends BaseActivity {
    private static final String[] c = {"android.permission.READ_PHONE_STATE", "android.permission.GET_ACCOUNTS"};
    private ProgressBar progressBar;
    LinearLayout contentLayout;
    private FooterLayout footerLayout;
    private ProgressLayout progressLayout;
    private RelativeLayout popButton;
    private TextView appNameTextView;
    private TextView updateTipTextView;
    private com.foss.fota.MaterialDialog materialDialog;
    private int otaStatus;
    private String languageStr;
    private int debugClickCount;
    private ImageView statusImageView;
    private TextView releaseNoteTextView;
    private LinearLayout abView;
    private LinearLayout proView;
    private LinearLayout preView;
    private TextView batteryTipTextView;
    private TextView updateTextView;
    private TextView progressTextView;
    private final long[] d = {3600000, 14400000, 28800000};
    private int m = 15;
    private int n = 1;
    private boolean o = false;
    private boolean q = false;
    private int r = 0;
    private Handler handler = new Handler() { // from class: com.foss.fota.GoogleOtaClient.1
        @Override // android.os.Handler
        public void handleMessage(Message message) {
            switch (message.what) {
                case 11:
                    if (GoogleOtaClient.this.materialDialog != null) {
                        if (GoogleOtaClient.this.m > 0) {
                            GoogleOtaClient.this.materialDialog.setTitle(GoogleOtaClient.this.getString(R.string.remind_last_time) + "(" + GoogleOtaClient.this.m + ")");
                            GoogleOtaClient.c(GoogleOtaClient.this);
                            GoogleOtaClient.this.handler.sendEmptyMessageDelayed(11, 1000L);
                        } else {
                            GoogleOtaClient.this.a(GoogleOtaClient.this.n);
                        }
                    }
                    break;
                case 33:
                    if (Status.progressLayout(GoogleOtaClient.this) == 0 && GoogleOtaClient.this.N() && NetWorkUtil.a(GoogleOtaClient.this)) {
                        GoogleOtaClient.this.statusImageView();
                        break;
                    }
                    break;
                case 100:
                    GoogleOtaClient.this.c(message.arg1);
                    break;
                default:
                    super.handleMessage(message);
                    break;
            }
        }
    };

    static /* synthetic */ int c(GoogleOtaClient googleOtaClient) {
        int updateTipTextView = googleOtaClient.m;
        googleOtaClient.m = updateTipTextView - 1;
        return updateTipTextView;
    }

    @Override // com.foss.fota.BaseActivity
    public void initData() {
        Trace.d("enter");
        if (PackageUtils.a(Process.myUid()) != 0) {
            Toast.makeText(getApplicationContext(), "It is Guest mode now, and Wireless Update is not available in this mode.", 0).show();
            finish();
        }
        setContentView(R.layout.activity_ota_client);
        ImageView imageView = (ImageView) findViewById(R.id.left_menu);
        imageView.setImageResource(R.mipmap.small_logo);
        imageView.setBackgroundResource(0);
        initView();
        EventBus.getDefault().register(this);
        updateTipTextView();
        this.handler.sendEmptyMessageDelayed(33, 1000L);
        if (Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }
        Trace.d("exit");
    }

    private void initView() {
        Trace.d("enter");
        this.footerLayout = (FooterLayout) findViewById(R.id.footer_layout);
        this.footerLayout.setOnClickListener(this);
        this.progressLayout = (ProgressLayout) findViewById(R.id.progress_layout);
        this.contentLayout = (LinearLayout) findViewById(R.id.child_scroll_ll);
        this.updateTipTextView = (TextView) findViewById(R.id.ota_update_tip);
        this.releaseNoteTextView = (TextView) findViewById(R.id.relese_view);
        this.popButton = (RelativeLayout) findViewById(R.id.btn_pop);
        this.popButton.setTag(10);
        this.popButton.setOnClickListener(this);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.left_menu_layout);
        frameLayout.setTag(9);
        frameLayout.setOnClickListener(this);
        this.appNameTextView = (TextView) findViewById(R.id.txt_app_name);
        this.appNameTextView.setTag(11);
        this.appNameTextView.setOnClickListener(this);
        this.preView = (LinearLayout) findViewById(R.id.pre_view);
        this.abView = (LinearLayout) findViewById(R.id.ab_view);
        this.proView = (LinearLayout) findViewById(R.id.pro_view);
        this.batteryTipTextView = (TextView) findViewById(R.id.battery_tip);
        this.updateTextView = (TextView) findViewById(R.id.update_txt);
        this.progressTextView = (TextView) findViewById(R.id.pro_txt);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_update_id);
        Trace.d("[initView] finish");
    }

    private void checkUpdateStatus() {
        int iF = Status.getUpdateStatus(this);
        Trace.d("version_status = " + iF);
        a(false, false);
        switch (iF) {
            case 0:
                m();
                break;
            case 1:
                this.r = 0;
                n();
                break;
            case 2:
                o();
                break;
            case 3:
                a(true);
                break;
            case 4:
                q();
                break;
            case 5:
                otaStatus();
                break;
            case 6:
                languageStr();
                break;
            default:
                m();
                break;
        }
    }

    private void checkPermissions() {
        for (String str : c) {
            if (checkSelfPermission(str) != 0) {
                requestPermissions(c, 1);
                return;
            }
        }
    }

    private void otaStatus() {
        int iB = PreferencesUtils.contentLayout(MyApplication.getInstance(), "ota_ab_progress", 0);
        a(true, true);
        this.updateTextView.setText(R.string.ab_installing);
        this.progressTextView.setText(iB + "%");
        this.progressBar.setProgress(iB);
        new Thread(new Runnable() { // from class: com.foss.fota.GoogleOtaClient.12
            @Override // java.lang.Runnable
            public void run() {
                Recovery.with(MyApplication.getInstance()).executeAb(StorageUtil.popButton(MyApplication.getInstance()));
            }
        }).start();
    }

    private void languageStr() {
        a(true, false);
        this.batteryTipTextView.setText(R.string.updated_need_reboot);
        this.footerLayout.a(6);
    }

    private void m() {
        Trace.d("enter");
        Status.a(this, 0);
        a(false, false);
        this.updateTipTextView.setVisibility(8);
        String strI = com.foss.fota.utils.DeviceUtil.getInstance().getVersionDisplay();
        if (TextUtils.isEmpty(strI)) {
            strI = com.foss.fota.utils.DeviceUtil.getInstance().getVersion();
        }
        this.releaseNoteTextView.setText(getString(R.string.current_version_text) + " " + strI);
        this.footerLayout.a(0);
        this.progressLayout.getType();
        L();
        Trace.d("exit");
    }

    private void n() {
        Trace.d("enter");
        Status.a(this, 1);
        this.updateTipTextView.setVisibility(0);
        r();
        this.footerLayout.a(1);
        this.progressLayout.getType();
        this.progressLayout.setVersionTip(getString(R.string.new_version_text));
        L();
        Trace.d("exit");
    }

    private void o() {
        Trace.d("enter");
        if (com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel() == null) {
            PreferencesUtils.putInt((Context) this, "ota_update_status", 3);
            a(true);
        } else {
            Status.a(this, 2);
            this.updateTipTextView.setVisibility(0);
            r();
            int iG = popButton();
            this.footerLayout.a(2);
            contentLayout(iG);
        }
        Trace.d("exit");
    }

    private void debugClickCount() {
        Trace.d("enter");
        Status.a(this, 2);
        this.updateTipTextView.setVisibility(0);
        r();
        this.footerLayout.a(2);
        contentLayout(popButton());
        Trace.d("exit");
    }

    private void a(boolean progressTextView) {
        Trace.d("enter");
        Status.a(this, 3);
        this.updateTipTextView.setVisibility(0);
        if (progressTextView) {
            r();
        }
        int iG = popButton();
        this.footerLayout.a(3);
        contentLayout(iG);
        Trace.d("exit");
    }

    private void q() {
        Trace.d("enter");
        if (com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel() == null) {
            m();
            return;
        }
        Status.a(this, 4);
        this.updateTipTextView.setVisibility(0);
        r();
        this.footerLayout.a(4);
        if (com.foss.fota.update.install.Install.getType()) {
            a(true, false);
            if (!com.foss.fota.update.install.Install.a(this)) {
                Trace.d("no update reason : support ab update but not support reboot ab update");
                EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, 100, 0L, 421L, "ab"));
                return;
            }
            int iIntValue = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(this).getPolicy("install_battery", Integer.class)).intValue();
            if (iIntValue <= 0) {
                iIntValue = 30;
            }
            if (com.foss.fota.update.install.Install.a(this, iIntValue)) {
                com.foss.fota.update.report.ReportData.reportQuery(this, "update");
                com.foss.fota.update.install.Install.contentLayout(MyApplication.getInstance());
            } else {
                Trace.d("no update reason : battery not enough");
                startService(new Intent(this, (Class<?>) BatteryService.class));
                EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, 100, 0L, 417L, "ab"));
            }
        } else {
            Trace.d("no update reason : not support ab update");
        }
        this.progressLayout.setDownLoadProgress(100);
        Trace.d("exit");
    }

    private void r() {
        int updateTipTextView;
        Trace.d("enter");
        VersionModel versionModelA = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
        if (versionModelA != null) {
            this.languageStr = getResources().getConfiguration().locale.getLanguage();
            String country = getResources().getConfiguration().locale.getCountry();
            if ("zh".equals(this.languageStr)) {
                this.languageStr += "_" + country;
            }
            Trace.d("mLanguageStr = " + this.languageStr);
            ArrayList<LanguageModel> releasenotes = versionModelA.getReleasenotes();
            if (releasenotes != null && releasenotes.size() > 0) {
                int size = releasenotes.size();
                if (size == 1) {
                    this.releaseNoteTextView.setText(Html.fromHtml(releasenotes.get(0).getContent().replaceAll("#FFFFFF", "#434343")));
                } else {
                    int i2 = 0;
                    int langIndex = 0;
                    while (true) {
                        if (i2 >= size) {
                            break;
                        } else {
                            if (this.languageStr.contains(releasenotes.get(i2).getCountry())) {
                                langIndex = i2;
                                break;
                            }
                            i2++;
                        }
                    }
                    this.releaseNoteTextView.setText(Html.fromHtml(releasenotes.get(langIndex).getContent().replaceAll("#FFFFFF", "#434343")));
                }
            }
            if (versionModelA.getIssilent() == 0) {
                this.handler.sendEmptyMessageDelayed(22, 1000L);
            }
        }
        Trace.d("exit");
    }

    @Override // com.foss.fota.BaseActivity
    public void widgetClick(View view) {
        if (view.getTag() != null) {
            if (((Integer) view.getTag()).intValue() == 2 || ((Integer) view.getTag()).intValue() == 3 || ((Integer) view.getTag()).intValue() == 1 || ((Integer) view.getTag()).intValue() == 4 || ((Integer) view.getTag()).intValue() == 7) {
                if (ClickControl.isFastClick()) {
                    Toast.makeText(this, R.string.button_click_toast, 0).show();
                    return;
                }
                ClickControl.updateLastClickTime();
            }
            switch (((Integer) view.getTag()).intValue()) {
                case 0:
                    statusImageView();
                    break;
                case 1:
                    releaseNoteTextView();
                    break;
                case 2:
                    abView();
                    break;
                case 3:
                case 4:
                    contentLayout(true);
                    break;
                case 5:
                    proView();
                    break;
                case 7:
                    preView();
                    break;
                case 8:
                    scheduleInstall();
                    break;
                case 9:
                    showMenu();
                    break;
                case 10:
                    new com.foss.fota.view.PopWindowsLayout().a(this, view);
                    break;
                case 11:
                    int updateTipTextView = this.debugClickCount + 1;
                    this.debugClickCount = updateTipTextView;
                    if (updateTipTextView > 4) {
                        this.debugClickCount = 0;
                        showDebugInfo();
                        Trace.setLoggingEnabled(true);
                    }
                    break;
                case 12:
                    Recovery.with(MyApplication.getInstance()).reboot();
                    break;
            }
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
    }

    @Override // com.foss.fota.BaseActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        this.o = false;
        com.foss.fota.utils.ActivityStackUtil.a(true);
    }

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onStop() {
        super.onStop();
        this.o = true;
        com.foss.fota.utils.ActivityStackUtil.a(false);
        if (PreferencesUtils.contentLayout(MyApplication.getInstance(), "ota_update_status", 0) == 5) {
            PreferencesUtils.putInt(MyApplication.getInstance(), "ota_ab_progress", this.progressBar.getProgress());
        }
    }

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
        this.handler.removeCallbacksAndMessages(null);
    }

    public void checkUpdateAction() {
        boolean zA = NetWorkUtil.isConnected(this);
        Trace.d("isConnected = " + zA);
        if (zA) {
            QueryVersion.getInstance(this).b(2);
        } else {
            handler();
        }
    }

    private void startDownloadAction() {
        Trace.d("");
        if (!com.foss.fota.update.install.Install.isInstallAllowed(this)) {
            Trace.d("no download reason : no reboot");
            com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 412, (String) null);
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.not_support_version).footerLayout(R.string.btn_ok).c();
            return;
        }
        if (NetWorkUtil.isConnected(this)) {
            boolean zB = PreferencesUtils.contentLayout(this, "download_only_wifi", com.foss.fota.utils.DeviceUtil.getInstance().isWifiOnlyEnabled());
            boolean zD = NetWorkUtil.isMobile(this);
            if (zB && zD) {
                Trace.d("no download reason : only support wifi update but mobile wifi off");
                com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 417, (String) null);
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.setting_network_tip).footerLayout(R.string.btn_ok).a(false).c();
                return;
            }
            VersionModel versionModelA = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
            if (versionModelA != null) {
                int iA = StorageUtil.a(this, versionModelA.getFilesize());
                if (iA == 1) {
                    Trace.d("no download reason : no sd card mounted");
                    com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 418, (String) null);
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.unmount_sdcard).footerLayout(R.string.btn_ok).c();
                    return;
                } else if (iA == 2) {
                    Trace.d("no download reason : sd card status illegal");
                    com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 419, (String) null);
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.sdCard_upgrade_memory_space_not_enough).a(R.string.sdcard_crash_or_unmount).footerLayout(R.string.btn_ok).c();
                    return;
                }
            }
            if (NetWorkUtil.isMobile(this)) {
                showNoWifiDialog();
                return;
            }
            DownVersion.getInstance(this).startDownload(0);
            Status.a(this, 2);
            this.footerLayout.a(2);
            return;
        }
        Trace.d("no download reason : no net connect");
        handler();
    }

    private void pauseDownloadAction() {
        Trace.d("enter");
        DownVersion.getInstance(this).stopDownload();
        Status.a(this, 3);
        this.footerLayout.a(3);
    }

    public void proView() {
        Trace.d("enter");
        int iF = Status.getUpdateStatus(this);
        if (iF == 2) {
            this.otaStatus = iF;
            DownVersion.getInstance(this).stopDownload();
            a(false);
        }
        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.cancel_download_title).a(R.string.cancel_download_content).footerLayout(R.string.cancel_download_positive_btn).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.22
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
                GoogleOtaClient.this.progressTextView();
            }
        }).progressLayout(R.string.cancel_download_negative_btn).contentLayout(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.21
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
                Trace.d("state=" + GoogleOtaClient.this.otaStatus);
                if (GoogleOtaClient.this.otaStatus == 2) {
                    GoogleOtaClient.this.otaStatus = 0;
                }
            }
        }).a(false).c();
    }

    private boolean contentLayout(boolean progressTextView) {
        Trace.d("enter");
        if (!com.foss.fota.update.install.Install.isInstallAllowed(this)) {
            Trace.d("no download reason : not support");
            com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 412, (String) null);
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.not_support_version).footerLayout(R.string.btn_ok).c();
            return false;
        }
        if (NetWorkUtil.isConnected(this)) {
            boolean zB = PreferencesUtils.contentLayout(this, "download_only_wifi", com.foss.fota.utils.DeviceUtil.getInstance().isWifiOnlyEnabled());
            boolean zD = NetWorkUtil.isMobile(this);
            if (zB && zD) {
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.setting_network_tip).footerLayout(R.string.btn_ok).a(false).c();
                Trace.d("no download reason : only support wifi update but mobile wifi off");
                com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 417, (String) null);
                return false;
            }
            VersionModel versionModelA = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
            if (versionModelA != null) {
                int iA = StorageUtil.a(this, versionModelA.getFilesize());
                if (iA == 1) {
                    Trace.d("no download reason : no sd card mounted");
                    com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 418, (String) null);
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.unmount_sdcard).footerLayout(R.string.btn_ok).c();
                    return false;
                }
                if (iA == 2) {
                    Trace.d("no download reason : sd card status not illegal");
                    com.foss.fota.update.report.ReportData.reportQuery((Context) this, false, 419, (String) null);
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.sdCard_upgrade_memory_space_not_enough).a(R.string.sdcard_crash_or_unmount).footerLayout(R.string.btn_ok).c();
                    return false;
                }
            }
            if (NetWorkUtil.isMobile(this)) {
                if (progressTextView) {
                    showNoWifiDialog();
                    return false;
                }
                int iIntValue = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(this).getPolicy("download_auto", Integer.class)).intValue();
                int iIntValue2 = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(this).getPolicy("download_wifi", Integer.class)).intValue();
                if (iIntValue == 1 && iIntValue2 == 0) {
                    return false;
                }
                showNoWifiDialog();
                return false;
            }
            DownVersion.getInstance(this).startDownload(0);
            Status.a(this, 2);
            this.footerLayout.a(2);
            return true;
        }
        Trace.d("no download reason : no net connect");
        handler();
        return false;
    }

    private void preView() {
        Trace.d("enter");
        if (!com.foss.fota.update.install.Install.isInstallAllowed(this)) {
            Trace.d("no update reason : no reboot");
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.not_support_version).footerLayout(R.string.btn_ok).c();
            return;
        }
        int iIntValue = ((Integer) com.foss.fota.update.query.QueryInfo.getInstance(this).getPolicy("install_battery", Integer.class)).intValue();
        int updateTipTextView = iIntValue <= 0 ? 30 : iIntValue;
        if (!com.foss.fota.update.install.Install.a(this, updateTipTextView)) {
            Trace.d("no update reason : battery not enough");
            this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_prompt_base, true).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.23
                @Override // com.foss.fota.MaterialDialog.contentLayout
                public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                    aVar.cancel();
                }
            }).a(false).getStatus();
            ImageView imageView = (ImageView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_icon);
            TextView textView = (TextView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_content);
            imageView.setBackgroundResource(R.mipmap.ota_battery);
            try {
                textView.setText(getString(R.string.ota_battery_low, new Object[]{Integer.valueOf(updateTipTextView), 30}));
            } catch (Exception footerLayout) {
            }
            this.materialDialog.show();
            return;
        }
        if (!com.foss.fota.update.install.Install.contentLayout(this, StorageUtil.popButton(this))) {
            Trace.d("no update reason : sd card status not illegal");
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.battery_remove_title).a(R.string.sdcard_crash_or_unmount).footerLayout(R.string.btn_ok).c();
        } else if (com.foss.fota.update.install.Install.getType() && !com.foss.fota.update.install.Install.a(this)) {
            Trace.d("no update reason : support ab update but not support reboot ab update");
            EventBus.getDefault().post(new com.foss.fota.update.EventMessage(300, 100, 0L, 421L, "ab"));
        } else {
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.update_prompt).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.24
                @Override // com.foss.fota.MaterialDialog.contentLayout
                public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                    GoogleOtaClient.this.updateTextView();
                }
            }).progressLayout(R.string.btn_cancel).a(false).c();
        }
    }

    public void batteryTipTextView() {
        Trace.d("enter");
        if (PreferencesUtils.c(this, "ota_install_delay_schedule") <= 0) {
            progressBar();
        } else {
            finish();
        }
    }

    public void updateTextView() {
        Trace.d("enter");
        this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_update_unzip, false).a(false).c();
        com.foss.fota.update.report.ReportData.reportQuery(this, "update");
        com.foss.fota.update.install.Install.contentLayout(getApplicationContext());
    }

    public void progressTextView() {
        Trace.d("enter");
        Status.a(this, 0);
        DownVersion.getInstance(this).cancelDownload();
        this.r = 1;
        m();
    }

    private void progressBar() {
        this.m = 15;
        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(getString(R.string.remind_last_time) + "(" + this.m + ")").c(17).a(R.layout.install_delay_dialog_schedule_choice, false).a(false).getStatus();
        RadioButton radioButton = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton1);
        RadioButton radioButton2 = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton2);
        RadioButton radioButton3 = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton3);
        radioButton.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.25
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (GoogleOtaClient.this.materialDialog != null) {
                    GoogleOtaClient.this.materialDialog.cancel();
                    GoogleOtaClient.this.materialDialog = null;
                }
                GoogleOtaClient.this.a(0);
            }
        });
        radioButton2.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.26
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (GoogleOtaClient.this.materialDialog != null) {
                    GoogleOtaClient.this.materialDialog.cancel();
                    GoogleOtaClient.this.materialDialog = null;
                }
                GoogleOtaClient.this.a(1);
            }
        });
        radioButton3.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.27
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (GoogleOtaClient.this.materialDialog != null) {
                    GoogleOtaClient.this.materialDialog.cancel();
                    GoogleOtaClient.this.materialDialog = null;
                }
                GoogleOtaClient.this.a(2);
            }
        });
        this.materialDialog.show();
        this.m--;
        this.handler.sendEmptyMessageDelayed(11, 1000L);
    }

    private void handler() {
        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.layout.dialog_no_network, true).a(false).footerLayout(R.string.ota_button_text_know).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.2
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
            }
        }).getStatus();
        ((TextView) this.materialDialog.getType().findViewById(R.id.wifi_enter)).setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (GoogleOtaClient.this.materialDialog != null) {
                    GoogleOtaClient.this.materialDialog.cancel();
                }
                GoogleOtaClient.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        });
        this.materialDialog.show();
    }

    private void showNoWifiDialog() {
        if (this.materialDialog != null && this.materialDialog.isShowing()) {
            this.materialDialog.dismiss();
        }
        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.layout.dialog_no_network, true).a(false).footerLayout(R.string.btn_cancel).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.5
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
                Trace.d("no download reason : user cancel");
                com.foss.fota.update.report.ReportData.reportQuery((Context) GoogleOtaClient.this, false, UnixStat.DEFAULT_FILE_PERM, (String) null);
            }
        }).progressLayout(R.string.btn_download).contentLayout(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.4
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
                DownVersion.getInstance(GoogleOtaClient.this).startDownload(0);
                Status.a(GoogleOtaClient.this, 2);
                GoogleOtaClient.this.footerLayout.a(2);
            }
        }).getStatus();
        ((TextView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_content)).setText(R.string.ota_no_wifi_tip);
        ((TextView) this.materialDialog.getType().findViewById(R.id.wifi_enter)).setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.6
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (GoogleOtaClient.this.materialDialog != null) {
                    GoogleOtaClient.this.materialDialog.cancel();
                }
                GoogleOtaClient.this.startActivity(new Intent("android.settings.WIFI_SETTINGS"));
            }
        });
        this.materialDialog.show();
    }

    public void setQueryType(int type) {
        Trace.d("delay time: " + this.d[type]);
        PreferencesUtils.putInt(this, "ota_install_delay_schedule", this.d[type]);
        com.foss.fota.update.Alarm.c(this, this.d[type] + System.currentTimeMillis());
        M();
        finish();
    }

    private void showDebugInfo() {
        String str;
        String strB;
        int iC;
        if (!PackageUtils.a(this, com.foss.fota.MaterialDialog.Constants.d)) {
            str = "not ok";
            strB = "";
            iC = 0;
        } else {
            str = "ok";
            strB = PackageUtils.contentLayout(this, com.foss.fota.MaterialDialog.Constants.d);
            iC = PackageUtils.c(this, com.foss.fota.MaterialDialog.Constants.d);
        }
        String str2 = "APK Release Date:2018-08-10 14:37\nGCM ID: " + PreferencesUtils.getString(this, "ota_gcm_id", "") + "\nIMEI: " + DeviceInfoProvider.getInstance(this).getDeviceId(this) + "\nMID: " + com.foss.fota.utils.Mid.getInstance(this).getMid() + "\nGID: " + DeviceInfoProvider.getInstance(this).getGid1(this) + "\nSPN: " + DeviceInfoProvider.getInstance(this).getSpn1(this) + "\nAppVersionName: " + PackageUtils.getVersionName(this) + ".0.1.001_2018-08-10 14:37\nAppVersionCode: " + PackageUtils.getVersionCode(this) + "\nversion: " + com.foss.fota.utils.DeviceUtil.getInstance().getVersionName() + "\nproject: " + com.foss.fota.utils.DeviceUtil.getInstance().getProject() + "\nReboot: " + str + " version=" + strB + " code=" + iC + "\n";
        if (!TextUtils.isEmpty(DeviceInfoProvider.getInstance(this).getDeviceId(this)) && !DeviceInfoProvider.getInstance(this).getDeviceId(this).matches("^[a-zA-Z0-9:]+$")) {
            Toast.makeText(this, R.string.match_imei, 1).show();
        }
        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.app_name).a(R.layout.dialog_debug_info, true).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.7
            @Override // com.foss.fota.MaterialDialog.contentLayout
            public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                aVar.cancel();
            }
        }).a(false).getStatus();
        ((TextView) this.materialDialog.getType().findViewById(R.id.info)).setText(str2);
        Button button = (Button) this.materialDialog.getType().findViewById(R.id.button_export_data);
        Button button2 = (Button) this.materialDialog.getType().findViewById(R.id.button_start_debug);
        Button button3 = (Button) this.materialDialog.getType().findViewById(R.id.button_stop_debug);
        button.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.8
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                GoogleOtaClient.this.E();
            }
        });
        button2.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.9
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                GoogleOtaClient.this.F();
            }
        });
        button3.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.GoogleOtaClient.10
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                GoogleOtaClient.this.G();
            }
        });
        this.materialDialog.show();
    }

    public void E() {
        final String str = getFilesDir().getParent() + "/shared_prefs/fossfota.xml";
        if (!"mounted".equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, R.string.sdcard_crash_or_unmount, 0).show();
            return;
        }
        File file = new File(com.foss.fota.utils.StorageUtil.getLogPath(this) + "/fota");
        if (!file.exists()) {
            file.mkdirs();
        }
        final String str2 = file.getAbsolutePath() + "/fossfota.txt";
        final String str3 = getFilesDir().getAbsolutePath() + "/" + com.foss.fota.config.Const.FIRMWARE_TXT;
        final String str4 = file.getAbsolutePath() + "/" + com.foss.fota.config.Const.FIRMWARE_TXT;
        new Thread(new Runnable() { // from class: com.foss.fota.GoogleOtaClient.11
            @Override // java.lang.Runnable
            public void run() {
                Process.setThreadPriority(10);
                FileUtil.a(str, str2, (Boolean) false);
                FileUtil.a(str3, str4, (Boolean) false);
            }
        }).start();
        Toast.makeText(this, getString(R.string.export_data) + " to " + file.getAbsolutePath(), 0).show();
    }

    public void F() {
        if (!"mounted".equals(Environment.getExternalStorageState())) {
            Toast.makeText(this, R.string.sdcard_crash_or_unmount, 0).show();
            return;
        }
        File file = new File(com.foss.fota.utils.StorageUtil.getLogPath(this) + "/fota");
        if (!file.exists()) {
            file.mkdirs();
        }
        File file2 = new File(file.getAbsolutePath() + "/" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Long.valueOf(System.currentTimeMillis())).replaceAll(" ", "-").replaceAll(":", "-") + ".txt");
        PreferencesUtils.putBoolean((Context) this, "debug_status", true);
        PreferencesUtils.putInt(this, "debug_log_path", file2.getAbsolutePath());
        Trace.setDebugEnabled(true);
        Trace.setLogPath(file2.getAbsolutePath());
        Toast.makeText(this, getString(R.string.start_catch_log) + " to " + file.getAbsolutePath(), 1).show();
    }

    public void G() {
        Trace.setDebugEnabled(false);
        PreferencesUtils.putBoolean((Context) this, "debug_status", false);
        Toast.makeText(this, getString(R.string.stop_catch_log), 1).show();
    }

    private void H() {
        Trace.d("enter");
        footerLayout.c(this);
        m();
        this.materialDialog = new MaterialDialog.Builder(this)
                .a(R.layout.dialog_prompt_base, true)
                .positiveText(R.string.ota_full_rom_check)
                .negativeText(R.string.btn_download)
                .setPositiveListener(new MaterialDialog.DialogActionListener() {
                    @Override
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                        GoogleOtaClient.this.K();
                    }
                })
                .setNegativeListener(new MaterialDialog.DialogActionListener() {
                    @Override
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                        GoogleOtaClient.this.startDownloadBypassRoot();
                    }
                })
                .a(false).getStatus();
        ImageView imageView = (ImageView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_icon);
        TextView textView = (TextView) this.materialDialog.getType().findViewById(R.id.dialog_prompt_content);
        imageView.setBackgroundResource(R.mipmap.ota_root);
        textView.setText(R.string.ota_device_rooted_content);
        this.materialDialog.show();
    }

    private void startDownloadBypassRoot() {
        com.foss.fota.update.model.VersionModel versionModel = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
        if (versionModel != null) {
            com.foss.fota.update.download.DownVersion.getInstance(this).startDownload(versionModel);
        }
    }

    public void onEventMainThread(com.foss.fota.update.EventMessage bVar) {
        Trace.d(" what  = " + bVar.getType() + "; param1= " + bVar.getStatus() + "; param2= " + bVar.c() + "; param3= " + bVar.d() + "");
        switch (bVar.getType()) {
            case 100:
                if (this.o) {
                    this.progressLayout.checkSchedule();
                    this.o = false;
                    this.updateTextView.setVisibility(0);
                }
                contentLayout(bVar);
                break;
            case 200:
                c(bVar);
                break;
            case 300:
                d(bVar);
                break;
        }
    }

    private void contentLayout(int updateTipTextView) {
        this.progressLayout.setDownLoadProgress(updateTipTextView);
    }

    private void a(long materialDialog, long j2) {
        if (Status.getUpdateStatus(this) != 2) {
            Status.a(this, 2);
            this.updateTipTextView.setVisibility(0);
            r();
            this.footerLayout.a(2);
        }
        contentLayout((int) (materialDialog > 0 ? (100 * j2) / materialDialog : 0L));
    }

    private void contentLayout(com.foss.fota.update.EventMessage bVar) {
        if (this.materialDialog != null) {
            this.materialDialog.cancel();
        }
        int iF = Status.getUpdateStatus(this);
        switch (bVar.getStatus()) {
            case 404:
                H();
                break;
            case 1000:
                if (iF == 0 && !this.o) {
                    Toast.makeText(this, R.string.new_version_query, 1).show();
                    break;
                }
                break;
            case PointerIconCompat.TYPE_CONTEXT_MENU /* 1001 */:
            case PointerIconCompat.TYPE_WAIT /* 1004 */:
                this.r = 0;
                n();
                break;
            case PointerIconCompat.TYPE_HAND /* 1002 */:
            case PointerIconCompat.TYPE_HELP /* 1003 */:
                I();
                break;
            case 1005:
                if (iF < 2) {
                    n();
                }
                break;
            case PointerIconCompat.TYPE_CELL /* 1006 */:
                m();
                break;
            case PointerIconCompat.TYPE_CROSSHAIR /* 1007 */:
                footerLayout.c(this);
                m();
                K();
                break;
            case PointerIconCompat.TYPE_VERTICAL_TEXT /* 1009 */:
                if (iF == 0) {
                    this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_loading, false).a(false).c();
                }
                break;
            case 1111:
                O();
                break;
            case 3005:
                Toast.makeText(this, R.string.sdcard_crash_or_unmount, 0).show();
                break;
            case 3008:
                a(bVar);
                break;
            case 3010:
                a(bVar);
                break;
        }
    }

    public void a(com.foss.fota.update.EventMessage bVar) {
        try {
            if (Status.getUpdateStatus(this) == 0 && !this.o) {
                if (!NetWorkUtil.isConnected(this)) {
                    Toast.makeText(this, R.string.ota_toast_no_network, 1).show();
                } else if (bVar.c() != 0 || bVar.initData() == null) {
                    Toast.makeText(this, getString(R.string.network_error) + "(" + bVar.c() + ")", 1).show();
                } else {
                    Toast.makeText(this, getString(R.string.network_error) + "(" + bVar.initData().toString().replaceAll("foss", "xxx") + ")", 1).show();
                }
            }
        } catch (Exception footerLayout) {
            Toast.makeText(this, R.string.ota_toast_no_network, 1).show();
        }
    }

    private void c(com.foss.fota.update.EventMessage bVar) {
        switch (bVar.getStatus()) {
            case 1000:
                if (this.materialDialog != null) {
                    this.materialDialog.cancel();
                }
                debugClickCount();
                break;
            case PointerIconCompat.TYPE_CONTEXT_MENU /* 1001 */:
                q();
                break;
            case 2000:
                a(bVar.c(), bVar.d());
                break;
            case 3000:
                int iF = Status.getUpdateStatus(this);
                if (iF != 2 && iF != 3) {
                    footerLayout.c(this);
                    m();
                } else {
                    try {
                        a(bVar.initData().toString());
                    } catch (Exception footerLayout) {
                        a("");
                        return;
                    }
                }
                break;
            case 5001:
                a(false);
                J();
                break;
        }
    }

    private void d(com.foss.fota.update.EventMessage bVar) {
        if (this.materialDialog != null) {
            this.materialDialog.cancel();
        }
        if (bVar.initData() != null && !TextUtils.isEmpty(bVar.initData().toString()) && bVar.initData().toString().equals("ab")) {
            Trace.d("ab install_callback enter");
            if (bVar.d() == 1) {
                if (bVar.getStatus() == 0) {
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.ab_install_success).a(R.string.updated_need_reboot).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.15
                        @Override // com.foss.fota.MaterialDialog.contentLayout
                        public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                            aVar.cancel();
                            Recovery.with(MyApplication.getInstance()).reboot();
                        }
                    }).progressLayout(R.string.btn_cancel).contentLayout(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.14
                        @Override // com.foss.fota.MaterialDialog.contentLayout
                        public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                            aVar.cancel();
                        }
                    }).a(false).c();
                    a(true, false);
                    this.batteryTipTextView.setText(R.string.updated_need_reboot);
                    this.footerLayout.a(6);
                }
                Trace.d("install_callback,install fail");
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.ab_install_fail).a(getString(R.string.ab_install_fail_reason) + bVar.getStatus()).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.16
                    @Override // com.foss.fota.MaterialDialog.contentLayout
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                    }
                }).a(false).c();
                return;
            }
            if (bVar.d() == 0) {
                Trace.d("install_callback,installing");
                this.footerLayout.a(5);
                a(true, true);
                this.updateTextView.setText(R.string.ab_installing);
                this.progressTextView.setText(bVar.c() + "%");
                this.progressBar.setProgress((int) bVar.c());
                return;
            }
            if (bVar.d() == 417) {
                a(true, false);
                this.footerLayout.a(4);
                this.batteryTipTextView.setText(getString(R.string.ab_battery_low, new Object[]{30}));
                return;
            }
            if (bVar.d() == 418) {
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.ab_install_fail).a(R.string.ab_parms_illegal).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.17
                    @Override // com.foss.fota.MaterialDialog.contentLayout
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                    }
                }).a(false).c();
                return;
            }
            if (bVar.d() == 419) {
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.ab_install_fail).a(R.string.ab_connect_fail).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.18
                    @Override // com.foss.fota.MaterialDialog.contentLayout
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                    }
                }).a(false).c();
                return;
            } else {
                if (bVar.d() == 5) {
                    this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_update_unzip, false).a(false).c();
                    return;
                }
                if (bVar.d() == 420) {
                    footerLayout.c(this);
                    m();
                    this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.ab_install_fail).a(R.string.sdCard_upgrade_find_update_file_fail).footerLayout(R.string.btn_ok).a(false).c();
                    return;
                } else {
                    if (bVar.d() == 421) {
                        a(true, false);
                        this.footerLayout.a(4);
                        this.batteryTipTextView.setText("ab-" + getString(R.string.not_support_version));
                        this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a("ab-" + getString(R.string.not_support_version)).footerLayout(R.string.btn_ok).a(false).c();
                        return;
                    }
                    return;
                }
            }
        }
        switch (bVar.getStatus()) {
            case 401:
            case 403:
            case 408:
            case 410:
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.package_unzip_error).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.19
                    @Override // com.foss.fota.MaterialDialog.contentLayout
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                    }
                }).a(false).c();
                break;
            case 402:
            case 409:
            case 411:
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.package_error_title).a(R.string.package_error_message_invalid).footerLayout(R.string.btn_ok).a(new MaterialDialog.DialogActionListener() { // from class: com.foss.fota.GoogleOtaClient.20
                    @Override // com.foss.fota.MaterialDialog.contentLayout
                    public void a(com.foss.fota.MaterialDialog aVar, DialogAction dialogAction) {
                        aVar.cancel();
                    }
                }).a(false).c();
                break;
            case 404:
                H();
                break;
            case 405:
                Trace.d("UPDATE_STATUS_OK");
                if (!com.foss.fota.update.install.Install.getType()) {
                    this.materialDialog = new MaterialDialog.Builder(this).a(R.layout.dialog_update_reboot, false).a(false).c();
                }
                break;
            case 412:
                footerLayout.c(this);
                m();
                this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.not_support_version).footerLayout(R.string.btn_ok).c();
                break;
        }
    }

    private void a(boolean progressTextView, boolean z2) {
        if (progressTextView) {
            this.preView.setVisibility(8);
            this.abView.setVisibility(0);
            if (z2) {
                this.proView.setVisibility(0);
                this.batteryTipTextView.setVisibility(8);
                return;
            } else {
                this.proView.setVisibility(8);
                this.batteryTipTextView.setVisibility(0);
                return;
            }
        }
        this.preView.setVisibility(0);
        this.abView.setVisibility(8);
    }

    public void c(int updateTipTextView) {
        switch (updateTipTextView) {
            case 1:
                releaseNoteTextView();
                break;
            case 4:
                preView();
                break;
        }
    }

    private void I() {
        Trace.d("enter");
        int iF = Status.getUpdateStatus(this);
        if (iF == 0) {
            this.progressLayout.setVersionTip(getString(R.string.no_new_version));
        }
        if (iF == 0 && !this.o) {
            Toast.makeText(this, R.string.no_new_version, 0).show();
        }
    }

    private void J() {
        Trace.d("enter");
        boolean zB = PreferencesUtils.contentLayout(this, "download_only_wifi", com.foss.fota.utils.DeviceUtil.getInstance().isWifiOnlyEnabled());
        boolean zD = NetWorkUtil.isMobile(this);
        if (zB && zD) {
            Trace.d(" ota network  wifi  to mobile  dialog");
            this.materialDialog = new MaterialDialog.Builder(this).contentLayout(R.string.not_support_fota_title).a(R.string.setting_network_tip).footerLayout(R.string.btn_ok).a(false).c();
        } else if (NetWorkUtil.isMobile(this)) {
            showNoWifiDialog();
        }
    }

    private void switchCheckUrl(String str) {
        Trace.d("enter");
        if (this.r == 1) {
            footerLayout.c(this);
            Trace.d("downloadfail----");
            this.r = 0;
            return;
        }
        if (!TextUtils.isEmpty(str) && str.equalsIgnoreCase(getString(R.string.package_unzip_error))) {
            Toast.makeText(this, str, 0).show();
            footerLayout.c(this);
            m();
            return;
        }
        a(false);
        if (!this.o && TextUtils.isEmpty(str)) {
            if (!NetWorkUtil.isConnected(this)) {
                Toast.makeText(this, R.string.ota_toast_no_network, 0).show();
                return;
            }
            VersionModel versionModelA = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
            if (versionModelA != null) {
                int iA = StorageUtil.a(this, versionModelA.getFilesize());
                if (iA == 1) {
                    Toast.makeText(this, R.string.unmount_sdcard, 0).show();
                } else if (iA == 2) {
                    Toast.makeText(this, R.string.sdcard_crash_or_unmount, 0).show();
                }
            }
        }
    }

    public void K() {
        Trace.d("enter");
        if (NetWorkUtil.isConnected(this)) {
            com.foss.fota.update.query.QueryVersion.getInstance(this).startQuery(2, 2);
        } else {
            handler();
        }
    }

    private void L() {
        CollapsingToolbarLayout collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collaspingView);
        if (Status.getUpdateStatus(this) != 0) {
            ((AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams()).setScrollFlags(21);
            collapsingToolbarLayout.requestLayout();
            ((AppBarLayout.LayoutParams) collapsingToolbarLayout.getLayoutParams()).setScrollFlags(4);
        }
    }

    private void M() {
        com.foss.fota.update.report.ReportData.reportQuery(this, "delay");
    }

    public boolean N() {
        String strB = PreferencesUtils.contentLayout(this, "ota_check_once_day", "");
        String str = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
        if (strB.equals(str)) {
            return false;
        }
        PreferencesUtils.putString(this, "ota_check_once_day", str);
        return true;
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        if (this.drawerLayout != null && this.drawerLayout.isDrawerOpen(3)) {
            this.drawerLayout.closeDrawer(3);
        } else if (com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel() != null) {
            moveTaskToBack(true);
        } else {
            finish();
        }
    }

    private void O() {
        if (!this.q) {
            this.q = true;
            this.statusImageView.setVisibility(4);
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, android.app.Activity, android.view.KeyEvent.Callback
    public boolean onKeyDown(int updateTipTextView, KeyEvent keyEvent) {
        if (updateTipTextView != 82) {
            return super.onKeyDown(updateTipTextView, keyEvent);
        }
        new com.foss.fota.view.PopWindowsLayout().a(this, this.popButton);
        return true;
    }

    public int popButton() {
        VersionModel versionModelA = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
        if (versionModelA == null) {
            return 0;
        }
        long filesize = versionModelA.getFilesize();
        int iE = filesize > 0 ? (int) ((100 * FileUtil.footerLayout(StorageUtil.popButton(this))) / filesize) : 0;
        return iE == 0 ? this.progressLayout.getProgress() : iE;
    }

    private static class ClickControl {
        private static long lastClickTime;

        public static boolean isFastClick() {
            long timeDiff = System.currentTimeMillis() - lastClickTime;
            return 0 < timeDiff && timeDiff < 1000;
        }

        public static void updateLastClickTime() {
            lastClickTime = System.currentTimeMillis();
        }
    }
    // Compatibility aliases for legacy deobfuscated call sites
    public void statusImageView() {
        if (com.foss.fota.utils.NetWorkUtil.isConnected(this)) {
            com.foss.fota.update.query.QueryVersion.getInstance(this).startQuery(2, 2);
        } else {
            handler();
        }
    }

    public void updateTipTextView() {
        int iF = com.foss.fota.update.Status.getUpdateStatus(this);
        Trace.d("version_status = " + iF);
        a(false, false);
        switch (iF) {
            case 0:
                m();
                break;
            case 1:
                this.r = 0;
                n();
                break;
            case 2:
                o();
                break;
            case 3:
                a(true);
                break;
            case 4:
                q();
                break;
        }
    }

    public void releaseNoteTextView() {
        Trace.d("enter");
        if (com.foss.fota.update.Status.getUpdateStatus(this) == 0) {
            if (com.foss.fota.utils.NetWorkUtil.isConnected(this)) {
                com.foss.fota.update.query.QueryVersion.getInstance(this).startQuery(2, 1);
            } else {
                handler();
            }
            return;
        }
        com.foss.fota.update.model.VersionModel versionModel = com.foss.fota.update.query.QueryInfo.getInstance(this).getVersionModel();
        if (versionModel != null) {
            com.foss.fota.update.download.DownVersion.getInstance(this).startDownload(versionModel);
        }
    }

    public void abView() {
        Trace.d("enter");
        com.foss.fota.update.download.DownVersion.getInstance(this).stopDownload();
        Status.a(this, 3);
        this.footerLayout.a(3);
    }

    public void scheduleInstall() {
        Trace.d("enter");
        com.foss.fota.update.install.Install.performAutoInstall(this);
    }

    public void showMenu() {
        new com.foss.fota.view.PopWindowsLayout().a(this, this.popButton);
    }

    // Status handler aliases
    public void a(int status) {
        c(status);
    }

    public void a(String message) {
        Trace.d("GoogleOtaClient", "Status message: " + message);
    }
}
