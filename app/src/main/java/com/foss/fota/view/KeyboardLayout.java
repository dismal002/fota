package com.foss.fota.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/* JADX INFO: loaded from: classes.dex */
public class KeyboardLayout extends RelativeLayout {
    private static final String a = KeyboardLayout.class.getSimpleName();
    private boolean b;
    private boolean c;
    private int d;
    private a e;

    public interface a {
        void a(int i);
    }

    public KeyboardLayout(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public KeyboardLayout(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    public void setOnkbdStateListener(a aVar) {
        this.e = aVar;
    }

    @Override // android.widget.RelativeLayout, android.view.ViewGroup, android.view.View
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (!this.b) {
            this.b = true;
            this.d = i4;
            if (this.e != null) {
                this.e.a(-1);
            }
        } else {
            this.d = this.d < i4 ? i4 : this.d;
        }
        if (this.b && this.d > i4) {
            this.c = true;
            if (this.e != null) {
                this.e.a(-3);
            }
        }
        if (this.b && this.c && this.d == i4) {
            this.c = false;
            if (this.e != null) {
                this.e.a(-2);
            }
        }
    }
}
