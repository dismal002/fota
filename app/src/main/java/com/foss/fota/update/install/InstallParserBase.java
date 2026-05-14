package com.foss.fota.update.install;

import android.content.Context;

public class InstallParserBase {
    public static final String a = InstallParserBase.class.getSimpleName();
    public String b;

    public int a(Context context, String str, String str2) {
        return 405;
    }

    public String a() {
        return this.b;
    }

    // Compatibility alias
    public String getType() {
        return a();
    }

    public void a(String str) {
        this.b = str;
    }
}
