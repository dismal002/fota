package com.foss.fota.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import androidx.core.view.PointerIconCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;
import com.foss.fota.FileBrowserActivity;
import com.foss.fota.MyApplication;
import com.foss.fota.R;
import com.foss.fota.SettingActivity;
import com.foss.fota.update.EventMessage;
import com.foss.fota.utils.DeviceUtil;
import com.foss.fota.utils.PreferencesUtils;
import com.foss.fota.utils.ScreenUtils;
import de.greenrobot.event.EventBus;
import java.util.Locale;

public class PopWindowsLayout {
    private PopupWindow a;
    private View b;

    @TargetApi(17)
    public void a(final Activity activity, View view) {
        this.b = LayoutInflater.from(activity).inflate(com.foss.fota.R.layout.settings_pop, (ViewGroup) null);
        this.a = new PopupWindow(this.b, -2, -2, true);
        this.a.setFocusable(true);
        this.a.setBackgroundDrawable(new ColorDrawable(0));
        int iA = ScreenUtils.a(activity) / 60;
        int dimension = (int) activity.getResources().getDimension(com.foss.fota.R.dimen.activity_title_height);
        int iA2 = ScreenUtils.a();
        int iA3 = (int) ScreenUtils.a(activity.getBaseContext(), 8.0f);
        int i = 53;
        if (Build.VERSION.SDK_INT >= 17 && 1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            i = 51;
        }
        this.a.showAtLocation(view, i, iA, (dimension + iA2) - iA3);
        LinearLayout linearLayout = (LinearLayout) this.b.findViewById(com.foss.fota.R.id.pop_file_select);
        LinearLayout linearLayout2 = (LinearLayout) this.b.findViewById(com.foss.fota.R.id.pop_full_check);
        LinearLayout linearLayout3 = (LinearLayout) this.b.findViewById(com.foss.fota.R.id.pop_setting);
        LinearLayout linearLayout4 = (LinearLayout) this.b.findViewById(com.foss.fota.R.id.pop_exit);
        if (!DeviceUtil.getInstance().isLocalUpdateEnabled()) {
            linearLayout.setVisibility(8);
        }
        if (!DeviceUtil.getInstance().isExitEnabled()) {
            linearLayout4.setVisibility(8);
        }
        if (PreferencesUtils.b((Context) activity, "isFull", 0) == 1) {
            linearLayout2.setVisibility(0);
        }
        linearLayout.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.view.PopWindowsLayout.1
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                PopWindowsLayout.this.a.dismiss();
                PopWindowsLayout.this.a = null;
                int iF = com.foss.fota.update.Status.f(MyApplication.getInstance());
                if (iF == 5 || iF == 2) {
                    Toast.makeText(activity, com.foss.fota.R.string.tips_abDownOrInstall, 1).show();
                } else {
                    activity.startActivity(new Intent(activity.getBaseContext(), (Class<?>) FileBrowserActivity.class));
                }
            }
        });
        linearLayout2.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.view.PopWindowsLayout.2
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                PopWindowsLayout.this.a.dismiss();
                PopWindowsLayout.this.a = null;
                if (com.foss.fota.update.Status.f(MyApplication.getInstance()) == 5) {
                    Toast.makeText(activity, com.foss.fota.R.string.tips_abInstall, 1).show();
                } else {
                    EventBus.getDefault().post(new EventMessage(100, PointerIconCompat.TYPE_CROSSHAIR, 0L, 0L, null));
                }
            }
        });
        linearLayout3.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.view.PopWindowsLayout.3
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                PopWindowsLayout.this.a.dismiss();
                PopWindowsLayout.this.a = null;
                activity.startActivity(new Intent(activity.getBaseContext(), (Class<?>) SettingActivity.class));
            }
        });
        linearLayout4.setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.view.PopWindowsLayout.4
            @Override // android.view.View.OnClickListener
            public void onClick(View view2) {
                activity.finish();
            }
        });
    }
}
