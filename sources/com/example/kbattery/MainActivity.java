package com.example.kbattery;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.example.kbattery.utils.DialogUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationBarView;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rikka.shizuku.Shizuku;

/* loaded from: classes3.dex */
public class MainActivity extends AppCompatActivity {
    private static final String PREF_FIRST_LAUNCH = "is_first_launch";
    private static final String TAG = "MainActivity";
    private BottomNavigationView bottomNav;
    private ViewPager2 viewPager;
    public static boolean isShizukuAvailable = false;
    public static boolean isShizukuActive = false;
    public static boolean isShizukuGranted = false;
    private OkHttpClient client = new OkHttpClient();
    private Handler mainHandler = new Handler(Looper.getMainLooper());
    private String sourceCode = "";
    private String b = "";
    private String c = "";

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(1792);
        int currentNightMode = getResources().getConfiguration().uiMode & 48;
        if (currentNightMode == 16) {
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() | 8192);
        } else {
            getWindow().setStatusBarColor(getResources().getColor(R.color.background));
            getWindow().getDecorView().setSystemUiVisibility(getWindow().getDecorView().getSystemUiVisibility() & (-8193));
        }
        getWindow().setNavigationBarColor(0);
        setContentView(R.layout.activity_main);
        checkShizukuStatus();
        this.viewPager = (ViewPager2) findViewById(R.id.view_pager);
        this.bottomNav = (BottomNavigationView) findViewById(R.id.bottom_navigation);
        this.viewPager.setAdapter(new ViewPagerAdapter(this));
        this.viewPager.setOffscreenPageLimit(2);
        this.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() { // from class: com.example.kbattery.MainActivity.1
            @Override // androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position >= 0 && position < MainActivity.this.bottomNav.getMenu().size()) {
                    MainActivity.this.bottomNav.getMenu().getItem(position).setChecked(true);
                }
            }
        });
        this.bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda9
            @Override // com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener
            public final boolean onNavigationItemSelected(MenuItem menuItem) {
                return this.f$0.m176lambda$onCreate$0$comexamplekbatteryMainActivity(menuItem);
            }
        });
        checkFirstLaunch();
        checkForUpdate();
    }

    /* renamed from: lambda$onCreate$0$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ boolean m176lambda$onCreate$0$comexamplekbatteryMainActivity(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.page_1) {
            this.viewPager.setCurrentItem(0);
        } else if (itemId == R.id.page_3) {
            this.viewPager.setCurrentItem(1);
        } else if (itemId == R.id.page_2) {
            this.viewPager.setCurrentItem(2);
        }
        return true;
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
    }

    private void checkFirstLaunch() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        SharedPreferences prefs = getSharedPreferences("AppPreferences", 0);
        boolean isFirstLaunch = prefs.getBoolean(PREF_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            showFirstUseGuideDialog();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putBoolean(PREF_FIRST_LAUNCH, false);
            editor.apply();
        }
    }

    private void showFirstUseGuideDialog() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = DialogUtils.createConfirmDialog(this, "欢迎使用", "检测到您是第一次使用，请先阅读使用手册了解如何操作，否则后果自负", "去阅读", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m177xbba23380(dialogInterface, i);
            }
        }, null, null).setCancelable(false);
        DialogUtils.showWithBlurEffect(builder);
    }

    /* renamed from: lambda$showFirstUseGuideDialog$1$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m177xbba23380(DialogInterface dialog, int which) {
        openUserManual();
        dialog.dismiss();
    }

    private void openUserManual() {
        try {
            Intent intent = new Intent("android.intent.action.VIEW", Uri.parse("https://kbattery.mtaokj.com/"));
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "打开使用手册失败", e);
            showToast("无法打开使用手册");
        }
    }

    private void checkForUpdate() {
        new Thread(new Runnable() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda7
            @Override // java.lang.Runnable
            public final void run() throws NumberFormatException {
                this.f$0.m173lambda$checkForUpdate$3$comexamplekbatteryMainActivity();
            }
        }).start();
    }

    /* renamed from: lambda$checkForUpdate$3$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m173lambda$checkForUpdate$3$comexamplekbatteryMainActivity() throws NumberFormatException {
        try {
            this.sourceCode = fetchWebSource("https://sharechain.qq.com/ca3a9cdfc255301103afe5deb332af16");
            this.b = extractContent(this.sourceCode, "【", "】");
            this.c = extractContent(this.sourceCode, "《", "》");
            checkUpdateStatus();
        } catch (IOException e) {
            e.printStackTrace();
            this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda5
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m172lambda$checkForUpdate$2$comexamplekbatteryMainActivity();
                }
            });
        }
    }

    /* renamed from: lambda$checkForUpdate$2$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m172lambda$checkForUpdate$2$comexamplekbatteryMainActivity() {
        showToast("检查更新失败");
    }

    private String fetchWebSource(String url) throws IOException {
        Request request = new Request.Builder().url(url).build();
        Response response = this.client.newCall(request).execute();
        if (!response.isSuccessful()) {
            throw new IOException("Unexpected code " + response);
        }
        return response.body().string();
    }

    private String extractContent(String source, String start, String end) {
        int startIndex;
        int startIndex2;
        int endIndex;
        return (source == null || start == null || end == null || (startIndex = source.indexOf(start)) == -1 || (endIndex = source.indexOf(end, (startIndex2 = startIndex + start.length()))) == -1) ? "" : source.substring(startIndex2, endIndex);
    }

    private void checkUpdateStatus() throws NumberFormatException {
        try {
            double versionCode = Double.parseDouble(this.b);
            if (versionCode == 1.182d) {
                this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m175lambda$checkUpdateStatus$4$comexamplekbatteryMainActivity();
                    }
                });
            } else {
                this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda4
                    @Override // java.lang.Runnable
                    public final void run() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                        this.f$0.showUpdateDialog();
                    }
                });
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda4
                @Override // java.lang.Runnable
                public final void run() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                    this.f$0.showUpdateDialog();
                }
            });
        }
    }

    /* renamed from: lambda$checkUpdateStatus$4$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m175lambda$checkUpdateStatus$4$comexamplekbatteryMainActivity() {
        showToast("已是最新版本");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUpdateDialog() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_update, (ViewGroup) null);
        TextView titleTv = (TextView) dialogView.findViewById(R.id.dialog_title);
        TextView messageTv = (TextView) dialogView.findViewById(R.id.dialog_message);
        Button cancelBtn = (Button) dialogView.findViewById(R.id.dialog_cancel);
        Button confirmBtn = (Button) dialogView.findViewById(R.id.dialog_confirm);
        titleTv.setText("发现更新");
        messageTv.setText(this.c.isEmpty() ? "有新版本可用，建议更新" : this.c);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setView(dialogView).setCancelable(false);
        final AlertDialog updateDialog = DialogUtils.showWithBlurEffect(builder);
        cancelBtn.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                updateDialog.dismiss();
            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m179lambda$showUpdateDialog$6$comexamplekbatteryMainActivity(updateDialog, view);
            }
        });
    }

    /* renamed from: lambda$showUpdateDialog$6$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m179lambda$showUpdateDialog$6$comexamplekbatteryMainActivity(AlertDialog updateDialog, View v) {
        openDownloadPage();
        updateDialog.dismiss();
    }

    private void openDownloadPage() {
        startActivity(new Intent("android.intent.action.VIEW", Uri.parse("https://www.123865.com/s/M54Ajv-nsaWA")));
    }

    private void showToast(String message) {
        Toast.makeText(this, message, 0).show();
    }

    private void checkShizukuStatus() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        try {
            isShizukuAvailable = Shizuku.pingBinder();
            if (isShizukuAvailable) {
                boolean z = true;
                isShizukuActive = true;
                if (Shizuku.checkSelfPermission() != 0) {
                    z = false;
                }
                isShizukuGranted = z;
                if (!isShizukuGranted) {
                    Shizuku.addRequestPermissionResultListener(new Shizuku.OnRequestPermissionResultListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda2
                        @Override // rikka.shizuku.Shizuku.OnRequestPermissionResultListener
                        public final void onRequestPermissionResult(int i, int i2) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                            this.f$0.m174lambda$checkShizukuStatus$7$comexamplekbatteryMainActivity(i, i2);
                        }
                    });
                    Shizuku.requestPermission(1001);
                }
            } else {
                showShizukuErrorDialog("Shizuku服务未启动或未安装");
            }
        } catch (Throwable e) {
            Log.e(TAG, "Shizuku检查失败", e);
            showShizukuErrorDialog("Shizuku未安装");
        }
    }

    /* renamed from: lambda$checkShizukuStatus$7$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m174lambda$checkShizukuStatus$7$comexamplekbatteryMainActivity(int requestCode, int grantResult) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (requestCode == 1001) {
            isShizukuGranted = grantResult == 0;
            if (!isShizukuGranted) {
                showShizukuErrorDialog("Shizuku权限被拒绝");
            }
        }
    }

    private void showShizukuErrorDialog(String message) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setTitle((CharSequence) "Shizuku服务异常").setMessage((CharSequence) message).setCancelable(false).setPositiveButton((CharSequence) "退出应用", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.MainActivity$$ExternalSyntheticLambda8
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m178x101f6325(dialogInterface, i);
            }
        });
        DialogUtils.showWithBlurEffect(builder);
    }

    /* renamed from: lambda$showShizukuErrorDialog$8$com-example-kbattery-MainActivity, reason: not valid java name */
    /* synthetic */ void m178x101f6325(DialogInterface dialog, int which) {
        finishAffinity();
    }
}
