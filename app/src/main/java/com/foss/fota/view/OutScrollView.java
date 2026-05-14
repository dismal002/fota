package com.foss.fota.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class OutScrollView extends ScrollView {
    float a;
    float b;
    private int c;

    public OutScrollView(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public OutScrollView(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.c = 5000;
    }

    @Override // android.view.ViewGroup, android.view.View
    public boolean dispatchTouchEvent(MotionEvent motionEvent) {
        super.dispatchTouchEvent(motionEvent);
        switch (motionEvent.getAction()) {
            case 0:
                this.b = motionEvent.getY();
                break;
            case 1:
                this.a = this.b - motionEvent.getY();
                smoothScrollTo(0, this.a > 0.0f ? this.c : 0);
                break;
        }
        return true;
    }
}
