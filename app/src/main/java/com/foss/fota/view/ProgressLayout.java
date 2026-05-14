package com.foss.fota.view;

import com.foss.fota.R;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ProgressLayout extends LinearLayout {
    ImageView a;
    RelativeLayout b;
    ProgressRingView c;
    TextView d;
    TextView e;

    public ProgressLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(0);
        LayoutInflater.from(getContext()).inflate(R.layout.main_pro_ring, this);
        this.a = (ImageView) findViewById(R.id.def_img);
        this.a.setTag(Integer.valueOf(R.id.def_img));
        this.b = (RelativeLayout) findViewById(R.id.rl_download_pro);
        this.c = (ProgressRingView) findViewById(R.id.download_pro_ring);
        this.d = (TextView) findViewById(R.id.txt_progress);
        this.e = (TextView) findViewById(R.id.def_version_tip);
        this.d.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/Bariol_Regular.ttf"));
    }

    @Override // android.view.View
    public void setOnClickListener(View.OnClickListener onClickListener) {
        this.a.setOnClickListener(onClickListener);
    }

    public void setDownLoadProgress(int i) {
        this.a.setVisibility(8);
        this.e.setVisibility(8);
        this.b.setVisibility(0);
        this.c.setProgress(i);
        this.d.setText("" + i);
    }

    public void checkSchedule() {
        this.a.setImageResource(R.mipmap.icon_update);
        this.a.setVisibility(0);
        this.e.setText("");
        this.e.setVisibility(0);
        this.b.setVisibility(8);
        this.c.setProgress(0);
        this.d.setText("");
    }

    public void setVersionTip(String str) {
        this.a.setImageDrawable(new ColorDrawable(0));
        this.e.setText(str);
    }

    public int getProgress() {
        return this.c.getProgress();
    }
    public int getType() {
        checkSchedule();
        return 0; // Or whatever value is expected for non-downloading state
    }
}
