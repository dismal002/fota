package com.foss.fota.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class ProgressRingView extends View {
    private RectF a;
    private boolean b;
    private Paint c;
    private int d;

    public ProgressRingView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        this.b = true;
        this.d = 0;
    }

    @Override // android.view.View
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (this.b) {
            this.a = new RectF(0.0f, 0.0f, getWidth(), getHeight());
            this.c = new Paint(1);
            this.c.setColor(-1);
            this.c.setAntiAlias(true);
            this.c.setStyle(Paint.Style.FILL);
            this.b = false;
        }
        canvas.drawArc(this.a, 272.0f, (this.d * 360) / 100, true, this.c);
        canvas.save();
        canvas.restore();
    }

    public int getProgress() {
        return this.d;
    }

    public void setProgress(int i) {
        if (i >= 0 && i <= 100) {
            this.d = i;
            invalidate();
        }
    }
}
