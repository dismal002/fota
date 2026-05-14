package com.foss.fota.view;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foss.fota.update.query.QueryInfo;
import com.foss.fota.R;
import com.foss.fota.utils.FileUtil;
import com.foss.fota.update.model.VersionModel;
import java.util.Locale;

/* JADX INFO: loaded from: classes.dex */
public class FooterLayout extends LinearLayout {
    TextView a;
    TextView b;
    TextView c;
    LinearLayout d;

    public FooterLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(0);
    }

    @Override // android.view.View
    protected void onFinishInflate() {
        super.onFinishInflate();
        LayoutInflater.from(getContext()).inflate(R.layout.main_footer, this);
        this.a = (TextView) findViewById(R.id.bt_check);
        this.a.setTag(0);
        this.d = (LinearLayout) findViewById(R.id.hl_bt_layout);
        this.d.setVisibility(8);
        this.b = (TextView) findViewById(R.id.button_left);
        this.b.setTag(2);
        this.c = (TextView) findViewById(R.id.button_right);
        this.c.setTag(5);
        if (1 == TextUtils.getLayoutDirectionFromLocale(Locale.getDefault())) {
            this.b.setBackgroundResource(R.drawable.button_right_selector);
            this.c.setBackgroundResource(R.drawable.button_left_selector);
        }
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.a.setOnClickListener(onClickListener);
        this.b.setOnClickListener(onClickListener);
        this.c.setOnClickListener(onClickListener);
    }

    public void setState(boolean z) {
        if (z) {
            this.a.setVisibility(0);
            this.d.setVisibility(8);
        } else {
            this.a.setVisibility(8);
            this.d.setVisibility(0);
        }
    }

    public void a(int i) {
        switch (i) {
            case 0:
                this.a.setTag(0);
                this.a.setText(R.string.check_now);
                setState(true);
                break;
            case 1:
                b(i);
                setState(true);
                break;
            case 2:
                this.b.setTag(2);
                this.c.setTag(5);
                this.b.setText(R.string.btn_cancel);
                this.c.setText(R.string.btn_pause);
                setState(false);
                break;
            case 3:
                this.b.setTag(3);
                this.c.setTag(5);
                this.b.setText(R.string.btn_resume);
                this.c.setText(R.string.btn_pause);
                setState(false);
                break;
            case 4:
                setState(false);
                this.b.setTag(7);
                this.c.setTag(8);
                this.b.setText(R.string.update_later);
                this.c.setText(R.string.update_now);
                break;
            case 5:
                this.a.setVisibility(4);
                this.d.setVisibility(8);
                break;
            case 6:
                setState(true);
                this.a.setTag(12);
                this.a.setText(R.string.update_now);
                break;
        }
    }

    public void setQueryType(int type) {
        a(type);
    }

    private void b(int i) {
        if (i == 1) {
            this.a.setTag(1);
            try {
                VersionModel versionModel = QueryInfo.getInstance(getContext()).getVersionModel();
                this.a.setText(getResources().getString(R.string.btn_download) + "(" + FileUtil.a(versionModel != null ? versionModel.getFilesize() : 0L) + ")");
            } catch (Exception e) {
                this.a.setText(R.string.btn_download);
            }
            setState(true);
        }
    }

    public void c(Context context) {
        a(1);
    }
}
