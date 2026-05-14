package com.foss.fota.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.foss.fota.R;

public class TitleContentView extends LinearLayout {
    private TextView a;
    private TextView b;
    private CheckBox c;
    private ImageView d;

    public TitleContentView(Context context) {
        super(context);
        b();
    }

    public TitleContentView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        b();
        setViewsFromAttrs(attributeSet);
    }

    public TitleContentView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        b();
        setViewsFromAttrs(attributeSet);
    }

    private void b() {
        inflate(getContext(), R.layout.title_content_view_layout, this);
        this.a = (TextView) findViewById(R.id.title);
        this.b = (TextView) findViewById(R.id.content);
        this.c = (CheckBox) findViewById(R.id.checkBox);
        this.d = (ImageView) findViewById(R.id.arrow);
    }

    private void setViewsFromAttrs(AttributeSet attributeSet) {
        TypedArray typedArrayObtainStyledAttributes = getContext().obtainStyledAttributes(attributeSet, R.styleable.TitleContentView);
        String string = typedArrayObtainStyledAttributes.getString(R.styleable.TitleContentView_desc);
        if (!TextUtils.isEmpty(string)) {
            setTitle(string);
        }
        String string2 = typedArrayObtainStyledAttributes.getString(R.styleable.TitleContentView_content);
        if (!TextUtils.isEmpty(string2)) {
            setContent(string2);
        } else {
            setContentVisible(8);
        }
        if (typedArrayObtainStyledAttributes.getBoolean(R.styleable.TitleContentView_showBox, false)) {
            showCheckBox();
        }
        typedArrayObtainStyledAttributes.recycle();
    }

    public void setTitle(String str) {
        this.a.setText(str);
    }

    public void setContent(String str) {
        this.b.setText(str);
    }

    public void setContentVisible(int i) {
        this.b.setVisibility(i);
    }

    public void checkSchedule() {
        this.c.setVisibility(0);
        this.d.setVisibility(8);
    }

    private void showCheckBox() {
        checkSchedule();
    }
}
