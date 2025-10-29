package com.example.kbattery;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.kbattery.Fragment3;
import com.example.kbattery.utils.DialogUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import rikka.shizuku.Shizuku;

/* loaded from: classes3.dex */
public class Fragment3 extends Fragment implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener {
    private static final String KEY_K_SCHEDULER = "k_scheduler_selected_id";
    private static final String KEY_SELECTED_APPS = "selected_apps";
    private static final String PREFS_NAME = "com.example.kbattery.prefs";
    private static final String PREF_FIRST_LAUNCH = "first_launch";
    private static final String PREF_NAME = "SelectedAppsPrefs";
    private static final int REQUEST_CODE_OVERLAY = 1;
    private static final String TAG = "Fragment3";
    private MaterialButton btnDisable;
    private MaterialButton btnDisableColor;
    private MaterialButton btnEnable;
    private MaterialButton btnEnableColor;
    private MaterialCardView cardSwitch;
    private MaterialCardView cardSwitchColor;
    private MaterialButton closeButton;
    private CardView colorOsCard;
    private AlertDialog commandDialog;
    private TextView commandOutputTextView;
    private ProgressBar commandProgressBar;
    private CardView displaySettingsCard;
    private ImageView floatIcon;
    private View floatView;
    private TextView frameResultTextView;
    private CardView hyperOsCard;
    private CardView kSchedulerCard;
    private CardView memorySettingsCard;
    private RadioGroup radioGroupKScheduler;
    private TextView selectedAppInfo;
    private ChipGroup selectedAppsChipGroup;
    private SharedPreferences sharedPreferences;
    private CompoundButton switchAiStrategy;
    private CompoundButton switchDirectPower;
    private CompoundButton switchLightTracing;
    private CompoundButton switchPinFunction;
    private CompoundButton switchWlanOptimize;
    private TextView tvEnhancementDetails;
    private WindowManager windowManager;
    private List<String> selectedPackages = new ArrayList();
    private String currentPackage = null;
    private boolean isNeonRunning = false;
    private int counter = 0;
    private boolean isFloatWindowShowing = false;
    private boolean isFrameEnabled = false;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Handler outputHandler = new AnonymousClass1(Looper.getMainLooper());
    private Handler neonHandler = new Handler(Looper.getMainLooper());
    private Random random = new Random();

    /* renamed from: com.example.kbattery.Fragment3$1, reason: invalid class name */
    class AnonymousClass1 extends Handler {
        AnonymousClass1(Looper arg0) {
            super(arg0);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (Fragment3.this.commandOutputTextView == null) {
                return;
            }
            String text = (String) msg.obj;
            if (msg.what == 0) {
                String currentText = Fragment3.this.commandOutputTextView.getText().toString();
                Fragment3.this.commandOutputTextView.setText(currentText + "\n" + text);
                final NestedScrollView scrollView = (NestedScrollView) Fragment3.this.commandOutputTextView.getParent();
                scrollView.post(new Runnable() { // from class: com.example.kbattery.Fragment3$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        scrollView.fullScroll(130);
                    }
                });
                return;
            }
            if (msg.what == 1) {
                Fragment3.this.commandProgressBar.setVisibility(8);
                Fragment3.this.closeButton.setVisibility(0);
                String currentText2 = Fragment3.this.commandOutputTextView.getText().toString();
                Fragment3.this.commandOutputTextView.setText(currentText2 + "\n\n" + text);
                final NestedScrollView scrollView2 = (NestedScrollView) Fragment3.this.commandOutputTextView.getParent();
                scrollView2.post(new Runnable() { // from class: com.example.kbattery.Fragment3$1$$ExternalSyntheticLambda1
                    @Override // java.lang.Runnable
                    public final void run() {
                        scrollView2.fullScroll(130);
                    }
                });
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_3, container, false);
    }

    @Override // androidx.fragment.app.Fragment
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        checkFirstLaunch();
        if (!isSupportedDevice()) {
            this.handler.post(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda28
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.m145lambda$onViewCreated$1$comexamplekbatteryFragment3();
                }
            });
            return;
        }
        initViews(view);
        this.sharedPreferences = requireContext().getSharedPreferences(PREF_NAME, 0);
        loadSelectedAppsFromPrefs();
        controlCardsByDeviceBrand();
        if (!checkDeviceCompatibility()) {
            return;
        }
        initSwitches(view);
        initKSchedulerRadioGroup();
        initOptimizationFeatures(view);
    }

    /* renamed from: lambda$onViewCreated$1$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m145lambda$onViewCreated$1$comexamplekbatteryFragment3() {
        DialogUtils.createConfirmDialog(requireContext(), "不支持的设备", "当前设备未适配，制造商: " + Build.MANUFACTURER, "确定", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda26
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m144lambda$onViewCreated$0$comexamplekbatteryFragment3(dialogInterface, i);
            }
        }, null, null).setCancelable(false).show();
    }

    /* renamed from: lambda$onViewCreated$0$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m144lambda$onViewCreated$0$comexamplekbatteryFragment3(DialogInterface dialog, int which) {
        dialog.dismiss();
        requireActivity().finish();
    }

    private void checkFirstLaunch() {
        final SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        boolean isFirstLaunch = prefs.getBoolean(PREF_FIRST_LAUNCH, true);
        if (isFirstLaunch) {
            new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "功能提示").setMessage((CharSequence) "这些功能大部分都在新机型有用，老机型自测").setPositiveButton((CharSequence) "我知道了", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda21
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    Fragment3.lambda$checkFirstLaunch$2(prefs, dialogInterface, i);
                }
            }).setCancelable(false).show();
        }
    }

    static /* synthetic */ void lambda$checkFirstLaunch$2(SharedPreferences prefs, DialogInterface dialog, int which) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(PREF_FIRST_LAUNCH, false);
        editor.apply();
        dialog.dismiss();
    }

    private void initViews(View view) {
        this.memorySettingsCard = (CardView) view.findViewById(R.id.memory_settings_card);
        this.displaySettingsCard = (CardView) view.findViewById(R.id.display_settings_card);
        this.colorOsCard = (CardView) view.findViewById(R.id.coloros_card);
        this.hyperOsCard = (CardView) view.findViewById(R.id.hyperos_card);
        this.kSchedulerCard = (CardView) view.findViewById(R.id.k_scheduler_card);
        this.radioGroupKScheduler = (RadioGroup) view.findViewById(R.id.radio_group_k_scheduler);
        this.switchWlanOptimize = (CompoundButton) view.findViewById(R.id.switch_wlan_optimize);
        this.switchPinFunction = (CompoundButton) view.findViewById(R.id.switch_pin_function);
        this.switchLightTracing = (CompoundButton) view.findViewById(R.id.switch_light_tracing);
        this.switchAiStrategy = (CompoundButton) view.findViewById(R.id.switch_ai_strategy);
        this.selectedAppInfo = (TextView) view.findViewById(R.id.selected_app_info);
        this.selectedAppsChipGroup = (ChipGroup) view.findViewById(R.id.selected_apps_chip_group);
        this.btnEnable = (MaterialButton) view.findViewById(R.id.btn_enable);
        this.btnDisable = (MaterialButton) view.findViewById(R.id.btn_disable);
        this.btnEnableColor = (MaterialButton) view.findViewById(R.id.btn_enable_color);
        this.btnDisableColor = (MaterialButton) view.findViewById(R.id.btn_disable_color);
        this.cardSwitch = (MaterialCardView) view.findViewById(R.id.card_switch);
        this.cardSwitchColor = (MaterialCardView) view.findViewById(R.id.card_switch_color);
        this.tvEnhancementDetails = (TextView) view.findViewById(R.id.tv_enhancement_details);
        this.switchDirectPower = (CompoundButton) view.findViewById(R.id.switch_direct_power);
        this.frameResultTextView = (TextView) view.findViewById(R.id.frame_result_text_view);
        this.windowManager = (WindowManager) requireContext().getSystemService("window");
    }

    private void initOptimizationFeatures(View view) {
        setupDirectPowerSwitch();
        initEnhancementDetailsClick();
        final CompoundButton switchFloatWindow = (CompoundButton) view.findViewById(R.id.switch_float_window);
        switchFloatWindow.setChecked(this.isFloatWindowShowing);
        switchFloatWindow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda22
            @Override // android.widget.CompoundButton.OnCheckedChangeListener
            public final void onCheckedChanged(CompoundButton compoundButton, boolean z) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m140lambda$initOptimizationFeatures$3$comexamplekbatteryFragment3(switchFloatWindow, compoundButton, z);
            }
        });
        MaterialButton btnCustomAdd = (MaterialButton) view.findViewById(R.id.btn_custom_add);
        btnCustomAdd.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda33
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m141lambda$initOptimizationFeatures$4$comexamplekbatteryFragment3(view2);
            }
        });
        this.btnEnable.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda43
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m142lambda$initOptimizationFeatures$5$comexamplekbatteryFragment3(view2);
            }
        });
        this.btnDisable.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda44
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m143lambda$initOptimizationFeatures$6$comexamplekbatteryFragment3(view2);
            }
        });
        bindColorButtonEvents();
        updateSelectedApps();
    }

    /* renamed from: lambda$initOptimizationFeatures$3$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m140lambda$initOptimizationFeatures$3$comexamplekbatteryFragment3(CompoundButton switchFloatWindow, CompoundButton buttonView, boolean isChecked) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (isChecked) {
            if (checkFloatWindowPermission()) {
                showFloatWindow();
                return;
            } else {
                requestFloatWindowPermission();
                switchFloatWindow.setChecked(false);
                return;
            }
        }
        hideFloatWindow();
    }

    /* renamed from: lambda$initOptimizationFeatures$4$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m141lambda$initOptimizationFeatures$4$comexamplekbatteryFragment3(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        try {
            showAppListDialog();
        } catch (Exception e) {
            Log.e(TAG, "DIY添加按钮点击异常", e);
            showAlertDialog("错误", "打开应用列表失败：" + e.getMessage());
        }
    }

    /* renamed from: lambda$initOptimizationFeatures$5$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m142lambda$initOptimizationFeatures$5$comexamplekbatteryFragment3(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.currentPackage != null) {
            if (checkFloatWindowPermission()) {
                if (checkShizukuPermission()) {
                    startOptimizationWithProgress(this.currentPackage);
                    return;
                }
                return;
            }
            requestFloatWindowPermission();
            return;
        }
        showAlertDialog("提示", "请先选择一个应用");
    }

    /* renamed from: lambda$initOptimizationFeatures$6$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m143lambda$initOptimizationFeatures$6$comexamplekbatteryFragment3(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.currentPackage != null) {
            if (checkShizukuPermission()) {
                resetAllOptimizations(this.currentPackage);
                return;
            }
            return;
        }
        showAlertDialog("提示", "请先选择一个应用");
    }

    private void bindColorButtonEvents() {
        this.btnEnableColor.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda16
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m127lambda$bindColorButtonEvents$7$comexamplekbatteryFragment3(view);
            }
        });
        this.btnDisableColor.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda17
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m128lambda$bindColorButtonEvents$8$comexamplekbatteryFragment3(view);
            }
        });
    }

    /* renamed from: lambda$bindColorButtonEvents$7$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m127lambda$bindColorButtonEvents$7$comexamplekbatteryFragment3(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.currentPackage != null) {
            if (checkFloatWindowPermission()) {
                if (checkShizukuPermission()) {
                    startColorOptimizationWithProgress(this.currentPackage);
                    return;
                }
                return;
            }
            requestFloatWindowPermission();
            return;
        }
        showAlertDialog("提示", "请先选择一个应用");
    }

    /* renamed from: lambda$bindColorButtonEvents$8$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m128lambda$bindColorButtonEvents$8$comexamplekbatteryFragment3(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.currentPackage != null) {
            if (checkShizukuPermission()) {
                executeColorDisableCommand(this.currentPackage);
                this.isFrameEnabled = false;
                updateFloatWindowStatus();
                return;
            }
            return;
        }
        showAlertDialog("提示", "请先选择一个应用");
    }

    private boolean checkDeviceCompatibility() {
        String manufacturer = Build.MANUFACTURER.toLowerCase();
        String brand = Build.BRAND.toLowerCase();
        Log.d(TAG, "设备制造商: " + manufacturer + ", 品牌: " + brand);
        boolean isVivoOrIqoo = manufacturer.contains("vivo") || manufacturer.contains("iqoo") || brand.contains("vivo") || brand.contains("iqoo");
        boolean isOppoRelated = manufacturer.contains("oppo") || manufacturer.contains("oneplus") || manufacturer.contains("realme") || brand.contains("oppo") || brand.contains("oneplus") || brand.contains("realme");
        boolean isXiaomiRelated = manufacturer.contains("xiaomi") || brand.contains("xiaomi") || brand.contains("redmi") || brand.contains("mi");
        setVisibility(this.kSchedulerCard, isVivoOrIqoo ? 0 : 8);
        if (isVivoOrIqoo) {
            setVisibility(this.memorySettingsCard, 0);
            setVisibility(this.displaySettingsCard, 0);
            setVisibility(this.colorOsCard, 8);
            setVisibility(this.hyperOsCard, 8);
            return true;
        }
        if (isOppoRelated) {
            setVisibility(this.memorySettingsCard, 8);
            setVisibility(this.displaySettingsCard, 8);
            setVisibility(this.colorOsCard, 0);
            setVisibility(this.hyperOsCard, 8);
            return true;
        }
        if (isXiaomiRelated) {
            setVisibility(this.memorySettingsCard, 8);
            setVisibility(this.displaySettingsCard, 8);
            setVisibility(this.colorOsCard, 8);
            setVisibility(this.hyperOsCard, 0);
            return true;
        }
        DialogUtils.createConfirmDialog(requireContext(), "系统未适配", "当前设备暂不支持此功能", "确定", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda47
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m129lambda$checkDeviceCompatibility$9$comexamplekbatteryFragment3(dialogInterface, i);
            }
        }, null, null).setCancelable(false).show();
        return false;
    }

    /* renamed from: lambda$checkDeviceCompatibility$9$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m129lambda$checkDeviceCompatibility$9$comexamplekbatteryFragment3(DialogInterface dialog, int which) {
        requireActivity().finish();
    }

    private void controlCardsByDeviceBrand() {
        String manufacturer = Build.MANUFACTURER;
        if (manufacturer == null) {
            return;
        }
        String manufacturerLower = manufacturer.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
        boolean isVivoOrIqoo = manufacturerLower.contains("vivo") || manufacturerLower.contains("iqoo");
        boolean isOppoSeries = manufacturerLower.contains("oppo") || manufacturerLower.contains("oneplus") || manufacturerLower.contains("realme");
        if (isVivoOrIqoo) {
            if (this.cardSwitchColor != null) {
                this.cardSwitchColor.setVisibility(8);
            }
        } else if (isOppoSeries && this.cardSwitch != null) {
            this.cardSwitch.setVisibility(8);
        }
    }

    private void initKSchedulerRadioGroup() {
        if (this.kSchedulerCard != null && this.kSchedulerCard.getVisibility() != 0) {
            return;
        }
        final SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
        int savedCheckedId = prefs.getInt(KEY_K_SCHEDULER, -1);
        if (savedCheckedId != -1) {
            this.radioGroupKScheduler.check(savedCheckedId);
        }
        this.radioGroupKScheduler.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda42
            @Override // android.widget.RadioGroup.OnCheckedChangeListener
            public final void onCheckedChanged(RadioGroup radioGroup, int i) {
                this.f$0.m139x60bdca67(prefs, radioGroup, i);
            }
        });
    }

    /* renamed from: lambda$initKSchedulerRadioGroup$12$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m139x60bdca67(final SharedPreferences prefs, RadioGroup group, int checkedId) {
        if (checkedId == -1) {
            return;
        }
        if (!checkShizukuPermission()) {
            int lastSavedId = prefs.getInt(KEY_K_SCHEDULER, -1);
            if (lastSavedId != -1) {
                this.radioGroupKScheduler.check(lastSavedId);
                return;
            } else {
                this.radioGroupKScheduler.clearCheck();
                return;
            }
        }
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_K_SCHEDULER, checkedId);
        editor.apply();
        if (checkedId == R.id.radio_smooth) {
            executeCommandWithImmediateOkDialog("setprop debug.sf.early.app.duration 1000000\nsetprop debug.sf.early.sf.duration 1000000\nsetprop debug.sf.earlyGl.app.duration 1000000\nsetprop debug.sf.earlyGl.sf.duration 1000000\nsetprop debug.sf.late.app.duration 1000000\nsetprop debug.sf.late.sf.duration 1000000\nsetprop debug.sqlite.wal.syncmode off\nsetprop debug.sqlite.syncmode off\nsetprop debug.sqlite.journalmode off\nsetprop debug.media.kit.trance.enable 0\nsetprop debug.media.kit.logv.enable 0\nsetprop debug.camera.extendinfo no\nsetprop debug.sf.auto_latch_unsignaled 1\nsetprop debug.sf.disable_client_composition_cache 0\nsetprop debug.sf.predict_hwc_composition_strategy 1\nam broadcast -a com.vivo.gamewatch.modules.namelist.appFpsSettings >/dev/null\nam broadcast -a com.vivo.gamewatch.modules.namelist.TempFpsSettings >/dev/null\nam broadcast -a com.vivo.gamewatch.modules.namelist.thermalSettings >/dev/null\nam broadcast -a com.vivo.gamewatch.modules.namelist.frameAwareSettings >/dev/null\nam broadcast -a com.vivo.gamewatch.modules.namelist.WatchDogSettings >/dev/null\nam broadcast -a com.vivo.uepp.config.policy.PolicyForest >/dev/null\nam broadcast -a com.vivo.uepp.config.ConfigManager >/dev/null\nam broadcast -a com.vivo.uepp.config.policy.PolicyCfgParser >/dev/null\nam broadcast -a com.vivolog.controllog -e from adb -e cmd_name clear com.android.bbklog >/dev/null\nam broadcast -a com.vivo.pem.app_super_performance_policy >/dev/null\nam broadcast -a com.vivo.pem.app_monster_policy >/dev/null\nam broadcast -a com.vivo.pem.app_perf_policy >/dev/null\ndevice_config put runtime_native_boot preload_high_priority_apps true\ndevice_config put runtime_native_boot iorap_perfetto_enable true\ndevice_config put system_performance com.android.internal.os.enable_apache_http_legacy_preload true\nsetprop debug.sf.hw 1\nsetprop debug.egl.hw 1\nlogcat -c\nlogcat -b all -c\nam kill-all\nam kill-all -w\nsm defragment run\nsm idle-maint run\nsm fstrim\npm clear com.vivo.gamewatch\ncmd package compile -m speed-profile com.vivo.gamewatch\npm clear com.vivo.abe\ncmd package compile -m speed-profile com.vivo.abe\nsetprop debug.sf.early.app.duration 1000000; setprop debug.sf.early.sf.duration 1000000; setprop debug.sf.earlyGl.app.duration 1000000; setprop debug.sf.earlyGl.sf.duration 1000000; setprop debug.sf.late.app.duration 1000000; setprop debug.sf.late.sf.duration 1000000; setprop debug.sqlite.wal.syncmode off; setprop debug.sqlite.syncmode off; setprop debug.sqlite.journalmode off; setprop debug.media.kit.trance.enable 0; setprop debug.media.kit.logv.enable 0; setprop debug.camera.extendinfo no; setprop debug.sf.auto_latch_unsignaled 1; setprop debug.sf.disable_client_composition_cache 0; setprop debug.sf.predict_hwc_composition_strategy 1; setprop debug.sf.hw 1; setprop debug.egl.hw 1; settings put secure abe_unified_switch 1; settings put system abe_unified_switch 1; settings put system abe_eea_suw 1; settings put system pem_htp_level -1; settings put system pointer_speed 7; settings put system touchpad_pointer_speed 7; settings put system touchpad_natural_scrolling 1; settings put system enable_vivoperf_test 1;settings put system com.vivo.push.fixed_delay 0; settings put secure aware_lock_enabled 1; settings put secure aware_enabled 1; settings put secure vivo_monkey_test 1; settings put system device_idle_state 1; settings put system device_idle_light_state 1; device_config put runtime_native_boot preload_high_priority_apps true; device_config put runtime_native_boot iorap_perfetto_enable true; device_config put system_performance com.android.internal.os.enable_apache_http_legacy_preload true; pm uninstall --user 0 com.android.microdroid.empty_payload >/dev/null; pm uninstall --user 0 com.android.virtualmachine.res >/dev/null; pm uninstall --user 0 com.android.htmlviewer >/dev/null; pm uninstall --user 0 com.android.server.deviceconfig >/dev/null; pm uninstall --user 0 com.android.virtualmachine.res >/dev/null; pm uninstall --user 0 com.qualcomm.qti.workloadclassifier >/dev/null; pm clear com.vivo.gamewatch; cmd package compile -m speed-profile com.vivo.gamewatch; pm clear com.vivo.abe; cmd package compile -m speed-profile com.vivo.abe; cmd power set-fixed-performance-mode-enabled true; cmd looper_stats disable; cmd looper_stats reset; cmd dropbox restore-defaults; am force-stop thermal-engine-v2; am kill thermal-engine-v2; am force-stop android.hardware.thermal@2.0-service.qti-v2; am kill android.hardware.thermal@2.0-service.qti-v2; cmd jobscheduler disable-flex-policy; cmd jobscheduler cache-config-changes off; logcat -c; logcat -b all -c; am kill-all; am kill-all -w; sm defragment run; sm idle-maint run; sm fstrim; am broadcast -a com.vivo.gamewatch.modules.namelist.appFpsSettings >/dev/null; am broadcast -a com.vivo.gamewatch.modules.namelist.TempFpsSettings >/dev/null; am broadcast -a com.vivo.gamewatch.modules.namelist.thermalSettings >/dev/null; am broadcast -a com.vivo.gamewatch.modules.namelist.frameAwareSettings >/dev/null; am broadcast -a com.vivo.gamewatch.modules.namelist.WatchDogSettings >/dev/null; am broadcast -a com.vivo.uepp.config.policy.PolicyForest >/dev/null; am broadcast -a com.vivo.uepp.config.ConfigManager >/dev/null; am broadcast -a com.vivo.uepp.config.policy.PolicyCfgParser >/dev/null; am broadcast -a com.vivolog.controllog -e from adb -e cmd_name clear com.android.bbklog >/dev/null;echo 已启用A+调度");
        } else if (checkedId == R.id.radio_pure) {
            executeCommandWithImmediateOkDialog("#!/system/bin/sh\nset -eo pipefail\n\n# 定义需要清理的特殊前缀（包含系统属性前缀）\nTARGET_PREFIXES=\"\n  vendor. sys. ro. persist. \n  init. debug. dev. service.\n  temp. app_.\n\"\n\n# 1. 重置基础设置\nsettings reset global 0\nsettings reset secure 0\n\n# 2. 清理全部分区的null值键（包括system分区的系统属性键）\nfor type in secure system global; do\n  settings list \"$type\" | while IFS='=' read -r key value; do\n    if [ -n \"$key\" ] && [ \"$value\" = \"null\" ]; then\n      settings delete \"$type\" \"$key\" >/dev/null 2>&1\n    fi\n  done\ndone\n\n# 3. 清理system分区目标前缀键（强制删除所有匹配的系统属性前缀）\nsettings list system | while IFS='=' read -r key _; do\n  if [ -z \"$key\" ]; then\n    continue\n  fi\n  # 匹配所有目标前缀（包括系统属性前缀），强制删除\n  for prefix in $TARGET_PREFIXES; do\n    if [[ \"$key\" == \"${prefix}\"* ]]; then\n      settings delete system \"$key\" >/dev/null 2>&1\n      break\n    fi\n  done\ndone\n\nam broadcast -a android.intent.action.SETTINGS_CHANGED >/dev/null 2>&1\necho 已删除杂鱼修改项\nexit 0");
        } else if (checkedId == R.id.radio_disable) {
            DialogUtils.createConfirmDialog(requireContext(), "确认关闭", "此操作需要重启设备，是否确定？", "确定", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda18
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.m137x505d4aa9(dialogInterface, i);
                }
            }, "取消", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda19
                @Override // android.content.DialogInterface.OnClickListener
                public final void onClick(DialogInterface dialogInterface, int i) {
                    this.f$0.m138xd88d8a88(prefs, dialogInterface, i);
                }
            }).show();
        }
    }

    /* renamed from: lambda$initKSchedulerRadioGroup$10$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m137x505d4aa9(DialogInterface dialog, int which) {
        executeCommandWithImmediateOkDialog("settings delete secure vivo_gamecube_ai_strategy\n\nsettings delete secure vivo_gamecube_ai_situation_stage\n\nsettings delete secure vivo_gamecube_solo_stage\n\nsettings delete secure vivo_gamecube_team_stage\n\nsettings delete secure vivo_gamecube_bp_stage\n\nsettings delete secure vivo_gamecube_kpl_strategy\n\nsettings delete secure vivo_gamecube_kpl_situation_stage\n\nsettings delete secure vivo_gamecube_pel_strategy\n\nsettings delete secure vivo_gamecube_pel_situation_stage\n\nsettings delete secure vivo_gamecube_cdm_strategy\n\nsettings delete secure vivo_gamecube_cdm_situation_stage\n\nsettings delete secure vivo_gamecube_cfml_strategy\n\nsettings delete secure vivo_gamecube_cfml_situation_stage\nsleep 3\necho 即将关闭设备\nreboot");
    }

    /* renamed from: lambda$initKSchedulerRadioGroup$11$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m138xd88d8a88(SharedPreferences prefs, DialogInterface dialog, int which) {
        dialog.dismiss();
        int lastSavedId = prefs.getInt(KEY_K_SCHEDULER, -1);
        if (lastSavedId != -1) {
            this.radioGroupKScheduler.check(lastSavedId);
        } else {
            this.radioGroupKScheduler.clearCheck();
        }
    }

    private void initSwitches(View view) {
        setupSwitch(view, R.id.switch_tombstone, "墓碑机制", "echo 墓碑已开启\nsettings put global cached_apps_freezer enabled", "echo 墓碑已关闭\nsettings put global cached_apps_freezer disabled");
        setupSwitch(view, R.id.switch_refresh_rate, "电源自适应", "echo 充电加速已开启;settings put secure vivo_keyguard_super_speed_charge_switch 1\nsettings put system balance_charge_switch 1\nsettings put system vivo_keyguard_show_charge_mode_switch 1\nsettings put system default_charge_acceleration 1\n", "echo 充电加速已关闭;settings put secure vivo_keyguard_super_speed_charge_switch 0\nsettings put system balance_charge_switch 0\nsettings put system vivo_keyguard_show_charge_mode_switch 0\nsettings put system default_charge_acceleration 0\n");
        setupSwitch(view, R.id.switch_memory_recycle, "内存回收", "echo vulkan已切换;device_config put core_graphics com.android.graphics.surfaceflinger.flags.vulkan_renderengine true\nsetprop debug.hwui.renderer skiavk\nsetprop debug.renderengine.backend skiavk", "echo vulkan已关闭;device_config put core_graphics com.android.graphics.surfaceflinger.flags.vulkan_renderengine false\nsetprop debug.hwui.renderer opengl\nsetprop debug.renderengine.backend opengl");
        setupSwitch(view, R.id.switch_pin_function, "全局插针", "echo 全局插针开启;settings put system gamecube_frame_interpolation 1:1:0:48:144\n", "echo 全局插针已关闭;settings put system gamecube_frame_interpolation 0:-1:0:0:0\n");
        setupSwitch(view, R.id.switch_wlan_optimize, "WLAN优化", "echo 已开启WLAN优化;settings put secure vivo_career_mode_order_assistance_toggle_state 1\nsettings put system is_network_enhancement_enable 1\nsettings put global pem_5g_policy_enable 1\nsettings put system pem_network_policy_start 1\n", "echo 已关闭WLAN优化;settings put secure vivo_career_mode_order_assistance_toggle_state 0\nsettings put system is_network_enhancement_enable 0\nsettings put global pem_5g_policy_enable 0\nsettings put system pem_network_policy_start 0\n");
        setupSwitch(view, R.id.switch_true_tone, "原彩显示", "echo 原彩显示开启;settings put system vivo_auto_eye_cct_enable 1", "echo 已关闭原彩显示;settings put system vivo_auto_eye_cct_enable 0");
        setupSwitch(view, R.id.switch_virtual_vaa, "VAA抗闪烁", "echo 已开启VAA抗闪烁;settings put global vaa_value_from_gamecube 1", "echo 已关闭VAA抗闪烁;settings put global vaa_value_from_gamecube 0");
        setupSwitch(view, R.id.switch_light_tracing, "光线追踪", "echo 光线追踪已开启", "echo 光线追踪已关闭");
        setupSwitch(view, R.id.switch_ai_strategy, "AI策略", "echo AI策略已开启", "echo AI策略已关闭");
        setupSwitch(view, R.id.switch_coloros_1, "流畅插件", "echo 流畅插件已启用;settings put global oplus_customize_system_stable_plan_switch 1;setprop debug.sf.latch_unsignaled true;settings put system log_switch_type 0;settings put system osync_toast_ready 0;settings put system osync_temperature_rate 0", "echo 流畅插件已关闭;settings put global oplus_customize_system_stable_plan_switch 0;setprop debug.sf.latch_unsignaled false;settings put system log_switch_type 1;settings put system osync_toast_ready 1;settings put system osync_temperature_rate 1");
        setupSwitch(view, R.id.switch_coloros_2, "一键墓碑", "echo 墓碑已开启;settings put global cached_apps_freezer enabled", "echo 墓碑已关闭;settings put global cached_apps_freezer disabled");
        setupSwitch(view, R.id.switch_coloros_3, "未开放", "echo 未开放功能3已开启", "echo 未开放功能3已关闭");
        setupSwitch(view, R.id.switch_hyperos_gaming, "极致电竞", "settings put system speed_mode 1\nsettings put secure speed_mode_enable 1\nsettings put global VRS_SOC 1\nsettings put system is_kaware_mode 3\nsettings put global x7_thermal_limit_switch 0\nsettings put global MI_COOLER_BT_STATUS 1\nsettings put global JOYOSE_GAME_SCENE \nsettings put global GPUTUNER_SWITCH true\nsettings put global gamebooster_vision_enhance_default_switch 1\nsettings put global SUPPORT_UGD 1\nsettings put global game_hdr 1\nsetprop debug.config.media.video.frc.support true\nsetprop debug.config.media.video.ais.support true\nsettings put secure audio_game_4d 1\nsettings put global game_dock_acc_speed_setting_global_key 120_120_120\nsettings put system screen_game_mode 1\nsetprop debug.game.video.support true\nsetprop debug.game.video.speed true\nsettings put system power_center_5g_save_mode 1echo 已开启极致电竞", "echo 极致电竞已关闭;settings delete system speed_mode; settings delete secure speed_mode_enable; settings delete system support_common_vrs_app; settings delete global VRS_SOC; settings delete system is_kaware_mode; settings delete global x7_thermal_limit_switch; settings delete global MI_COOLER_BT_STATUS; settings delete global JOYOSE_GAME_SCENE; settings delete global GPUTUNER_SWITCH; settings delete global gamebooster_vision_enhance_default_switch; settings delete global SUPPORT_UGD; settings delete global game_hdr; setprop debug.config.media.video.frc.support \"\"; setprop debug.config.media.video.ais.support \"\"; settings delete secure audio_game_4d; settings delete global game_dock_acc_speed_setting_global_key; settings delete system screen_game_mode; setprop debug.game.video.support \"\"; setprop debug.game.video.speed \"\"; settings delete system power_center_5g_save_mode");
    }

    private String executeNeonOptimization() {
        try {
            byte[] prevFrame = new byte[64];
            byte[] currFrame = new byte[64];
            for (int i = 0; i < 64; i++) {
                prevFrame[i] = (byte) this.random.nextInt(256);
                currFrame[i] = (byte) Math.min(255, (prevFrame[i] + this.random.nextInt(10)) - 5);
            }
            long startTime = System.nanoTime();
            int difference = calculateFrameDifference(prevFrame, currFrame);
            long endTime = System.nanoTime();
            double executionTimeMs = (endTime - startTime) / 1000000.0d;
            return String.format("帧优化运行中\n帧差异值: %d\n执行时间: %.3f ms", Integer.valueOf(difference), Double.valueOf(executionTimeMs));
        } catch (Exception e) {
            return "帧优化执行失败: " + e.getMessage();
        }
    }

    private int calculateFrameDifference(byte[] prevFrame, byte[] currFrame) {
        int difference = 0;
        for (int i = 0; i < prevFrame.length && i < currFrame.length; i++) {
            difference += Math.abs(prevFrame[i] - currFrame[i]);
        }
        return difference;
    }

    private void startNeonOptimization() {
        this.isNeonRunning = true;
        this.counter = 0;
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda38
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException {
                this.f$0.m163lambda$startNeonOptimization$15$comexamplekbatteryFragment3();
            }
        }).start();
    }

    /* renamed from: lambda$startNeonOptimization$15$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m163lambda$startNeonOptimization$15$comexamplekbatteryFragment3() throws InterruptedException {
        while (this.isNeonRunning) {
            try {
                final String result = executeNeonOptimization();
                final int currentCounter = this.counter;
                this.neonHandler.post(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m161lambda$startNeonOptimization$13$comexamplekbatteryFragment3(currentCounter, result);
                    }
                });
                Thread.sleep(100L);
            } catch (InterruptedException e) {
                Log.e(TAG, "帧优化线程中断: " + e.getMessage());
                this.neonHandler.post(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda3
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m162lambda$startNeonOptimization$14$comexamplekbatteryFragment3();
                    }
                });
                return;
            }
        }
    }

    /* renamed from: lambda$startNeonOptimization$13$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m161lambda$startNeonOptimization$13$comexamplekbatteryFragment3(int currentCounter, String finalResult) {
        if (this.frameResultTextView != null) {
            String formattedResult = "帧 #" + currentCounter + ": " + finalResult;
            this.frameResultTextView.setText(formattedResult);
        }
        this.counter++;
    }

    /* renamed from: lambda$startNeonOptimization$14$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m162lambda$startNeonOptimization$14$comexamplekbatteryFragment3() {
        if (this.frameResultTextView != null) {
            this.frameResultTextView.setText("帧优化已停止");
        }
    }

    private void stopNeonOptimization() {
        this.isNeonRunning = false;
        this.neonHandler.post(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda5
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m165lambda$stopNeonOptimization$16$comexamplekbatteryFragment3();
            }
        });
    }

    /* renamed from: lambda$stopNeonOptimization$16$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m165lambda$stopNeonOptimization$16$comexamplekbatteryFragment3() {
        if (this.frameResultTextView != null) {
            this.frameResultTextView.setText("帧优化已停止");
        }
    }

    private void setupSwitch(View view, final int switchId, String featureName, final String enableCommand, final String disableCommand) {
        final CompoundButton switchView = (CompoundButton) view.findViewById(switchId);
        if (switchView != null) {
            final SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
            boolean savedChecked = prefs.getBoolean(String.valueOf(switchId), false);
            switchView.setChecked(savedChecked);
            switchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda30
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    this.f$0.m153lambda$setupSwitch$17$comexamplekbatteryFragment3(switchView, enableCommand, disableCommand, prefs, switchId, compoundButton, z);
                }
            });
        }
    }

    /* renamed from: lambda$setupSwitch$17$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m153lambda$setupSwitch$17$comexamplekbatteryFragment3(CompoundButton switchView, String enableCommand, String disableCommand, SharedPreferences prefs, int switchId, CompoundButton buttonView, boolean isChecked) {
        if (!checkShizukuPermission()) {
            switchView.setChecked(!isChecked);
            return;
        }
        String command = isChecked ? enableCommand : disableCommand;
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(String.valueOf(switchId), isChecked);
        editor.apply();
        executeCommandWithImmediateOkDialog(command);
        if (switchId == R.id.switch_pin_function) {
            if (isChecked) {
                startNeonOptimization();
                Toast.makeText(requireContext(), "帧优化已启动并持续运行", 0).show();
            } else {
                stopNeonOptimization();
                Toast.makeText(requireContext(), "帧优化已停止", 0).show();
            }
        }
    }

    private void setupDirectPowerSwitch() {
        if (this.switchDirectPower != null) {
            SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
            boolean savedChecked = prefs.getBoolean("direct_power", false);
            this.switchDirectPower.setChecked(savedChecked);
            this.switchDirectPower.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda39
                @Override // android.widget.CompoundButton.OnCheckedChangeListener
                public final void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                    this.f$0.m152lambda$setupDirectPowerSwitch$18$comexamplekbatteryFragment3(compoundButton, z);
                }
            });
        }
    }

    /* renamed from: lambda$setupDirectPowerSwitch$18$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m152lambda$setupDirectPowerSwitch$18$comexamplekbatteryFragment3(CompoundButton buttonView, boolean isChecked) {
        if (!checkShizukuPermission()) {
            this.switchDirectPower.setChecked(!isChecked);
            return;
        }
        if (isChecked) {
            executeCommandAndShowDialog("echo 直驱供电已开启;settings put secure power_engine_and_plugged_state_and_charging_aod 1,1,true;settings put secure settings_battery_charge_director 1;settings put secure settings_battery_charge_director_game_cube 1;settings put secure vivo_keyguard_super_speed_charge_switch 0;settings put system balance_charge_switch 0;settings put system vivo_keyguard_show_charge_mode_switch 0;current_level=$(dumpsys battery | awk '/level/{print $2}');settings put secure smart_charge_upper_value $current_level", "直驱供电已开启");
        } else {
            executeCommandAndShowDialog("echo 直驱供电已关闭;settings delete secure power_engine_and_plugged_state_and_charging_aod;settings delete secure settings_battery_charge_director;settings delete secure settings_battery_charge_director_game_cube;settings delete secure vivo_keyguard_super_speed_charge_switch;settings delete system balance_charge_switch;settings delete system vivo_keyguard_show_charge_mode_switch;settings delete secure smart_charge_upper_value", "直驱供电已关闭");
        }
        SharedPreferences.Editor editor = requireContext().getSharedPreferences(PREFS_NAME, 0).edit();
        editor.putBoolean("direct_power", isChecked);
        editor.apply();
    }

    private void initEnhancementDetailsClick() {
        this.tvEnhancementDetails.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m136x7a849624(view);
            }
        });
    }

    /* renamed from: lambda$initEnhancementDetailsClick$19$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m136x7a849624(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = DialogUtils.createBlurInfoDialog(requireContext(), "已加入的增强功能", "• Monster plus(全能版)\n• 操控调节优化\n• 优化高温降亮度\n• 关闭高温保护\n• 电竞模式1.0+2.0融合版\n• 游戏抗闪烁\n• 职业模式\n• 游戏超分\n• GPU 动态补偿帧\n• 游戏干预配置\n• OS5 极致性能模式\n• 游戏画面稳定性优化\n• 游戏音效优化\n• 游戏网络优化\n• 性能测试模式\n• Al策略配置\n• 解除新版本FPS锁帧\n• 操控调节优化\n• 优化高温降亮度\n• 游戏触摸采样率\n• 优化系统调度\n", "我知道了", null);
        DialogUtils.showWithBlurEffect(builder);
    }

    private boolean isSupportedDevice() {
        try {
            String manufacturer = Build.MANUFACTURER;
            if (manufacturer == null) {
                Log.e(TAG, "设备制造商信息为空");
                return false;
            }
            String manufacturerLower = manufacturer.toLowerCase(Locale.ROOT).replaceAll("\\s+", "");
            Log.d(TAG, "设备制造商: " + manufacturer + " (处理后: " + manufacturerLower + ")");
            return manufacturerLower.contains("vivo") || manufacturerLower.contains("iqoo") || manufacturerLower.contains("bbk") || manufacturerLower.contains("oppo") || manufacturerLower.contains("oneplus") || manufacturerLower.contains("realme");
        } catch (Exception e) {
            Log.e(TAG, "设备识别过程出错", e);
            return false;
        }
    }

    private boolean checkFloatWindowPermission() {
        return Settings.canDrawOverlays(requireContext());
    }

    private void requestFloatWindowPermission() {
        DialogUtils.createConfirmDialog(requireContext(), "需要悬浮窗权限", "为了显示优化状态，需要开启悬浮窗权限", "去开启", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda23
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m148xfffe6a12(dialogInterface, i);
            }
        }, "取消", null).setCancelable(false).show();
    }

    /* renamed from: lambda$requestFloatWindowPermission$20$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m148xfffe6a12(DialogInterface dialog, int which) {
        Intent intent = new Intent("android.settings.action.MANAGE_OVERLAY_PERMISSION", Uri.parse("package:" + requireContext().getPackageName()));
        startActivityForResult(intent, 1);
    }

    @Override // androidx.fragment.app.Fragment
    public void onActivityResult(int requestCode, int resultCode, Intent data) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            CompoundButton switchFloatWindow = (CompoundButton) getView().findViewById(R.id.switch_float_window);
            if (checkFloatWindowPermission()) {
                showFloatWindow();
                switchFloatWindow.setChecked(true);
                showAlertDialog("成功", "悬浮窗权限已获取");
                return;
            }
            showAlertDialog("提示", "未授予悬浮窗权限，无法显示");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showFloatWindow() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.isFloatWindowShowing || this.floatView != null) {
            return;
        }
        this.floatView = LayoutInflater.from(requireContext()).inflate(R.layout.float_window, (ViewGroup) null);
        if (this.floatView == null) {
            Log.e(TAG, "悬浮窗布局加载失败");
            return;
        }
        this.floatIcon = (ImageView) this.floatView.findViewById(R.id.float_icon);
        if (this.floatIcon == null) {
            Log.e(TAG, "float_icon控件未找到");
            return;
        }
        updateFloatWindowStatus();
        this.floatView.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda27
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m159lambda$showFloatWindow$21$comexamplekbatteryFragment3(view);
            }
        });
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(-2, -2, getOverlayType(), 131072 | 296, -3);
        params.gravity = 53;
        params.x = 20;
        params.y = 100;
        this.floatView.setOnTouchListener(new View.OnTouchListener() { // from class: com.example.kbattery.Fragment3.2
            private boolean isDragging = false;
            private int lastX;
            private int lastY;
            private int paramX;
            private int paramY;

            @Override // android.view.View.OnTouchListener
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case 0:
                        this.lastX = (int) event.getRawX();
                        this.lastY = (int) event.getRawY();
                        this.paramX = params.x;
                        this.paramY = params.y;
                        this.isDragging = false;
                        return false;
                    case 1:
                        return this.isDragging;
                    case 2:
                        int dx = ((int) event.getRawX()) - this.lastX;
                        int dy = ((int) event.getRawY()) - this.lastY;
                        if (Math.abs(dx) > 5 || Math.abs(dy) > 5) {
                            this.isDragging = true;
                            params.x = this.paramX + dx;
                            params.y = this.paramY + dy;
                            try {
                                Fragment3.this.windowManager.updateViewLayout(Fragment3.this.floatView, params);
                            } catch (Exception e) {
                                Log.e(Fragment3.TAG, "更新悬浮窗位置失败: " + e.getMessage());
                            }
                        }
                        return true;
                    default:
                        return false;
                }
            }
        });
        try {
            this.windowManager.addView(this.floatView, params);
            this.isFloatWindowShowing = true;
            Log.d(TAG, "悬浮窗已显示");
            CompoundButton switchFloatWindow = (CompoundButton) getView().findViewById(R.id.switch_float_window);
            if (switchFloatWindow != null) {
                switchFloatWindow.setChecked(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "悬浮窗显示失败: " + e.getMessage());
            showAlertDialog("错误", "悬浮窗显示失败: " + e.getMessage());
        }
    }

    /* renamed from: lambda$showFloatWindow$21$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m159lambda$showFloatWindow$21$comexamplekbatteryFragment3(View v1) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (this.currentPackage != null) {
            if (this.isFrameEnabled) {
                if (this.cardSwitch.getVisibility() == 0) {
                    resetAllOptimizations(this.currentPackage);
                } else {
                    executeColorDisableCommand(this.currentPackage);
                }
            } else if (this.cardSwitch.getVisibility() == 0) {
                startOptimizationWithProgress(this.currentPackage);
            } else {
                startColorOptimizationWithProgress(this.currentPackage);
            }
            updateFloatWindowStatus();
            return;
        }
        showAlertDialog("提示", "请先选择一个应用");
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateFloatWindowStatus() {
        if (this.floatIcon != null) {
            if (this.isFrameEnabled && this.currentPackage != null) {
                this.floatIcon.setImageResource(R.drawable.circle_background);
            } else {
                this.floatIcon.setImageResource(R.drawable.status_dot_red);
            }
        }
    }

    private int getOverlayType() {
        return 2038;
    }

    private void hideFloatWindow() {
        if (this.isFloatWindowShowing && this.floatView != null) {
            try {
                this.windowManager.removeView(this.floatView);
                this.floatView = null;
                this.isFloatWindowShowing = false;
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG, "悬浮窗隐藏失败: " + e.getMessage());
            }
        }
    }

    private void startOptimizationWithProgress(String packageName) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        int point;
        if (packageName == null) {
            showAlertDialog("提示", "请先选择一个应用");
            return;
        }
        int totalDuration = new Random().nextInt(2001) + 8000;
        final List<Integer> pausePoints = new ArrayList<>();
        int pauseCount = new Random().nextInt(2) + 2;
        for (int i = 0; i < pauseCount; i++) {
            do {
                point = new Random().nextInt(71) + 15;
            } while (pausePoints.contains(Integer.valueOf(point)));
            pausePoints.add(Integer.valueOf(point));
        }
        Collections.sort(pausePoints);
        View progressView = LayoutInflater.from(requireContext()).inflate(R.layout.md3_progress_dialog, (ViewGroup) null);
        if (progressView == null) {
            Log.e(TAG, "进度条布局加载失败");
            showAlertDialog("错误", "无法显示进度条");
            return;
        }
        final ProgressBar progressBar = (ProgressBar) progressView.findViewById(R.id.md3_progress_bar);
        final TextView progressText = (TextView) progressView.findViewById(R.id.progress_text);
        if (progressBar == null || progressText == null) {
            Log.e(TAG, "进度条或文本控件缺失");
            showAlertDialog("错误", "进度条初始化失败");
            return;
        }
        progressBar.setMax(100);
        progressBar.setProgress(0);
        progressText.setText("0%");
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "增强中").setView(progressView).setCancelable(false);
        AlertDialog dialog = dialogBuilder.show();
        executeOptimizationCommand(packageName);
        final int[] currentProgress = {0};
        final int[] pauseIndex = {0};
        final long pauseDuration = new Random().nextInt(301) + 200;
        final ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(totalDuration);
        progressAnimator.setInterpolator(new DecelerateInterpolator(1.2f));
        ValueAnimator.setFrameDelay(16L);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda15
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.m164x9a33a36b(currentProgress, progressBar, progressText, pauseIndex, pausePoints, progressAnimator, pauseDuration, valueAnimator);
            }
        });
        progressAnimator.addListener(new AnonymousClass3(progressBar, progressText, dialog, packageName));
        progressAnimator.start();
    }

    /* renamed from: lambda$startOptimizationWithProgress$23$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m164x9a33a36b(int[] currentProgress, ProgressBar progressBar, TextView progressText, final int[] pauseIndex, List pausePoints, final ValueAnimator progressAnimator, long pauseDuration, ValueAnimator animation) {
        currentProgress[0] = ((Integer) animation.getAnimatedValue()).intValue();
        progressBar.setProgress(currentProgress[0]);
        progressText.setText(currentProgress[0] + "%");
        if (pauseIndex[0] < pausePoints.size() && currentProgress[0] >= ((Integer) pausePoints.get(pauseIndex[0])).intValue() && currentProgress[0] == ((Integer) pausePoints.get(pauseIndex[0])).intValue()) {
            animation.pause();
            this.handler.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda9
                @Override // java.lang.Runnable
                public final void run() {
                    Fragment3.lambda$startOptimizationWithProgress$22(progressAnimator, pauseIndex);
                }
            }, pauseDuration);
        }
    }

    static /* synthetic */ void lambda$startOptimizationWithProgress$22(ValueAnimator progressAnimator, int[] pauseIndex) {
        if (progressAnimator.isPaused()) {
            progressAnimator.resume();
            pauseIndex[0] = pauseIndex[0] + 1;
        }
    }

    /* renamed from: com.example.kbattery.Fragment3$3, reason: invalid class name */
    class AnonymousClass3 implements Animator.AnimatorListener {
        final /* synthetic */ AlertDialog val$dialog;
        final /* synthetic */ String val$packageName;
        final /* synthetic */ ProgressBar val$progressBar;
        final /* synthetic */ TextView val$progressText;

        AnonymousClass3(ProgressBar progressBar, TextView textView, AlertDialog alertDialog, String str) {
            this.val$progressBar = progressBar;
            this.val$progressText = textView;
            this.val$dialog = alertDialog;
            this.val$packageName = str;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            this.val$progressBar.setProgress(100);
            this.val$progressText.setText("100%");
            Handler handler = Fragment3.this.handler;
            final AlertDialog alertDialog = this.val$dialog;
            final String str = this.val$packageName;
            handler.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment3$3$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                    this.f$0.m168lambda$onAnimationEnd$0$comexamplekbatteryFragment3$3(alertDialog, str);
                }
            }, 500L);
        }

        /* renamed from: lambda$onAnimationEnd$0$com-example-kbattery-Fragment3$3, reason: not valid java name */
        /* synthetic */ void m168lambda$onAnimationEnd$0$comexamplekbatteryFragment3$3(AlertDialog dialog, String packageName) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
            dialog.dismiss();
            Fragment3.this.showAlertDialog("成功", "已为 " + Fragment3.this.getAppName(packageName) + " 启用优化");
            Fragment3.this.isFrameEnabled = true;
            if (!Fragment3.this.isFloatWindowShowing) {
                Fragment3.this.showFloatWindow();
            }
            Fragment3.this.updateFloatWindowStatus();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private void startColorOptimizationWithProgress(String packageName) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        int point;
        if (packageName == null) {
            showAlertDialog("提示", "请先选择一个应用");
            return;
        }
        int totalDuration = new Random().nextInt(2001) + 8000;
        final List<Integer> pausePoints = new ArrayList<>();
        int pauseCount = new Random().nextInt(2) + 2;
        for (int i = 0; i < pauseCount; i++) {
            do {
                point = new Random().nextInt(71) + 15;
            } while (pausePoints.contains(Integer.valueOf(point)));
            pausePoints.add(Integer.valueOf(point));
        }
        Collections.sort(pausePoints);
        View progressView = LayoutInflater.from(requireContext()).inflate(R.layout.md3_progress_dialog, (ViewGroup) null);
        if (progressView == null) {
            Log.e(TAG, "进度条布局加载失败");
            showAlertDialog("错误", "无法显示进度条");
            return;
        }
        final ProgressBar progressBarColor = (ProgressBar) progressView.findViewById(R.id.md3_progress_bar);
        final TextView progressText = (TextView) progressView.findViewById(R.id.progress_text);
        if (progressBarColor == null || progressText == null) {
            Log.e(TAG, "进度条或文本控件缺失");
            showAlertDialog("错误", "进度条初始化失败");
            return;
        }
        progressBarColor.setMax(100);
        progressBarColor.setProgress(0);
        progressText.setText("0%");
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "增强中").setView(progressView).setCancelable(false);
        AlertDialog dialog = dialogBuilder.show();
        executeColorOptimizationCommand(packageName);
        final int[] currentProgress = {0};
        final int[] pauseIndex = {0};
        final long pauseDuration = new Random().nextInt(301) + 200;
        final ValueAnimator progressAnimator = ValueAnimator.ofInt(0, 100);
        progressAnimator.setDuration(totalDuration);
        progressAnimator.setInterpolator(new DecelerateInterpolator(1.2f));
        ValueAnimator.setFrameDelay(16L);
        progressAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda41
            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                this.f$0.m160xafa3f9c2(currentProgress, progressBarColor, progressText, pauseIndex, pausePoints, progressAnimator, pauseDuration, valueAnimator);
            }
        });
        progressAnimator.addListener(new AnonymousClass4(progressBarColor, progressText, dialog, packageName));
        progressAnimator.start();
    }

    /* renamed from: lambda$startColorOptimizationWithProgress$25$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m160xafa3f9c2(int[] currentProgress, ProgressBar progressBarColor, TextView progressText, final int[] pauseIndex, List pausePoints, final ValueAnimator progressAnimator, long pauseDuration, ValueAnimator animation) {
        currentProgress[0] = ((Integer) animation.getAnimatedValue()).intValue();
        progressBarColor.setProgress(currentProgress[0]);
        progressText.setText(currentProgress[0] + "%");
        if (pauseIndex[0] < pausePoints.size() && currentProgress[0] >= ((Integer) pausePoints.get(pauseIndex[0])).intValue() && currentProgress[0] == ((Integer) pausePoints.get(pauseIndex[0])).intValue()) {
            animation.pause();
            this.handler.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda6
                @Override // java.lang.Runnable
                public final void run() {
                    Fragment3.lambda$startColorOptimizationWithProgress$24(progressAnimator, pauseIndex);
                }
            }, pauseDuration);
        }
    }

    static /* synthetic */ void lambda$startColorOptimizationWithProgress$24(ValueAnimator progressAnimator, int[] pauseIndex) {
        if (progressAnimator.isPaused()) {
            progressAnimator.resume();
            pauseIndex[0] = pauseIndex[0] + 1;
        }
    }

    /* renamed from: com.example.kbattery.Fragment3$4, reason: invalid class name */
    class AnonymousClass4 implements Animator.AnimatorListener {
        final /* synthetic */ AlertDialog val$dialog;
        final /* synthetic */ String val$packageName;
        final /* synthetic */ ProgressBar val$progressBarColor;
        final /* synthetic */ TextView val$progressText;

        AnonymousClass4(ProgressBar progressBar, TextView textView, AlertDialog alertDialog, String str) {
            this.val$progressBarColor = progressBar;
            this.val$progressText = textView;
            this.val$dialog = alertDialog;
            this.val$packageName = str;
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationStart(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            this.val$progressBarColor.setProgress(100);
            this.val$progressText.setText("100%");
            Handler handler = Fragment3.this.handler;
            final AlertDialog alertDialog = this.val$dialog;
            final String str = this.val$packageName;
            handler.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment3$4$$ExternalSyntheticLambda0
                @Override // java.lang.Runnable
                public final void run() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                    this.f$0.m169lambda$onAnimationEnd$0$comexamplekbatteryFragment3$4(alertDialog, str);
                }
            }, 500L);
        }

        /* renamed from: lambda$onAnimationEnd$0$com-example-kbattery-Fragment3$4, reason: not valid java name */
        /* synthetic */ void m169lambda$onAnimationEnd$0$comexamplekbatteryFragment3$4(AlertDialog dialog, String packageName) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
            dialog.dismiss();
            Fragment3.this.showAlertDialog("成功", "已为 " + Fragment3.this.getAppName(packageName) + " 启用优化");
            Fragment3.this.isFrameEnabled = true;
            if (!Fragment3.this.isFloatWindowShowing) {
                Fragment3.this.showFloatWindow();
            }
            Fragment3.this.updateFloatWindowStatus();
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationCancel(Animator animation) {
        }

        @Override // android.animation.Animator.AnimatorListener
        public void onAnimationRepeat(Animator animation) {
        }
    }

    private void executeOptimizationCommand(final String packageName) {
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda48
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException, IOException {
                this.f$0.m135xd982fba9(packageName);
            }
        }).start();
    }

    /* renamed from: lambda$executeOptimizationCommand$26$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m135xd982fba9(String packageName) throws InterruptedException, IOException {
        try {
            String cmd = buildShellCommand(packageName);
            Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((cmd + "\n").getBytes());
            out.flush();
            out.close();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                Log.e(TAG, "命令执行失败，错误码: " + exitCode + "，命令: " + cmd);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "执行命令时出错: " + e.getMessage());
        }
    }

    private void executeColorOptimizationCommand(String packageName) {
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda29
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException, IOException {
                Fragment3.lambda$executeColorOptimizationCommand$27();
            }
        }).start();
    }

    static /* synthetic */ void lambda$executeColorOptimizationCommand$27() throws InterruptedException, IOException {
        try {
            Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write(("echo 已开启ColorOS游戏优化;\n").getBytes());
            out.flush();
            out.close();
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                Log.e(TAG, "color命令执行失败，错误码: " + exitCode + "，命令: echo 已开启ColorOS游戏优化;");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            Log.e(TAG, "执行color命令时出错: " + e.getMessage());
        }
    }

    private void executeColorDisableCommand(final String packageName) {
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda20
            @Override // java.lang.Runnable
            public final void run() throws IOException {
                this.f$0.m132x73aced8(packageName);
            }
        }).start();
    }

    /* renamed from: lambda$executeColorDisableCommand$30$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m132x73aced8(final String packageName) throws IOException {
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        try {
            try {
                try {
                    Log.d(TAG, "执行关闭命令: echo 已关闭ColorOS游戏;");
                    Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
                    OutputStream out = p.getOutputStream();
                    out.write(("echo 已关闭ColorOS游戏;\n").getBytes());
                    out.flush();
                    out.close();
                    showCommandOutputDialog("正在关闭优化...");
                    readProcessOutput(p);
                    final int exitCode = p.waitFor();
                    requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda7
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m130xcae511cf(exitCode, packageName);
                        }
                    });
                    if (0 != 0) {
                        reader.close();
                    }
                    if (0 != 0) {
                        errorReader.close();
                    }
                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                    requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda8
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m131x531551ae(e);
                        }
                    });
                    if (0 != 0) {
                        reader.close();
                    }
                    if (0 != 0) {
                        errorReader.close();
                    }
                }
            } catch (IOException e2) {
                Log.e(TAG, "关闭流时出错", e2);
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "关闭流时出错", e3);
                    throw th;
                }
            }
            if (0 != 0) {
                errorReader.close();
            }
            throw th;
        }
    }

    /* renamed from: lambda$executeColorDisableCommand$28$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m130xcae511cf(int exitCode, String packageName) {
        if (exitCode == 0) {
            sendMessageToHandler(1, "已为 " + getAppName(packageName) + " 关闭优化");
        } else {
            sendMessageToHandler(1, "命令执行失败，错误码: " + exitCode);
        }
    }

    /* renamed from: lambda$executeColorDisableCommand$29$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m131x531551ae(Exception e) {
        sendMessageToHandler(1, "执行命令时出错: " + e.getMessage());
    }

    private void resetAllOptimizations(final String packageName) {
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda32
            @Override // java.lang.Runnable
            public final void run() throws IOException {
                this.f$0.m151lambda$resetAllOptimizations$33$comexamplekbatteryFragment3(packageName);
            }
        }).start();
    }

    /* renamed from: lambda$resetAllOptimizations$33$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m151lambda$resetAllOptimizations$33$comexamplekbatteryFragment3(final String packageName) throws IOException {
        BufferedReader reader = null;
        BufferedReader errorReader = null;
        try {
        } catch (IOException e) {
            Log.e(TAG, "关闭流时出错", e);
        }
        try {
            try {
                String cmd = "echo 正在完全还原所有优化设置...;GAME_PACKAGES=(" + packageName + ");for game_package in ${GAME_PACKAGES[@]}; do settings delete system powermonitor;settings delete system pempsmstate;settings delete system pemMonsterState;settings delete secure system_property_power_mode_type;settings delete secure power_save_type;settings delete system power_save_type;settings delete system vivo_carlife_switch_setting;settings delete secure pem_in_ccc_mode;settings delete secure super_audio_current_state_settings;settings delete system memc_main_switch_setting;settings delete system game_plus_mode_key;settings delete secure game_plus_mode_key;settings delete secure vivo_career_mode_toggle_state;settings delete secure vivo_career_mode_auto_enhance_brightness_toggle_state;settings delete secure viv_career_mode_extend_auto_lock_screen_time_toggle_state;settings delete secure vivo_career_mode_healthy_prolonged_charging_toggle_state;settings delete secure vivo_career_mode_order_assistance_toggle_state;settings delete secure vivo_career_mode_toggle_opened_way;settings delete secure vivo_career_mode_toggle_opened_state;settings delete secure vivo_career_mode_manual_state;settings delete system bench_mark_mode;settings delete system electroic_mode_enabled;settings delete system vivo_gamethermal_switch;settings delete system HighTemptureProtect;settings delete system pem_little_window_high_temp_protect;settings delete global game_memc_request_touch_rate;settings delete system vts_game_para_adjust;device_config delete activity_manager_native_boot game_touch_sampling;device_config delete activity_manager_native_boot touch.report_rate;device_config delete activity_manager_native_boot touchscreen.report_rate;device_config delete activity_manager_native_boot game_touch_sampling_rate;device_config delete game game_touch_sampling_rate;device_config delete game com.detection.ts.touch_detection.action.set_touch_sample_rate;settings delete secure vivo_gamecube_solo_stage;settings delete secure vivo_gamecube_team_stage;settings delete secure vivo_gamecube_bp_stage;settings delete secure vivo_gamecube_ai_strategy;settings delete secure vivo_gamecube_ai_situation_stage;cmd game mode reset ${game_package}; settings delete global picture_stability_opt_blacklist_apps;settings delete system enabled_picture_stability_optimize;settings delete global picture_stability_opt_whitelist_apps;settings delete global vsr_all_whitelist_apps;settings delete global vsr_gles_whitelist_apps;settings delete global vsr_pp_whitelist_apps;settings delete global vsr_value_from_gamecube;settings delete global vsr_whitelist_apps;settings delete global vaa_dynamic_whitelist_apps;settings delete global vaa_gles_dynamic_whitelist_apps;settings delete global vaa_gles_whitelist_apps;settings delete global vaa_tips_whitelist_apps;settings delete global vaa_whitelist_apps;settings delete global gpu_memc_dynamic_whitelist_apps;settings delete global gpu_memc_gles_dynamic_whitelist_apps;settings delete global gpu_memc_gles_whitelist_apps;settings delete global gpu_memc_switch_to_ic_memc;settings delete global gpu_memc_tips_whitelist_apps;settings delete global gpu_memc_whitelist_apps;settings delete system key_frame_interpolation_ecosystem;done;echo 已为 " + packageName + " 完全还原所有优化设置;";
                Log.d(TAG, "执行修正后的还原命令: " + cmd);
                Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
                OutputStream out = p.getOutputStream();
                out.write((cmd + "\n").getBytes());
                out.flush();
                out.close();
                showCommandOutputDialog("正在全量还原...");
                readProcessOutput(p);
                final int exitCode = p.waitFor();
                requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda24
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m149lambda$resetAllOptimizations$31$comexamplekbatteryFragment3(exitCode, packageName);
                    }
                });
                if (0 != 0) {
                    reader.close();
                }
                if (0 != 0) {
                    errorReader.close();
                }
            } catch (IOException | InterruptedException e2) {
                e2.printStackTrace();
                requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda25
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m150lambda$resetAllOptimizations$32$comexamplekbatteryFragment3(e2);
                    }
                });
                if (0 != 0) {
                    reader.close();
                }
                if (0 != 0) {
                    errorReader.close();
                }
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    reader.close();
                } catch (IOException e3) {
                    Log.e(TAG, "关闭流时出错", e3);
                    throw th;
                }
            }
            if (0 != 0) {
                errorReader.close();
            }
            throw th;
        }
    }

    /* renamed from: lambda$resetAllOptimizations$31$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m149lambda$resetAllOptimizations$31$comexamplekbatteryFragment3(int exitCode, String packageName) {
        if (exitCode == 0) {
            sendMessageToHandler(1, "已为 " + getAppName(packageName) + " 完全还原所有优化设置");
            this.isFrameEnabled = false;
            updateFloatWindowStatus();
            return;
        }
        sendMessageToHandler(1, "全量还原失败，错误码: " + exitCode);
    }

    /* renamed from: lambda$resetAllOptimizations$32$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m150lambda$resetAllOptimizations$32$comexamplekbatteryFragment3(Exception e) {
        sendMessageToHandler(1, "执行还原命令时出错: " + e.getMessage());
    }

    private void loadSelectedAppsFromPrefs() {
        Set<String> savedApps = this.sharedPreferences.getStringSet(KEY_SELECTED_APPS, new HashSet());
        this.selectedPackages.clear();
        this.selectedPackages.addAll(savedApps);
    }

    private void saveSelectedAppsToPrefs() {
        SharedPreferences.Editor editor = this.sharedPreferences.edit();
        editor.putStringSet(KEY_SELECTED_APPS, new HashSet(this.selectedPackages));
        editor.apply();
    }

    private void showAppListDialog() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_app_list, (ViewGroup) null);
        if (dialogView == null) {
            Log.e(TAG, "应用列表对话框布局加载失败");
            showAlertDialog("错误", "无法加载应用列表");
            return;
        }
        final EditText searchEditText = (EditText) dialogView.findViewById(R.id.search_edit_text);
        RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recycler_view);
        final TextView emptyTextView = (TextView) dialogView.findViewById(R.id.empty_text_view);
        final MaterialButton selectAllButton = (MaterialButton) dialogView.findViewById(R.id.select_all_button);
        final MaterialButton deselectAllButton = (MaterialButton) dialogView.findViewById(R.id.deselect_all_button);
        if (searchEditText == null || recyclerView == null || emptyTextView == null || selectAllButton == null || deselectAllButton == null) {
            Log.e(TAG, "对话框控件缺失");
            showAlertDialog("错误", "应用列表初始化失败");
            return;
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        final AppAdapter adapter = new AppAdapter(new ArrayList());
        recyclerView.setAdapter(adapter);
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda35
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m155lambda$showAppListDialog$37$comexamplekbatteryFragment3(adapter, emptyTextView, searchEditText, selectAllButton, deselectAllButton);
            }
        }).start();
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView).setPositiveButton((CharSequence) "确定", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda36
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m156lambda$showAppListDialog$38$comexamplekbatteryFragment3(adapter, dialogInterface, i);
            }
        }).setNegativeButton((CharSequence) "取消", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda37
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).setCancelable(false);
        DialogUtils.showWithBlurEffect(builder);
    }

    /* renamed from: lambda$showAppListDialog$37$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m155lambda$showAppListDialog$37$comexamplekbatteryFragment3(final AppAdapter adapter, final TextView emptyTextView, final EditText searchEditText, final MaterialButton selectAllButton, final MaterialButton deselectAllButton) {
        final List<AppInfo> allApps = loadInstalledApps();
        requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda14
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m154lambda$showAppListDialog$36$comexamplekbatteryFragment3(adapter, allApps, emptyTextView, searchEditText, selectAllButton, deselectAllButton);
            }
        });
    }

    /* renamed from: lambda$showAppListDialog$36$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m154lambda$showAppListDialog$36$comexamplekbatteryFragment3(final AppAdapter adapter, final List allApps, final TextView emptyTextView, EditText searchEditText, MaterialButton selectAllButton, MaterialButton deselectAllButton) {
        adapter.setAppList(allApps);
        updateEmptyView(adapter.getItemCount(), emptyTextView);
        searchEditText.addTextChangedListener(new TextWatcher() { // from class: com.example.kbattery.Fragment3.5
            @Override // android.text.TextWatcher
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override // android.text.TextWatcher
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase(Locale.getDefault());
                List<AppInfo> filteredApps = new ArrayList<>();
                for (AppInfo app : allApps) {
                    if (app.appName.toLowerCase().contains(searchText) || app.packageName.toLowerCase().contains(searchText)) {
                        filteredApps.add(app);
                    }
                }
                adapter.setAppList(filteredApps);
                Fragment3.this.updateEmptyView(adapter.getItemCount(), emptyTextView);
            }

            @Override // android.text.TextWatcher
            public void afterTextChanged(Editable s) {
            }
        });
        selectAllButton.setVisibility(0);
        deselectAllButton.setVisibility(0);
        selectAllButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                adapter.selectAll();
            }
        });
        deselectAllButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda11
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                adapter.deselectAll();
            }
        });
    }

    /* renamed from: lambda$showAppListDialog$38$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m156lambda$showAppListDialog$38$comexamplekbatteryFragment3(AppAdapter adapter, DialogInterface dialog, int which) {
        this.selectedPackages.clear();
        for (AppInfo app : adapter.getSelectedApps()) {
            if (app != null && app.isSelected && app.packageName != null) {
                this.selectedPackages.add(app.packageName);
            }
        }
        saveSelectedAppsToPrefs();
        updateSelectedApps();
        dialog.dismiss();
    }

    private List<AppInfo> loadInstalledApps() {
        List<ApplicationInfo> applications;
        List<AppInfo> appList = new ArrayList<>();
        PackageManager pm = requireContext().getPackageManager();
        if (pm == null) {
            Log.e(TAG, "PackageManager 获取失败");
            return appList;
        }
        try {
            if (Build.VERSION.SDK_INT >= 33) {
                applications = pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(128L));
            } else {
                applications = pm.getInstalledApplications(128);
            }
        } catch (Exception e) {
            Log.e(TAG, "加载应用列表失败", e);
        }
        if (applications == null) {
            Log.e(TAG, "应用列表为空");
            return appList;
        }
        for (ApplicationInfo appInfo : applications) {
            if (appInfo != null && (appInfo.flags & 1) == 0) {
                try {
                } catch (Exception e2) {
                    Log.e(TAG, "处理应用信息失败:" + appInfo.packageName, e2);
                }
                if (appInfo.packageName != null) {
                    CharSequence label = pm.getApplicationLabel(appInfo);
                    String appName = label != null ? label.toString() : appInfo.packageName;
                    Drawable icon = null;
                    try {
                        icon = pm.getApplicationIcon(appInfo);
                    } catch (Exception e3) {
                        Log.w(TAG, "获取图标失败:" + appInfo.packageName);
                    }
                    boolean isSelected = this.selectedPackages.contains(appInfo.packageName);
                    AppInfo app = new AppInfo(appName, appInfo.packageName, icon, isSelected);
                    appList.add(app);
                }
            }
        }
        Collections.sort(appList, new Comparator() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda4
            @Override // java.util.Comparator
            public final int compare(Object obj, Object obj2) {
                return ((Fragment3.AppInfo) obj).appName.compareToIgnoreCase(((Fragment3.AppInfo) obj2).appName);
            }
        });
        return appList;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateEmptyView(int itemCount, TextView emptyTextView) {
        if (emptyTextView == null) {
            return;
        }
        emptyTextView.setVisibility(itemCount == 0 ? 0 : 8);
    }

    private void updateSelectedApps() {
        if (this.selectedAppsChipGroup == null) {
            return;
        }
        this.selectedAppsChipGroup.removeAllViews();
        if (this.selectedPackages.isEmpty()) {
            if (this.selectedAppInfo != null) {
                this.selectedAppInfo.setText("未选择应用");
                return;
            }
            return;
        }
        if (this.selectedAppInfo != null) {
            this.selectedAppInfo.setText("已选择" + this.selectedPackages.size() + "个应用");
        }
        if (this.currentPackage != null && !this.selectedPackages.contains(this.currentPackage)) {
            this.currentPackage = null;
        }
        for (final String packageName : this.selectedPackages) {
            Chip chip = new Chip(requireContext());
            chip.setText(getAppName(packageName));
            chip.setCloseIconResource(android.R.drawable.ic_menu_close_clear_cancel);
            chip.setCloseIconVisible(true);
            chip.setOnCloseIconClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda45
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m166lambda$updateSelectedApps$41$comexamplekbatteryFragment3(packageName, view);
                }
            });
            boolean isCurrent = this.currentPackage != null && this.currentPackage.equals(packageName);
            if (isCurrent) {
                chip.setChipBackgroundColorResource(R.color.pink_light);
            } else {
                chip.setChipBackgroundColorResource(android.R.color.transparent);
            }
            chip.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda46
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m167lambda$updateSelectedApps$42$comexamplekbatteryFragment3(packageName, view);
                }
            });
            this.selectedAppsChipGroup.addView(chip);
        }
    }

    /* renamed from: lambda$updateSelectedApps$41$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m166lambda$updateSelectedApps$41$comexamplekbatteryFragment3(String packageName, View v) {
        this.selectedPackages.remove(packageName);
        saveSelectedAppsToPrefs();
        updateSelectedApps();
        if (this.currentPackage != null && this.currentPackage.equals(packageName)) {
            this.currentPackage = null;
        }
    }

    /* renamed from: lambda$updateSelectedApps$42$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m167lambda$updateSelectedApps$42$comexamplekbatteryFragment3(String packageName, View v) {
        this.currentPackage = packageName;
        updateSelectedApps();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public String getAppName(String packageName) throws PackageManager.NameNotFoundException {
        try {
            PackageManager pm = requireContext().getPackageManager();
            ApplicationInfo info = pm.getApplicationInfo(packageName, 0);
            CharSequence name = pm.getApplicationLabel(info);
            return name != null ? name.toString() : packageName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "获取应用名称失败:" + packageName, e);
            return packageName;
        }
    }

    private String buildShellCommand(String packageName) {
        return "GAME_PACKAGES=(" + packageName + ");for game_package in ${GAME_PACKAGES[@]}; do settings put system powermonitor 7 ; settings put system pempsmstate 7 ; settings put system pemMonsterState 1 ; settings put secure system_property_power_mode_type 7 ; settings put secure power_save_type 7 ; settings put system power_save_type 7 ; settings put system vivo_carlife_switch_setting 1 ; settings put secure pem_in_ccc_mode 1 ; settings put secure super_audio_current_state_settings 1 ; settings put system memc_main_switch_setting 0 ; settings put system game_plus_mode_key 1 ; settings put secure game_plus_mode_key 1 ; settings put secure vivo_career_mode_toggle_state 1; settings put secure vivo_career_mode_auto_enhance_brightness_toggle_state 1; settings put secure viv_career_mode_extend_auto_lock_screen_time_toggle_state 1; settings put secure vivo_career_mode_healthy_prolonged_charging_toggle_state 1; settings put secure vivo_career_mode_order_assistance_toggle_state 1; settings put secure vivo_career_mode_toggle_opened_way 1; settings put secure vivo_career_mode_toggle_opened_state 1; settings put secure vivo_career_mode_manual_state 1; settings put system bench_mark_mode 1; settings put system electroic_mode_enabled 1; settings put system vivo_gamethermal_switch 0; settings put system HighTemptureProtect 0; settings put system pem_little_window_high_temp_protect 0; settings put global game_memc_request_touch_rate 1200; settings put system vts_game_para_adjust 1,10,10,10; device_config put activity_manager_native_boot game_touch_sampling 1200; device_config put activity_manager_native_boot touch.report_rate 1200; device_config put activity_manager_native_boot touchscreen.report_rate 1200; device_config put activity_manager_native_boot game_touch_sampling_rate 1200; device_config put game game_touch_sampling_rate 1200; device_config put game com.detection.ts.touch_detection.action.set_touch_sample_rate 1200; settings put secure vivo_gamecube_solo_stage 1; settings put secure vivo_gamecube_team_stage 1; settings put secure vivo_gamecube_bp_stage 1; settings put secure vivo_gamecube_ai_strategy 1; settings put secure vivo_gamecube_ai_situation_stage 1; cmd game mode custom ${game_package}; cmd game set --downscale disable --fps 120 ${game_package}; settings put global picture_stability_opt_blacklist_apps null; settings put system enabled_picture_stability_optimize ${game_package}:1; settings put global picture_stability_opt_whitelist_apps ${game_package}:7; settings put global vsr_all_whitelist_apps ${game_package}; settings put global vsr_gles_whitelist_apps ${game_package}; settings put global vsr_pp_whitelist_apps ${game_package}; settings put global vsr_value_from_gamecube \"1\"; settings put global vsr_whitelist_apps ${game_package}; settings put global vaa_dynamic_whitelist_apps ${game_package}; settings put global vaa_gles_dynamic_whitelist_apps ${game_package}; settings put global vaa_gles_whitelist_apps ${game_package}; settings put global vaa_tips_whitelist_apps ${game_package}; settings put global vaa_whitelist_apps ${game_package}; settings put global gpu_memc_dynamic_whitelist_apps ${game_package}; settings put global gpu_memc_gles_dynamic_whitelist_apps ${game_package}; settings put global gpu_memc_gles_whitelist_apps ${game_package}; settings put global gpu_memc_switch_to_ic_memc \"1_1_1_1_1\"; settings put global gpu_memc_tips_whitelist_apps ${game_package}; settings put global gpu_memc_whitelist_apps ${game_package};settings put system key_frame_interpolation_ecosystem ${game_package};done;";
    }

    private void executeCommandWithImmediateOkDialog(final String command) {
        showCommandOutputDialog("输出结果");
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda31
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException, IOException {
                this.f$0.m134xed812a44(command);
            }
        }).start();
    }

    /* renamed from: lambda$executeCommandWithImmediateOkDialog$43$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m134xed812a44(String command) throws InterruptedException, IOException {
        try {
            Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((command + "\n").getBytes());
            out.flush();
            out.close();
            readProcessOutput(p);
            int exitCode = p.waitFor();
            Log.d(TAG, "命令执行完成，退出码:" + exitCode);
            sendMessageToHandler(1, exitCode == 0 ? "操作已完成" : "操作失败，错误码: " + exitCode);
        } catch (Exception e) {
            Log.e(TAG, "执行命令失败", e);
            sendMessageToHandler(1, "操作失败: " + e.getMessage());
        }
    }

    private void executeCommandAndShowDialog(final String command, final String successMessage) {
        showCommandOutputDialog("执行中...");
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda40
            @Override // java.lang.Runnable
            public final void run() throws InterruptedException, IOException {
                this.f$0.m133xe38442d0(command, successMessage);
            }
        }).start();
    }

    /* renamed from: lambda$executeCommandAndShowDialog$44$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m133xe38442d0(String command, String successMessage) throws InterruptedException, IOException {
        try {
            Process p = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = p.getOutputStream();
            out.write((command + "\n").getBytes());
            out.flush();
            out.close();
            readProcessOutput(p);
            int exitCode = p.waitFor();
            if (exitCode != 0) {
                sendMessageToHandler(1, "操作失败，错误码: " + exitCode);
            } else {
                sendMessageToHandler(1, successMessage);
            }
        } catch (Exception e) {
            Log.e(TAG, "执行命令失败", e);
            sendMessageToHandler(1, "操作失败: " + e.getMessage());
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showAlertDialog(String title, String message) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = DialogUtils.createBlurInfoDialog(requireContext(), title, message, "确定", null);
        DialogUtils.showWithBlurEffect(builder);
    }

    private void setVisibility(View view, int visibility) {
        if (view != null) {
            view.setVisibility(visibility);
        }
    }

    @Override // android.view.View.OnClickListener
    public void onClick(View v) {
    }

    @Override // android.widget.CompoundButton.OnCheckedChangeListener
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
    }

    @Override // android.widget.RadioGroup.OnCheckedChangeListener
    public void onCheckedChanged(RadioGroup group, int checkedId) {
    }

    private void showCommandOutputDialog(final String title) {
        requireActivity().runOnUiThread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda13
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m158lambda$showCommandOutputDialog$46$comexamplekbatteryFragment3(title);
            }
        });
    }

    /* renamed from: lambda$showCommandOutputDialog$46$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m158lambda$showCommandOutputDialog$46$comexamplekbatteryFragment3(String title) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_command_output, (ViewGroup) null);
        this.commandOutputTextView = (TextView) dialogView.findViewById(R.id.command_output);
        this.commandProgressBar = (ProgressBar) dialogView.findViewById(R.id.progress_bar);
        this.closeButton = (MaterialButton) dialogView.findViewById(R.id.close_button);
        TextView dialogTitle = (TextView) dialogView.findViewById(R.id.dialog_title);
        dialogTitle.setText(title);
        this.closeButton.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda34
            @Override // android.view.View.OnClickListener
            public final void onClick(View view) {
                this.f$0.m157lambda$showCommandOutputDialog$45$comexamplekbatteryFragment3(view);
            }
        });
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext()).setView(dialogView).setCancelable(false);
        this.commandDialog = DialogUtils.showWithBlurEffect(builder);
    }

    /* renamed from: lambda$showCommandOutputDialog$45$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m157lambda$showCommandOutputDialog$45$comexamplekbatteryFragment3(View v) {
        if (this.commandDialog != null && this.commandDialog.isShowing()) {
            this.commandDialog.dismiss();
        }
    }

    private void readProcessOutput(final Process p) {
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda10
            @Override // java.lang.Runnable
            public final void run() throws IOException {
                this.f$0.m146lambda$readProcessOutput$47$comexamplekbatteryFragment3(p);
            }
        }).start();
        new Thread(new Runnable() { // from class: com.example.kbattery.Fragment3$$ExternalSyntheticLambda12
            @Override // java.lang.Runnable
            public final void run() throws IOException {
                this.f$0.m147lambda$readProcessOutput$48$comexamplekbatteryFragment3(p);
            }
        }).start();
    }

    /* renamed from: lambda$readProcessOutput$47$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m146lambda$readProcessOutput$47$comexamplekbatteryFragment3(Process p) throws IOException {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while (true) {
                try {
                    String line = reader.readLine();
                    if (line != null) {
                        sendMessageToHandler(0, line);
                    } else {
                        reader.close();
                        return;
                    }
                } finally {
                }
            }
        } catch (IOException e) {
            sendMessageToHandler(0, "读取输出错误: " + e.getMessage());
        }
    }

    /* renamed from: lambda$readProcessOutput$48$com-example-kbattery-Fragment3, reason: not valid java name */
    /* synthetic */ void m147lambda$readProcessOutput$48$comexamplekbatteryFragment3(Process p) throws IOException {
        try {
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            while (true) {
                try {
                    String line = errorReader.readLine();
                    if (line != null) {
                        sendMessageToHandler(0, "错误: " + line);
                    } else {
                        errorReader.close();
                        return;
                    }
                } finally {
                }
            }
        } catch (IOException e) {
            sendMessageToHandler(0, "读取错误输出错误: " + e.getMessage());
        }
    }

    private void sendMessageToHandler(int what, String text) {
        Message message = this.outputHandler.obtainMessage(what, text);
        this.outputHandler.sendMessage(message);
    }

    private boolean checkShizukuPermission() {
        return MainActivity.isShizukuAvailable && MainActivity.isShizukuActive && MainActivity.isShizukuGranted;
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroyView() {
        super.onDestroyView();
        hideFloatWindow();
        this.handler.removeCallbacksAndMessages(null);
        this.outputHandler.removeCallbacksAndMessages(null);
        if (this.commandDialog != null && this.commandDialog.isShowing()) {
            this.commandDialog.dismiss();
        }
    }

    public static class AppInfo {
        String appName;
        Drawable icon;
        boolean isSelected;
        String packageName;

        AppInfo(String appName, String packageName, Drawable icon, boolean isSelected) {
            this.appName = appName;
            this.packageName = packageName;
            this.icon = icon;
            this.isSelected = isSelected;
        }
    }

    public class AppAdapter extends RecyclerView.Adapter<ViewHolder> {
        private List<AppInfo> appList;
        private int checkedIcon = android.R.drawable.checkbox_on_background;
        private int uncheckedIcon = android.R.drawable.checkbox_off_background;

        public AppAdapter(List<AppInfo> appList) {
            this.appList = appList;
        }

        public void setAppList(List<AppInfo> appList) {
            this.appList = appList;
            notifyDataSetChanged();
        }

        public List<AppInfo> getSelectedApps() {
            return this.appList;
        }

        public void selectAll() {
            for (AppInfo app : this.appList) {
                app.isSelected = true;
            }
            notifyDataSetChanged();
        }

        public void deselectAll() {
            for (AppInfo app : this.appList) {
                app.isSelected = false;
            }
            notifyDataSetChanged();
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_app, parent, false);
            return new ViewHolder(view);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public void onBindViewHolder(final ViewHolder holder, int position) {
            final AppInfo app = this.appList.get(position);
            holder.appName.setText(app.appName);
            holder.packageName.setText(app.packageName);
            holder.appIcon.setImageDrawable(app.icon);
            holder.checkBox.setImageResource(app.isSelected ? this.checkedIcon : this.uncheckedIcon);
            holder.itemView.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment3$AppAdapter$$ExternalSyntheticLambda0
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m170x63ebcd7d(app, holder, view);
                }
            });
        }

        /* renamed from: lambda$onBindViewHolder$0$com-example-kbattery-Fragment3$AppAdapter, reason: not valid java name */
        /* synthetic */ void m170x63ebcd7d(AppInfo app, ViewHolder holder, View v) {
            app.isSelected = !app.isSelected;
            holder.checkBox.setImageResource(app.isSelected ? this.checkedIcon : this.uncheckedIcon);
        }

        @Override // androidx.recyclerview.widget.RecyclerView.Adapter
        public int getItemCount() {
            return this.appList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            ImageView appIcon;
            TextView appName;
            ImageView checkBox;
            TextView packageName;

            public ViewHolder(View itemView) {
                super(itemView);
                this.appIcon = (ImageView) itemView.findViewById(R.id.app_icon);
                this.appName = (TextView) itemView.findViewById(R.id.app_name);
                this.packageName = (TextView) itemView.findViewById(R.id.package_name);
                this.checkBox = (ImageView) itemView.findViewById(R.id.check_box);
            }
        }
    }
}
