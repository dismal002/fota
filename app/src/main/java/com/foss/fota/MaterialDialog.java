package com.foss.fota;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.core.view.GravityCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.foss.fota.utils.Trace;
import com.foss.fota.config.Const;

public class MaterialDialog extends Dialog {
    static TextView titleView;
    static Builder builder;
    private View contentView;

    public interface DialogActionListener {
        void a(MaterialDialog aVar, DialogAction dialogAction);
    }

    public MaterialDialog(Context context, int i) {
        super(context, i);
    }

    public static MaterialDialog create(final Builder builder) {
        LayoutInflater layoutInflater = (LayoutInflater) builder.o.getSystemService("layout_inflater");
        final MaterialDialog aVar = new MaterialDialog(builder.o, R.style.Dialog);
        View viewInflate = layoutInflater.inflate(R.layout.dialog, (ViewGroup) null);
        aVar.contentView = viewInflate;
        aVar.addContentView(viewInflate, new ViewGroup.LayoutParams(-1, -2));
        titleView = (TextView) viewInflate.findViewById(R.id.title);
        titleView.setText(builder.c);
        titleView.setGravity(builder.a);
        ((RelativeLayout) viewInflate.findViewById(R.id.bottom)).setGravity(builder.b);
        if (builder.e != null) {
            ((TextView) viewInflate.findViewById(R.id.positiveButton)).setText(builder.e);
            viewInflate.findViewById(R.id.positiveButton).setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.MaterialDialog.1
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (builder.positiveListener != null) {
                        builder.positiveListener.a(aVar, DialogAction.POSITIVE);
                    }
                    aVar.dismiss();
                }
            });
        } else {
            viewInflate.findViewById(R.id.positiveButton).setVisibility(8);
        }
        if (builder.f != null) {
            ((TextView) viewInflate.findViewById(R.id.negativeButton)).setText(builder.f);
            viewInflate.findViewById(R.id.negativeButton).setOnClickListener(new View.OnClickListener() { // from class: com.foss.fota.MaterialDialog.2
                @Override // android.view.View.OnClickListener
                public void onClick(View view) {
                    if (builder.negativeListener != null) {
                        builder.negativeListener.a(aVar, DialogAction.NEGATIVE);
                    }
                    aVar.dismiss();
                }
            });
        } else {
            viewInflate.findViewById(R.id.negativeButton).setVisibility(8);
        }
        if (builder.d != null) {
            ((TextView) viewInflate.findViewById(R.id.message)).setText(builder.d);
        } else if (builder.g != null) {
            ((LinearLayout) viewInflate.findViewById(R.id.content)).removeAllViews();
            ((LinearLayout) viewInflate.findViewById(R.id.content)).addView(builder.g, new ViewGroup.LayoutParams(-1, -1));
        }
        if (builder.e == null && builder.f == null) {
            viewInflate.findViewById(R.id.bottom).setVisibility(8);
        }
        if (builder.c == null) {
            viewInflate.findViewById(R.id.title).setVisibility(8);
        }
        if (builder.l != null) {
            aVar.setOnKeyListener(builder.l);
        }
        if (builder.m != null) {
            aVar.setOnCancelListener(builder.m);
        }
        if (builder.n != null) {
            aVar.setOnDismissListener(builder.n);
        }
        aVar.setCancelable(builder.h);
        aVar.setContentView(viewInflate);
        Display defaultDisplay = ((WindowManager) builder.o.getSystemService("window")).getDefaultDisplay();
        WindowManager.LayoutParams attributes = aVar.getWindow().getAttributes();
        attributes.width = (int) (((double) defaultDisplay.getWidth()) * 0.85d);
        aVar.getWindow().setAttributes(attributes);
        return aVar;
    }

    @Override // android.app.Dialog
    public final void setTitle(CharSequence charSequence) {
        titleView.setText(charSequence);
    }

    public final View getContentView() {
        return this.contentView;
    }

    // Compatibility aliases from the decompiler/migration.
    public final View a() { return getContentView(); }
    public final View getType() { return getContentView(); }

    public static final class Constants {
        public static final String d = Const.PRIVACY_POLICY_PACKAGE;
        public static final String g = Const.UPDATE_ZIP_PATH;
        public static final String h = Const.LOCAL_SD_UPDATE_ZIP_PATH;
        public static final String i = Const.ACTION_OUT_UPDATE_SUCCESS;
        public static final String k = Const.PERMISSION_FOTA;
        public static final String l = Const.ALARM_ACTION;
        public static final String autoDownloadCheckBox = Const.PRIVACY_POLICY_PACKAGE;
        public static final String scheduleTextView = Const.GDPR_ACTIVITY;
    }

    public static class Builder {
        int a = GravityCompat.START;
        int b = GravityCompat.END;
        String c;
        String d;
        String e;
        String f;
        View g;
        boolean h = true;
        boolean i;
        DialogInterface.OnKeyListener l = null;
        DialogInterface.OnCancelListener m = null;
        DialogInterface.OnDismissListener n = null;
        private Context o;
        DialogActionListener positiveListener;
        DialogActionListener negativeListener;

        public Builder(Context context) {
            this.o = context;
        }

        public Builder a(DialogInterface.OnDismissListener onDismissListener) {
            this.n = onDismissListener;
            return this;
        }

        public Builder a(String str) {
            this.d = str;
            return this;
        }

        public View a() {
            return this.g;
        }

        public Builder a(int i) {
            this.d = (String) this.o.getText(i);
            return this;
        }

        public Builder b(int i) {
            this.c = (String) this.o.getText(i);
            return this;
        }

        public Builder b(String str) {
            this.c = str;
            return this;
        }

        public Builder c(int i) {
            this.a = i;
            return this;
        }

        public Builder d(int i) {
            this.b = i;
            return this;
        }

        public Builder a(int i, boolean z) {
            this.g = LayoutInflater.from(this.o).inflate(i, (ViewGroup) null);
            this.i = z;
            return this;
        }

        public Builder e(int i) {
            this.e = (String) this.o.getText(i);
            return this;
        }

        public Builder a(boolean z) {
            this.h = z;
            return this;
        }

        public Builder f(int i) {
            return i == 0 ? this : a(this.o.getText(i));
        }

        public Builder a(CharSequence charSequence) {
            this.f = (String) charSequence;
            return this;
        }

        public Builder a(DialogActionListener listener) {
            return setPositiveListener(listener);
        }

        public Builder setPositiveListener(DialogActionListener listener) {
            this.positiveListener = listener;
            return this;
        }

        public Builder setNegativeListener(DialogActionListener listener) {
            this.negativeListener = listener;
            return this;
        }

        public MaterialDialog build() {
            Trace.d("MaterialDialog", "MaterialDialog create()");
            return MaterialDialog.create(this);
        }

        public MaterialDialog show() {
            MaterialDialog aVarB = build();
            aVarB.show();
            return aVarB;
        }

        // Readable API wrappers
        public Builder contentLayout(int titleResId) { return b(titleResId); }
        public Builder contentLayout(String title) { return b(title); }
        public Builder message(int messageResId) { return a(messageResId); }
        public Builder message(String message) { return a(message); }
        public Builder footerLayout(int positiveTextResId) { return e(positiveTextResId); }
        public Builder positiveText(int positiveTextResId) { return e(positiveTextResId); }
        public Builder negativeText(int negativeTextResId) { return f(negativeTextResId); }

        // Compatibility aliases from the decompiler/migration.
        public Builder selectedFile(int positiveTextResId) { return e(positiveTextResId); }
        public Builder materialDialog(int negativeTextResId) { return f(negativeTextResId); }
        public Builder progressLayout(int negativeTextResId) { return f(negativeTextResId); }
        public Builder progressLayout(DialogActionListener listener) { return setNegativeListener(listener); }
        public Builder contentLayout(DialogActionListener listener) { return setPositiveListener(listener); }
        public MaterialDialog getStatus() { return build(); }
        public MaterialDialog b() { return build(); }
        public MaterialDialog c() { return show(); }
    }
}
