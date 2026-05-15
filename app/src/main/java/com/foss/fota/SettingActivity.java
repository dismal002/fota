package com.foss.fota;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.content.pm.PackageManager;
import android.net.Uri;
import com.foss.fota.MaterialDialog;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.view.TitleContentView;
import java.io.Serializable;

/* JADX INFO: loaded from: classes.dex */
public class SettingActivity extends BaseActivity {
    private static final String b = SettingActivity.class.getSimpleName();
    private RelativeLayout autoCheckLayout;
    private CheckBox wifiCheckBox;
    private CheckBox autoDownloadCheckBox;
    private TextView scheduleTextView;
    private RelativeLayout wifiLayout;
    private RelativeLayout autoDownloadLayout;
    private RelativeLayout loggingLayout;
    private CheckBox loggingCheckBox;
    private TitleContentView aboutView;
    private RelativeLayout launcherIconLayout;
    private CheckBox launcherIconCheckBox;
    private RelativeLayout githubLayout;
    private int checkFrequency;
    private int scheduleIndex = 0;
    private MaterialDialog materialDialog;

    @Override // com.foss.fota.BaseActivity
    protected void initData() {
        setContentView(R.layout.activity_setting);
        Trace.debugIf(true, "[onCreate]============");
        wifiLayout();
        autoDownloadLayout();
    }

    public void onBack(View view) {
        finish();
    }

    private void wifiLayout() {
        ((TextView) findViewById(R.id.title_text)).setText(R.string.option_settings);
        this.scheduleTextView = (TextView) findViewById(R.id.auto_check_schedule);
        this.autoCheckLayout = (RelativeLayout) findViewById(R.id.setting_auto_check_layout);
        this.autoCheckLayout.setOnClickListener(this);
        this.wifiLayout = (RelativeLayout) findViewById(R.id.setting_wifi_layout);
        this.wifiLayout.setOnClickListener(this);
        this.wifiCheckBox = (CheckBox) findViewById(R.id.wifi_checkbox);
        this.wifiCheckBox.setOnClickListener(this);
        this.autoDownloadLayout = (RelativeLayout) findViewById(R.id.setting_auto_download_layout);
        this.autoDownloadLayout.setOnClickListener(this);
        this.autoDownloadCheckBox = (CheckBox) findViewById(R.id.auto_download_checkbox);
        this.autoDownloadCheckBox.setOnClickListener(this);
        this.loggingLayout = (RelativeLayout) findViewById(R.id.setting_logging_layout);
        this.loggingLayout.setOnClickListener(this);
        this.loggingCheckBox = (CheckBox) findViewById(R.id.logging_checkbox);
        this.loggingCheckBox.setOnClickListener(this);
        this.aboutView = (TitleContentView) findViewById(R.id.about);
        if (MyApplication.isPrivacyPolicyInstalled()) {
            this.aboutView.setVisibility(0);
            this.aboutView.setOnClickListener(this);
        }
        this.launcherIconLayout = (RelativeLayout) findViewById(R.id.setting_launcher_icon_layout);
        this.launcherIconLayout.setOnClickListener(this);
        this.launcherIconCheckBox = (CheckBox) findViewById(R.id.launcher_icon_checkbox);
        this.launcherIconCheckBox.setOnClickListener(this);
        this.githubLayout = (RelativeLayout) findViewById(R.id.setting_github_layout);
        this.githubLayout.setOnClickListener(this);
    }

    private void autoDownloadLayout() {
        a((int) PreferencesUtils.getLong(this, "check_local_freq", 1440L));
        boolean zB = PreferencesUtils.getBoolean(this, "download_only_wifi", DeviceUtil.getInstance().isWifiOnlyEnabled());
        if (zB) {
            this.wifiCheckBox.setChecked(zB);
        }
        boolean zB2 = PreferencesUtils.getBoolean(this, "download_wifi_auto", DeviceUtil.getInstance().isAutoWifiEnabled());
        if (zB2) {
            this.autoDownloadCheckBox.setChecked(zB2);
        }
        boolean isLoggingEnabled = PreferencesUtils.getBoolean(this, "user_logging_enabled", false);
        this.loggingCheckBox.setChecked(isLoggingEnabled);
        this.launcherIconCheckBox.setChecked(isLauncherIconEnabled());
    }

    private boolean isLauncherIconEnabled() {
        ComponentName componentName = new ComponentName(this, "com.foss.fota.GoogleOtaClient");
        int setting = getPackageManager().getComponentEnabledSetting(componentName);
        return setting != PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
    }

    private void setLauncherIconEnabled(boolean enabled) {
        ComponentName componentName = new ComponentName(this, "com.foss.fota.GoogleOtaClient");
        int newState = enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
        getPackageManager().setComponentEnabledSetting(componentName, newState, PackageManager.DONT_KILL_APP);
    }

    private void a(int aboutView) {
        this.checkFrequency = aboutView;
        if (aboutView == 1440) {
            this.scheduleIndex = 0;
            this.scheduleTextView.setText(R.string.setting_autocheck_schedule1);
        } else if (aboutView == 4320) {
            this.scheduleIndex = 1;
            this.scheduleTextView.setText(R.string.setting_autocheck_schedule2);
        } else if (aboutView == 10080) {
            this.scheduleIndex = 2;
            this.scheduleTextView.setText(R.string.setting_autocheck_schedule3);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void startNormalQuery(int type) {
        int i2 = R.string.setting_autocheck_schedule1;
        this.scheduleIndex = type;
        if (type == 0) {
            this.checkFrequency = 1440;
        } else if (type == 1) {
            i2 = R.string.setting_autocheck_schedule2;
            this.checkFrequency = 4320;
        } else if (type == 2) {
            i2 = R.string.setting_autocheck_schedule3;
            this.checkFrequency = 10080;
        }
        this.scheduleTextView.setText(i2);
        PreferencesUtils.putLong((Context) this, "check_local_freq", this.checkFrequency);
        com.foss.fota.update.Alarm.startCheckAlarm(this);
    }

    private void checkRomDamaged() {
        this.materialDialog = new MaterialDialog.Builder(this).b(R.string.setting_autocheck_title).a(R.layout.activity_setting_dialog_schedule_choice, false).getStatus();
        RadioButton radioButton = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton1);
        RadioButton radioButton2 = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton2);
        RadioButton radioButton3 = (RadioButton) this.materialDialog.getType().findViewById(R.id.RadioButton3);
        if (this.scheduleIndex == 0) {
            radioButton.setChecked(true);
        } else if (this.scheduleIndex == 1) {
            radioButton2.setChecked(true);
        } else if (this.scheduleIndex == 2) {
            radioButton3.setChecked(true);
        }
        radioButton.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.SettingActivity.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (SettingActivity.this.materialDialog != null) {
                    SettingActivity.this.materialDialog.cancel();
                }
                SettingActivity.this.startNormalQuery(0);
            }
        });
        radioButton2.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.SettingActivity.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (SettingActivity.this.materialDialog != null) {
                    SettingActivity.this.materialDialog.cancel();
                }
                SettingActivity.this.startNormalQuery(1);
            }
        });
        radioButton3.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.SettingActivity.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view) {
                if (SettingActivity.this.materialDialog != null) {
                    SettingActivity.this.materialDialog.cancel();
                }
                SettingActivity.this.startNormalQuery(2);
            }
        });
        this.materialDialog.show();
    }

    @Override // com.foss.fota.BaseActivity
    public void widgetClick(View view) {
        if (view == this.autoCheckLayout) {
            checkRomDamaged();
            return;
        }
        if (view == this.wifiCheckBox) {
            PreferencesUtils.putBoolean(this, "download_only_wifi", this.wifiCheckBox.isChecked());
            return;
        }
        if (view == this.wifiLayout) {
            this.wifiCheckBox.setChecked(this.wifiCheckBox.isChecked() ? false : true);
            PreferencesUtils.putBoolean(this, "download_only_wifi", this.wifiCheckBox.isChecked());
            return;
        }
        if (view == this.autoDownloadCheckBox) {
            PreferencesUtils.putBoolean(this, "download_wifi_auto", this.autoDownloadCheckBox.isChecked());
            return;
        }
        if (view == this.autoDownloadLayout) {
            this.autoDownloadCheckBox.setChecked(this.autoDownloadCheckBox.isChecked() ? false : true);
            PreferencesUtils.putBoolean(this, "download_wifi_auto", this.autoDownloadCheckBox.isChecked());
        } else if (view == this.loggingCheckBox) {
            PreferencesUtils.putBoolean(this, "user_logging_enabled", this.loggingCheckBox.isChecked());
        } else if (view == this.loggingLayout) {
            this.loggingCheckBox.setChecked(this.loggingCheckBox.isChecked() ? false : true);
            PreferencesUtils.putBoolean(this, "user_logging_enabled", this.loggingCheckBox.isChecked());
        } else if (view == this.aboutView) {
            ComponentName componentName = new ComponentName(com.foss.fota.MaterialDialog.Constants.autoDownloadCheckBox, com.foss.fota.MaterialDialog.Constants.scheduleTextView);
            Intent intent = new Intent();
            intent.setComponent(componentName);
            intent.putExtra("param", (Serializable) com.foss.fota.update.request.RequestParam.autoCheckLayout(this));
            startActivityForResult(intent, 100);
        } else if (view == this.launcherIconCheckBox) {
            setLauncherIconEnabled(this.launcherIconCheckBox.isChecked());
        } else if (view == this.launcherIconLayout) {
            this.launcherIconCheckBox.setChecked(!this.launcherIconCheckBox.isChecked());
            setLauncherIconEnabled(this.launcherIconCheckBox.isChecked());
        } else if (view == this.githubLayout) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/dismal002/fota"));
            startActivity(intent);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onActivityResult(int aboutView, int i2, Intent intent) {
        super.onActivityResult(aboutView, i2, intent);
        // Legacy privacy-policy hooks removed during migration.
    }
}
