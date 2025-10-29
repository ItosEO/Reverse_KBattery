package com.example.kbattery;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.kbattery.utils.DialogUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import rikka.shizuku.Shizuku;

/* loaded from: classes3.dex */
public class ModeActivity extends AppCompatActivity {
    private static final long COMMAND_TIMEOUT = 60000;
    private static final String TAG = "ModeActivity";
    private Button btnDaily;
    private Button btnDefault;
    private Button btnPerformance;
    private Button btnPowerSaving;
    private View ivModeIndicator;
    private SharedPreferences sharedPreferences;
    private ScrollView svOutput;
    private TextView tvCurrentMode;
    private TextView tvOutput;
    private boolean isCommandExecuting = false;
    private boolean isVivoDevice = false;
    private Handler uiHandler = new Handler(Looper.getMainLooper());
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = new Shizuku.OnRequestPermissionResultListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda5
        @Override // rikka.shizuku.Shizuku.OnRequestPermissionResultListener
        public final void onRequestPermissionResult(int i, int i2) {
            this.f$0.m189lambda$new$0$comexamplekbatteryModeActivity(i, i2);
        }
    };

    /* renamed from: lambda$new$0$com-example-kbattery-ModeActivity, reason: not valid java name */
    /* synthetic */ void m189lambda$new$0$comexamplekbatteryModeActivity(int requestCode, int grantResult) {
        updateShizukuState();
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, androidx.core.app.ComponentActivity, android.app.Activity
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_mode);
        checkDeviceManufacturer();
        this.tvCurrentMode = (TextView) findViewById(R.id.tv_current_mode);
        this.ivModeIndicator = findViewById(R.id.iv_mode_indicator);
        this.svOutput = (ScrollView) findViewById(R.id.sv_output);
        this.tvOutput = (TextView) findViewById(R.id.tv_output);
        this.tvOutput.setHorizontallyScrolling(false);
        this.tvOutput.setSingleLine(false);
        this.sharedPreferences = getSharedPreferences("app_preferences", 0);
        String savedMode = this.sharedPreferences.getString("current_mode", "默认模式");
        updateMode(savedMode);
        this.btnPowerSaving = (Button) findViewById(R.id.btn_power_saving);
        this.btnPerformance = (Button) findViewById(R.id.btn_performance);
        this.btnDaily = (Button) findViewById(R.id.btn_daily);
        this.btnDefault = (Button) findViewById(R.id.btn_default);
        initButtonListeners();
        Shizuku.addRequestPermissionResultListener(this.REQUEST_PERMISSION_RESULT_LISTENER);
        updateShizukuState();
    }

    private void checkDeviceManufacturer() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        this.isVivoDevice = manufacturer.contains("vivo");
        if (!this.isVivoDevice) {
            runOnUiThread(new Runnable() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                    this.f$0.showUnsupportedDeviceDialog();
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showUnsupportedDeviceDialog() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = DialogUtils.createBlurConfirmDialog(this, "设备不支持", "当前设备不支持，请阅读手册！", "确定", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }, null, null);
        DialogUtils.showWithBlurEffect(builder);
    }

    private void initButtonListeners() {
        this.btnPowerSaving.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m185lambda$initButtonListeners$2$comexamplekbatteryModeActivity(view);
            }
        });
        this.btnPerformance.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m186lambda$initButtonListeners$3$comexamplekbatteryModeActivity(view);
            }
        });
        this.btnDaily.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m187lambda$initButtonListeners$4$comexamplekbatteryModeActivity(view);
            }
        });
        this.btnDefault.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.ModeActivity$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m188lambda$initButtonListeners$5$comexamplekbatteryModeActivity(view);
            }
        });
    }

    /* renamed from: lambda$initButtonListeners$2$com-example-kbattery-ModeActivity, reason: not valid java name */
    /* synthetic */ void m185lambda$initButtonListeners$2$comexamplekbatteryModeActivity(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        handleModeButton("省电模式", buildPowerSavingCommands());
    }

    /* renamed from: lambda$initButtonListeners$3$com-example-kbattery-ModeActivity, reason: not valid java name */
    /* synthetic */ void m186lambda$initButtonListeners$3$comexamplekbatteryModeActivity(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        handleModeButton("性能模式", buildPerformanceCommands());
    }

    /* renamed from: lambda$initButtonListeners$4$com-example-kbattery-ModeActivity, reason: not valid java name */
    /* synthetic */ void m187lambda$initButtonListeners$4$comexamplekbatteryModeActivity(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        handleModeButton("日常模式", buildDailyCommands());
    }

    /* renamed from: lambda$initButtonListeners$5$com-example-kbattery-ModeActivity, reason: not valid java name */
    /* synthetic */ void m188lambda$initButtonListeners$5$comexamplekbatteryModeActivity(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        handleModeButton("默认模式", buildDefaultCommands());
    }

    private String[] buildPowerSavingCommands() {
        return new String[]{"echo 续航脚本3.0启用中...\necho \"防冲突进行中...\"\nsettings delete system power_sleep_mode_enabled\nsettings delete global adaptive_battery_management_enabled\nsettings delete global app_standby_enabled\nsettings delete system light_effect_power_saving_strategy_switch\nsettings delete global touch_power_saving_mode\nsettings delete secure basic_sleep_enabled\nsettings delete system soft_wakeup_enhance_enabled\nsettings delete system pem_network_policy_start\nsettings delete global standby_battery_saving_enabled\nsettings delete global standby_power_saving_enabled\nsettings delete secure enable_bg_activity_feature\nsettings delete secure pem_in_sleepmode\nsettings delete system call_log\nsettings delete global activity_starts_logging_enabled\nsettings delete global ecg_disable_logging\nsettings delete global battery_saver_constants >/dev/null 2>&1\nsettings delete global device_idle_constants >/dev/null 2>&1\ndevice_config delete activity_manager_native_boot low_power_mode\necho 开启睡眠模式1\nsettings put system power_sleep_mode_enabled 1\necho 开启自适应电池管理 \nsettings put global adaptive_battery_management_enabled 1\necho 开启应用待机优化\nsettings put global app_standby_enabled 1\necho 开启灯光省电策略\nsettings put system light_effect_power_saving_strategy_switch 1\necho 开启触摸省电\nsettings put global touch_power_saving_mode 1\necho 开启睡眠模式\nsettings put secure basic_sleep_enabled 1\necho 关闭软唤醒增强\nsettings put system soft_wakeup_enhance_enabled 0\necho 开启PEM网络策略\nsettings put system pem_network_policy_start 1\necho 开启待机电池节省\nsettings put global standby_battery_saving_enabled 1\necho 开启待机功耗节省\nsettings put global standby_power_saving_enabled 1\necho 开启后台活动优化\nsettings put secure enable_bg_activity_feature 1\necho 进入深度睡眠模式\nsettings put secure pem_in_sleepmode 1\necho 关闭系统日志\nsettings put system call_log 0\nsettings put global activity_starts_logging_enabled 0\nsettings put global ecg_disable_logging 1\necho doze 模式\nsettings put global battery_saver_constants datasayer_disabled=false,disable_animation=true,vibration_disabled=true,disable_vibration=true,force_all_apps_standby=true,enable_quick_doze=true,animation_disabled=true,enable_datasayer=true,quick_doze_enabled=true,force_background_check=true,advertise_is_enabled >/dev/null 2>&1\necho doze 配置\nsettings put global device_idle_constants inactive_to=1800000,motion_inactive_to=900000,light_after_inactive_to=150000,light_pre_idle_to=300000,light_max_idle_to=432000000,light_idle_to=360000000,light_idle_factor=8,light_idle_maintenance_max_budget=500,light_idle_maintenance_min_budget=200,min_light_maintenance_time=500,min_time_to_alarm=300000 >/dev/null 2>&1\necho 激活活动省电\ndevice_config put activity_manager_native_boot low_power_mode true\necho 启用活动管理器冻结功能\ndevice_config put activity_manager_native_boot use_freezer true\necho 完毕\n"};
    }

    private String[] buildPerformanceCommands() {
        return new String[]{"echo 续航脚本3.0启用中...\necho \"防冲突进行中...\"\nsettings delete system power_sleep_mode_enabled\nsettings delete global adaptive_battery_management_enabled\nsettings delete global app_standby_enabled\nsettings delete system light_effect_power_saving_strategy_switch\nsettings delete global touch_power_saving_mode\nsettings delete secure basic_sleep_enabled\nsettings delete system soft_wakeup_enhance_enabled\nsettings delete system pem_network_policy_start\nsettings delete global standby_battery_saving_enabled\nsettings delete global standby_power_saving_enabled\nsettings delete secure enable_bg_activity_feature\nsettings delete secure pem_in_sleepmode\nsettings delete system call_log\nsettings delete global activity_starts_logging_enabled\nsettings delete global ecg_disable_logging\nsettings delete global battery_saver_constants >/dev/null 2>&1\nsettings delete global device_idle_constants >/dev/null 2>&1\nsettings put system is_game_mode 1\n\n\necho 强制开启 GPU 加速\nsettings put system game_enable_gpu_turbo 1\n\necho 强制开启 CPU 加速\nsettings put system game_enable_cpu_turbo 1\n\necho 允许 60FPS 高帧率模式\nsettings put system game_enable_60_fps 1\n\necho 性能增强\nsettings put system game_performance_lingfan_model 1\n\necho 开启系统级优化\nsettings put system game_performance_model_mysterious_bonus 1\n\necho 强制启用动态分辨率\nsettings put system game_dynamic_resolution 1\n\necho 开启MEMC 运动补偿\nsettings put system gamecube_frame_interpolation 1\n\n\necho 启用 AI 情景优化\nsettings put secure vivo_gamecube_ai_situation_stage 1\n\necho 关闭充电限制\n settings put secure battery_charge_director_game_cube 0\n\necho 开启 60FPS+ 高帧率模式\nsettings put secure game_enable_high_fps 1\necho 完毕\n"};
    }

    private String[] buildDailyCommands() {
        return new String[]{"echo 防冲突进行中...\n                \"echo 续航脚本3.0启用中...\\n\" +  // 修复：添加换行符，避免命令拼接错误\n                        \"echo \\\"防冲突进行中...\\\"\\n\" +\n                        \"settings delete system power_sleep_mode_enabled\\n\" +\n                        \"settings delete global adaptive_battery_management_enabled\\n\" +\n                        \"settings delete global app_standby_enabled\\n\" +\n                        \"settings delete system light_effect_power_saving_strategy_switch\\n\" +\n                        \"settings delete global touch_power_saving_mode\\n\" +\n                        \"settings delete secure basic_sleep_enabled\\n\" +\n                        \"settings delete system soft_wakeup_enhance_enabled\\n\" +\n                        \"settings delete system pem_network_policy_start\\n\" +\n                        \"settings delete global standby_battery_saving_enabled\\n\" +\n                        \"settings delete global standby_power_saving_enabled\\n\" +\n                        \"settings delete secure enable_bg_activity_feature\\n\" +\n                        \"settings delete secure pem_in_sleepmode\\n\" +\n                        \"settings delete system call_log\\n\" +\n                        \"settings delete global activity_starts_logging_enabled\\n\" +\n                        \"settings delete global ecg_disable_logging\\n\" +\n                        \"settings delete global battery_saver_constants >/dev/null 2>&1\\n\" +\n                        \"settings delete global device_idle_constants >/dev/null 2>&1\\n\" +sleep 2\necho 原生均衡模式配置\nsettings put secure power_save_type 1\nsettings put system power_save_type 1\nsettings put secure system_property_power_mode_type 1\nsettings put system powermonitor 1\nsettings put system pempsmstate 1\nsettings put system haptic_feedback_enabled 1\nsettings put system unlock_enter_launcher_animation 3\nsettings put system screen_off_remind 1\nsettings put system aod_tp_support_switch 1\nsettings put system nightpearl_is_showing 1\nsettings put secure enter_exit_app_realtime_blur_state 1\nsettings put system motion_blur_state 1\nsettings put system enhanced_dynamic_effects 1\nsettings put secure launcher_widget_support_blur 1\necho 临时关闭LTPO\nsettings put system vivo_ltpo_setting 0\ndevice_config put core_graphics com.android.graphics.surfaceflinger.flags.vrr_config false\necho 硬件加速提速\nsetprop debug.egl.hw 1\nsetprop debug.sf.hw 1\necho 完毕\n"};
    }

    private String[] buildDefaultCommands() {
        return new String[]{"echo 配置还原脚本启动中...\necho 正在清除省电/性能/日常模式残留配置...\n\n# 清除省电模式相关设置\nsettings delete system power_sleep_mode_enabled\nsettings delete global adaptive_battery_management_enabled\nsettings delete global app_standby_enabled\nsettings delete system light_effect_power_saving_strategy_switch\nsettings delete global touch_power_saving_mode\nsettings delete secure basic_sleep_enabled\nsettings delete system soft_wakeup_enhance_enabled\nsettings delete system pem_network_policy_start\nsettings delete global standby_battery_saving_enabled\nsettings delete global standby_power_saving_enabled\nsettings delete secure enable_bg_activity_feature\nsettings delete secure pem_in_sleepmode\nsettings delete system call_log\nsettings delete global activity_starts_logging_enabled\nsettings delete global ecg_disable_logging\nsettings delete global battery_saver_constants >/dev/null 2>&1\nsettings delete global device_idle_constants >/dev/null 2>&1\ndevice_config delete activity_manager_native_boot low_power_mode\ndevice_config delete activity_manager_native_boot use_freezer\n\n# 清除性能模式相关设置\nsettings delete system is_game_mode\nsettings delete system game_enable_gpu_turbo\nsettings delete system game_enable_cpu_turbo\nsettings delete system game_enable_60_fps\nsettings delete system game_performance_lingfan_model\nsettings delete system game_performance_model_mysterious_bonus\nsettings delete system game_dynamic_resolution\nsettings delete system gamecube_frame_interpolation\nsettings delete secure vivo_gamecube_ai_situation_stage\nsettings delete secure battery_charge_director_game_cube\nsettings delete secure game_enable_high_fps\n\n# 清除日常模式相关设置\nsettings delete secure power_save_type\nsettings delete system power_save_type\nsettings delete secure system_property_power_mode_type\nsettings delete system powermonitor\nsettings delete system pempsmstate\nsettings delete system haptic_feedback_enabled\nsettings delete system unlock_enter_launcher_animation\nsettings delete system screen_off_remind\nsettings delete system aod_tp_support_switch\nsettings delete system nightpearl_is_showing\nsettings delete secure enter_exit_app_realtime_blur_state\nsettings delete system motion_blur_state\nsettings delete system enhanced_dynamic_effects\nsettings delete secure launcher_widget_support_blur\nsettings delete system vivo_ltpo_setting\ndevice_config delete core_graphics com.android.graphics.surfaceflinger.flags.vrr_configsleep 1\necho 还原完毕\n"};
    }

    private void handleModeButton(String mode, String[] commands) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (!this.isVivoDevice) {
            showUnsupportedDeviceDialog();
            return;
        }
        if (this.isCommandExecuting) {
            Toast.makeText(this, "可白要忙不过来啦QWQ", 0).show();
            return;
        }
        this.tvOutput.setText("");
        updateMode(mode);
        this.isCommandExecuting = true;
        this.executor.execute(new CommandRunner(commands));
    }

    private void updateMode(String mode) {
        Log.d(TAG, "Updating mode to: " + mode);
        this.tvCurrentMode.setText("当前模式：" + mode);
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putString("current_mode", mode);
        editor.apply();
        updateShizukuState();
    }

    private void updateShizukuState() {
        if (MainActivity.isShizukuAvailable && MainActivity.isShizukuActive && MainActivity.isShizukuGranted) {
            this.ivModeIndicator.setBackgroundResource(R.drawable.circle_background);
        } else {
            this.ivModeIndicator.setBackgroundResource(R.drawable.status_dot_red);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class CommandRunner implements Runnable {
        private String[] commands;
        private boolean isCancelled = false;
        private Process process;

        CommandRunner(String[] commands) {
            this.commands = commands;
        }

        @Override // java.lang.Runnable
        public void run() {
            Handler handler;
            Runnable runnable;
            try {
                try {
                    if (MainActivity.isShizukuAvailable && MainActivity.isShizukuActive && MainActivity.isShizukuGranted) {
                        this.process = Shizuku.newProcess(new String[]{"sh"}, null, null);
                        if (this.process != null) {
                            OutputStream out = this.process.getOutputStream();
                            new Thread(ModeActivity.this.new InputGobbler(this.process.getInputStream())).start();
                            new Thread(ModeActivity.this.new ErrorGobbler(this.process.getErrorStream())).start();
                            for (String cmd : this.commands) {
                                if (this.isCancelled) {
                                    break;
                                }
                                out.write((cmd + "\n").getBytes());
                                out.flush();
                                Thread.sleep(50L);
                            }
                            out.close();
                            this.process.waitFor();
                        }
                    } else {
                        ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$CommandRunner$$ExternalSyntheticLambda0
                            @Override // java.lang.Runnable
                            public final void run() {
                                this.f$0.m190lambda$run$0$comexamplekbatteryModeActivity$CommandRunner();
                            }
                        });
                    }
                    if (this.process != null) {
                        this.process.destroy();
                    }
                    handler = ModeActivity.this.uiHandler;
                    runnable = new Runnable() { // from class: com.example.kbattery.ModeActivity$CommandRunner$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m192lambda$run$2$comexamplekbatteryModeActivity$CommandRunner();
                        }
                    };
                } catch (Exception e) {
                    final String errorMsg = e.getMessage();
                    ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$CommandRunner$$ExternalSyntheticLambda2
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m191lambda$run$1$comexamplekbatteryModeActivity$CommandRunner(errorMsg);
                        }
                    });
                    if (this.process != null) {
                        this.process.destroy();
                    }
                    handler = ModeActivity.this.uiHandler;
                    runnable = new Runnable() { // from class: com.example.kbattery.ModeActivity$CommandRunner$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m192lambda$run$2$comexamplekbatteryModeActivity$CommandRunner();
                        }
                    };
                }
                handler.post(runnable);
            } catch (Throwable th) {
                if (this.process != null) {
                    this.process.destroy();
                }
                ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$CommandRunner$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m192lambda$run$2$comexamplekbatteryModeActivity$CommandRunner();
                    }
                });
                throw th;
            }
        }

        /* renamed from: lambda$run$0$com-example-kbattery-ModeActivity$CommandRunner, reason: not valid java name */
        /* synthetic */ void m190lambda$run$0$comexamplekbatteryModeActivity$CommandRunner() {
            ModeActivity.this.tvOutput.append("Shizuku未授权或未激活，请检查权限\n");
        }

        /* renamed from: lambda$run$1$com-example-kbattery-ModeActivity$CommandRunner, reason: not valid java name */
        /* synthetic */ void m191lambda$run$1$comexamplekbatteryModeActivity$CommandRunner(String errorMsg) {
            ModeActivity.this.tvOutput.append("执行错误: " + errorMsg + "\n");
        }

        /* renamed from: lambda$run$2$com-example-kbattery-ModeActivity$CommandRunner, reason: not valid java name */
        /* synthetic */ void m192lambda$run$2$comexamplekbatteryModeActivity$CommandRunner() {
            ModeActivity.this.isCommandExecuting = false;
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class InputGobbler implements Runnable {
        private InputStream is;

        InputGobbler(InputStream is) {
            this.is = is;
        }

        @Override // java.lang.Runnable
        public void run() throws IOException {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
                while (true) {
                    try {
                        final String line = br.readLine();
                        if (line != null) {
                            ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$InputGobbler$$ExternalSyntheticLambda0
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f$0.m195lambda$run$0$comexamplekbatteryModeActivity$InputGobbler(line);
                                }
                            });
                        } else {
                            br.close();
                            return;
                        }
                    } finally {
                    }
                }
            } catch (IOException e) {
                if (!e.getMessage().contains("Stream closed")) {
                    ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$InputGobbler$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m196lambda$run$1$comexamplekbatteryModeActivity$InputGobbler(e);
                        }
                    });
                }
            }
        }

        /* renamed from: lambda$run$0$com-example-kbattery-ModeActivity$InputGobbler, reason: not valid java name */
        /* synthetic */ void m195lambda$run$0$comexamplekbatteryModeActivity$InputGobbler(String outputLine) {
            ModeActivity.this.tvOutput.append(outputLine + "\n");
            ModeActivity.this.svOutput.fullScroll(130);
        }

        /* renamed from: lambda$run$1$com-example-kbattery-ModeActivity$InputGobbler, reason: not valid java name */
        /* synthetic */ void m196lambda$run$1$comexamplekbatteryModeActivity$InputGobbler(IOException e) {
            ModeActivity.this.tvOutput.append("读取输出错误: " + e.getMessage() + "\n");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    class ErrorGobbler implements Runnable {
        private InputStream is;

        ErrorGobbler(InputStream is) {
            this.is = is;
        }

        @Override // java.lang.Runnable
        public void run() throws IOException {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.is));
                while (true) {
                    try {
                        final String line = br.readLine();
                        if (line != null) {
                            ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$ErrorGobbler$$ExternalSyntheticLambda0
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f$0.m193lambda$run$0$comexamplekbatteryModeActivity$ErrorGobbler(line);
                                }
                            });
                        } else {
                            br.close();
                            return;
                        }
                    } finally {
                    }
                }
            } catch (IOException e) {
                if (!e.getMessage().contains("Stream closed")) {
                    ModeActivity.this.uiHandler.post(new Runnable() { // from class: com.example.kbattery.ModeActivity$ErrorGobbler$$ExternalSyntheticLambda1
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m194lambda$run$1$comexamplekbatteryModeActivity$ErrorGobbler(e);
                        }
                    });
                }
            }
        }

        /* renamed from: lambda$run$0$com-example-kbattery-ModeActivity$ErrorGobbler, reason: not valid java name */
        /* synthetic */ void m193lambda$run$0$comexamplekbatteryModeActivity$ErrorGobbler(String errorLine) {
            ModeActivity.this.tvOutput.append(Html.fromHtml(String.format("<font color='red'>%s</font>\n", errorLine)));
            ModeActivity.this.svOutput.fullScroll(130);
        }

        /* renamed from: lambda$run$1$com-example-kbattery-ModeActivity$ErrorGobbler, reason: not valid java name */
        /* synthetic */ void m194lambda$run$1$comexamplekbatteryModeActivity$ErrorGobbler(IOException e) {
            ModeActivity.this.tvOutput.append("读取错误: " + e.getMessage() + "\n");
        }
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        this.executor.shutdownNow();
    }
}
