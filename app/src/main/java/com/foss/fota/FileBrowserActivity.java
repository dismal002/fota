package com.foss.fota;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.foss.fota.utils.Trace;
import com.foss.fota.utils.StorageUtil;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/* JADX INFO: loaded from: classes.dex */
public class FileBrowserActivity extends BaseActivity {
    private static int d;
    boolean b = false;
    private ListView f;
    private List<b> g;
    private String h;
    private String i;
    private String j;
    private TextView k;
    private static final String c = FileBrowserActivity.class.getSimpleName();
    private static Stack<Integer> e = new Stack<>();

    @Override // com.foss.fota.BaseActivity, androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_file_browser);
        j();
        n();
    }

    public void onBack(View view) {
        finish();
    }

    private void j() {
        this.i = StorageUtil.b((Context) this, true);
        this.j = StorageUtil.b((Context) this, false);
        this.g = new ArrayList();
        boolean hasExternal = !TextUtils.isEmpty(this.i);
        if (hasExternal) {
            this.g.add(new b(getString(R.string.out_sdcard), this.i, false));
        }
        if (!TextUtils.isEmpty(this.j)) {
            this.g.add(new b(getString(R.string.inner_sdcard), this.j, false));
        }
        this.f = (ListView) findViewById(R.id.file_list_view);
        ((TextView) findViewById(R.id.title_text)).setText(R.string.option_file_select);
        this.k = (TextView) findViewById(R.id.option_file_select_dir);
        this.k.setText(R.string.selected_update_zip);
        k();
    }

    private void k() {
        this.f.setAdapter((ListAdapter) new a(this));
        this.f.setOnItemClickListener(new AdapterView.OnItemClickListener() { // from class: com.foss.fota.FileBrowserActivity.1
            @Override // android.widget.AdapterView.OnItemClickListener
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                char c2;
                FileBrowserActivity.e.push(Integer.valueOf(FileBrowserActivity.d));
                FileBrowserActivity.this.b = false;
                Trace.d(FileBrowserActivity.c, "initListViews, " + ((b) FileBrowserActivity.this.g.get(i)).b);
                if (((b) FileBrowserActivity.this.g.get(i)).c) {
                    FileBrowserActivity.this.a(((b) FileBrowserActivity.this.g.get(i)).b);
                    return;
                }
                File[] fileArrListFiles = new File(((b) FileBrowserActivity.this.g.get(i)).b).listFiles();
                if (fileArrListFiles != null) {
                    for (int i2 = 0; i2 < fileArrListFiles.length; i2++) {
                        if (fileArrListFiles[i2].isDirectory()) {
                            c2 = 1;
                            break;
                        } else {
                            if (fileArrListFiles[i2].getName().toLowerCase(Locale.US).endsWith(".zip")) {
                                c2 = 1;
                                break;
                            }
                        }
                    }
                    c2 = 0;
                } else {
                    c2 = 0;
                }
                if (c2 > 0) {
                    FileBrowserActivity.this.h = ((b) FileBrowserActivity.this.g.get(i)).b;
                    try {
                        FileBrowserActivity.this.g = FileBrowserActivity.this.l();
                    } catch (Throwable th) {
                        th.printStackTrace();
                    }
                    FileBrowserActivity.this.f.setAdapter((ListAdapter) FileBrowserActivity.this.new a(FileBrowserActivity.this));
                } else if (Build.VERSION.SDK_INT >= 23 && FileBrowserActivity.this.checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
                    FileBrowserActivity.this.requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 2);
                } else {
                    Toast.makeText(FileBrowserActivity.this.getBaseContext(), FileBrowserActivity.this.getString(R.string.empty_directory), 1).show();
                }
            }
        });
        this.f.setOnScrollListener(new AbsListView.OnScrollListener() { // from class: com.foss.fota.FileBrowserActivity.2
            @Override // android.widget.AbsListView.OnScrollListener
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            }

            @Override // android.widget.AbsListView.OnScrollListener
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (i == 0) {
                    int unused = FileBrowserActivity.d = FileBrowserActivity.this.f.getFirstVisiblePosition();
                }
            }
        });
    }

    @Override // com.foss.fota.BaseActivity
    public void initData() {
    }

    @Override // com.foss.fota.BaseActivity
    public void widgetClick(View view) {
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    public void onBackPressed() {
        if (this.b || TextUtils.isEmpty(this.h)) {
            e.clear();
            super.onBackPressed();
            return;
        }
        if (!TextUtils.isEmpty(this.j) && this.h.equals(this.j)) {
            this.b = true;
            this.h = new File(this.j).getParent();
            j();
            return;
        }
        if (!TextUtils.isEmpty(this.i) && this.h.equals(this.i)) {
            this.b = true;
            this.h = new File(this.i).getParent();
            j();
            return;
        }
        this.b = false;
        this.h = new File(this.h).getParent();
        try {
            this.g = l();
        } catch (Throwable th) {
            th.printStackTrace();
        }
        this.f.setAdapter((ListAdapter) new a(this));
        if (e.size() > 0) {
            this.f.setSelection(e.pop().intValue());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public List<b> l() throws Throwable {
        ArrayList arrayList = new ArrayList();
        new File(this.h).listFiles();
        File[] fileArrM = m();
        if (!TextUtils.isEmpty(this.j) && this.h.contains(this.j)) {
            this.k.setVisibility(0);
            this.k.setText(this.h.replace(this.j, getString(R.string.inner_sdcard)));
        } else if (!TextUtils.isEmpty(this.i) && this.h.contains(this.i)) {
            this.k.setVisibility(0);
            this.k.setText(this.h.replace(this.i, getString(R.string.out_sdcard)));
        }
        if (fileArrM != null) {
            for (int i = 0; i < fileArrM.length; i++) {
                if (fileArrM[i].isDirectory()) {
                    arrayList.add(new b(fileArrM[i].getName(), fileArrM[i].getPath(), false));
                } else if (fileArrM[i].getName().toLowerCase(Locale.US).endsWith(".zip")) {
                    arrayList.add(new b(fileArrM[i].getName(), fileArrM[i].getPath(), true));
                }
            }
        }
        return arrayList;
    }

    private File[] m() throws Throwable {
        File[] fileArrListFiles = new File(this.h).listFiles();
        ArrayList arrayList = new ArrayList();
        if (this.h.equals("/storage")) {
            if (StorageUtil.hasExternalSdCard(getApplicationContext())) {
                if (StorageUtil.isExternalStorageMounted(getApplicationContext())) {
                    return new File[]{new File(StorageUtil.a(getApplicationContext(), false))};
                }
                return new File[0];
            }
            List<StorageUtil.StorageVolumeInfo> listB = StorageUtil.getStatus();
            if (listB != null) {
                for (int i = 0; i < listB.size(); i++) {
                    String str = listB.get(i).path;
                    if (str != null && "mounted".equals(StorageUtil.a(this, str))) {
                        arrayList.add(str);
                    }
                }
                if (arrayList.size() > 0) {
                    File[] fileArr = new File[arrayList.size()];
                    for (int i2 = 0; i2 < fileArr.length; i2++) {
                        fileArr[i2] = new File((String) arrayList.get(i2));
                    }
                    return fileArr;
                }
                return new File[]{Environment.getExternalStorageDirectory()};
            }
            return new File[]{Environment.getExternalStorageDirectory()};
        }
        return fileArrListFiles;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void a(String str) {
        Intent intent = new Intent();
        intent.putExtra("selected_file", str);
        intent.setClass(this, SdcardUpdateActivity.class);
        startActivityForResult(intent, 1);
        finish();
    }

    private void n() {
        if (Build.VERSION.SDK_INT == 23) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE"}, 1);
            }
        } else if (Build.VERSION.SDK_INT >= 24) {
            if (checkSelfPermission("android.permission.WRITE_EXTERNAL_STORAGE") != 0) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 2);
            }
            if (checkSelfPermission("android.permission.READ_EXTERNAL_STORAGE") != 0) {
                requestPermissions(new String[]{"android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.READ_EXTERNAL_STORAGE"}, 2);
            }
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity, androidx.core.app.ActivityCompat.OnRequestPermissionsResultCallback
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        super.onRequestPermissionsResult(i, strArr, iArr);
    }

    public class b {
        String a;
        String b;
        boolean c;

        public b(String str, String str2, boolean z) {
            this.a = str;
            this.b = str2;
            this.c = z;
        }
    }

    public class a extends BaseAdapter {
        private LayoutInflater b;

        public a(Context context) {
            this.b = LayoutInflater.from(context);
        }

        @Override // android.widget.Adapter
        public int getCount() {
            return FileBrowserActivity.this.g.size();
        }

        @Override // android.widget.Adapter
        public Object getItem(int i) {
            return FileBrowserActivity.this.g.get(i);
        }

        @Override // android.widget.Adapter
        public long getItemId(int i) {
            return 0L;
        }

        @Override // android.widget.Adapter
        public View getView(int i, View view, ViewGroup viewGroup) {
            C0013a c0013a;
            if (view == null) {
                c0013a = new C0013a();
                view = this.b.inflate(R.layout.browser_file_list_item, (ViewGroup) null);
                c0013a.a = (ImageView) view.findViewById(R.id.img);
                c0013a.b = (TextView) view.findViewById(R.id.title);
                view.setTag(c0013a);
            } else {
                c0013a = (C0013a) view.getTag();
            }
            b bVar = (b) getItem(i);
            if (bVar.c) {
                c0013a.a.setImageResource(R.mipmap.ex_doc);
            } else {
                c0013a.a.setImageResource(R.mipmap.ex_folder);
            }
            c0013a.b.setText(bVar.a);
            return view;
        }

        /* JADX INFO: renamed from: com.foss.fota.FileBrowserActivity$a$a, reason: collision with other inner class name */
        public final class C0013a {
            public ImageView a;
            public TextView b;

            public C0013a() {
            }
        }
    }
}
