package com.example.kbattery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/* loaded from: classes3.dex */
public class BatteryActivity extends AppCompatActivity {
    private static final int MAX_BATTERY_CAPACITY = 20000;
    private static final float MAX_POWER = 3000.0f;
    private static final float MAX_PREDICT_HOURS = 168.0f;
    private static final int MAX_RECORDS = 30;
    private static final float MIN_POWER = 50.0f;
    private static final String PREFS_NAME = "BatteryPrefs";
    private static final int REQUEST_BATTERY_OPTIMIZATION = 1001;
    private static final String TAG = "BatteryActivity";
    private static final long UPDATE_INTERVAL = 30000;
    private TextView avgPowerText;
    private ImageView batteryIcon;
    private TextView batteryPercent;
    private ProgressBar batteryProgress;
    private View capacityCard;
    private TextView capacityText;
    private TextView fullRangeTime;
    private View powerCard;
    private TextView remainTime;
    private TextView remainingTime;
    private ImageView statusIcon;
    private TextView statusText;
    private TextView usedTime;
    private int currentPercent = 0;
    private boolean isCharging = false;
    private long lastRecordTime = 0;
    private int lastRecordPercent = -1;
    private long dischargeStartTime = 0;
    private final List<Float> powerRecords = new ArrayList(30);
    private int batteryCapacity = 4000;
    private long lastFullTimestamp = 0;
    private float manualPower = 0.0f;
    private boolean isFirstLaunch = true;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable updateTask = new Runnable() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda3
        @Override // java.lang.Runnable
        public final void run() {
            this.f$0.scheduleUpdate();
        }
    };
    private final BroadcastReceiver batteryReceiver = new AnonymousClass1();

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
        setContentView(R.layout.activity_battery);
        BatteryMonitorService.startService(this);
        checkBatteryOptimizationWhitelist();
        restoreState();
        this.batteryCapacity = getBatteryCapacity();
        initViews();
        if (this.isFirstLaunch || !getSharedPreferences(PREFS_NAME, 0).contains("capacity")) {
            showCapacityDialog(true);
            this.isFirstLaunch = false;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        filter.addAction("android.intent.action.BATTERY_LOW");
        filter.addAction("android.intent.action.BATTERY_OKAY");
        registerReceiver(this.batteryReceiver, filter);
        this.mainHandler.post(this.updateTask);
        Log.d(TAG, "BatteryActivity initialized with capacity: " + this.batteryCapacity + "mAh");
    }

    private void initViews() {
        try {
            this.batteryPercent = (TextView) findViewById(R.id.batteryPercentage);
            this.statusText = (TextView) findViewById(R.id.batteryStatus);
            this.usedTime = (TextView) findViewById(R.id.usedTime);
            this.remainTime = (TextView) findViewById(R.id.remainingTime);
            this.avgPowerText = (TextView) findViewById(R.id.powerValue);
            this.capacityText = (TextView) findViewById(R.id.batteryCapacity);
            this.remainingTime = (TextView) findViewById(R.id.remainingTime);
            this.fullRangeTime = (TextView) findViewById(R.id.fullRangeTime);
            this.batteryProgress = (ProgressBar) findViewById(R.id.batteryProgress);
            this.batteryIcon = (ImageView) findViewById(R.id.batteryIcon);
            this.statusIcon = (ImageView) findViewById(R.id.statusIcon);
            this.powerCard = findViewById(R.id.powerCard);
            this.capacityCard = findViewById(R.id.capacityCard);
            this.powerCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda5
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m79lambda$initViews$0$comexamplekbatteryBatteryActivity(view);
                }
            });
            this.capacityCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda6
                @Override // android.view.View.OnClickListener
                public final void onClick(View view) {
                    this.f$0.m80lambda$initViews$1$comexamplekbatteryBatteryActivity(view);
                }
            });
            this.capacityText.setText(this.batteryCapacity + " mAh");
            this.remainingTime.setText("0小时0分钟");
            this.fullRangeTime.setText("0小时0分钟");
            if (this.usedTime != null) {
                this.usedTime.setText("--");
            }
            if (this.avgPowerText != null) {
                this.avgPowerText.setText("--");
            }
            if (this.remainTime != null) {
                this.remainTime.setText("--");
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化视图失败: " + e.getMessage());
        }
    }

    /* renamed from: lambda$initViews$0$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m79lambda$initViews$0$comexamplekbatteryBatteryActivity(View v) {
        showPowerDialog();
    }

    /* renamed from: lambda$initViews$1$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m80lambda$initViews$1$comexamplekbatteryBatteryActivity(View v) {
        showCapacityDialog(false);
    }

    private void startEntranceAnimation() {
        View titleText = findViewById(R.id.titleText);
        View batteryStatusCard = findViewById(R.id.batteryStatusCard);
        View usageTimeCard = findViewById(R.id.usageTimeCard);
        View appUsageCard = findViewById(R.id.appUsageCard);
        View[] cards = {titleText, batteryStatusCard, usageTimeCard, appUsageCard};
        for (int i = 0; i < cards.length; i++) {
            View card = cards[i];
            if (card != null) {
                card.setAlpha(0.0f);
                card.setTranslationY(50.0f);
                card.animate().alpha(1.0f).translationY(0.0f).setStartDelay((i * 150) + 100).setDuration(500L).start();
            }
        }
    }

    /* renamed from: com.example.kbattery.BatteryActivity$1, reason: invalid class name */
    class AnonymousClass1 extends BroadcastReceiver {
        AnonymousClass1() {
        }

        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, final Intent intent) {
            final String action;
            if (intent != null && (action = intent.getAction()) != null) {
                BatteryActivity.this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.BatteryActivity$1$$ExternalSyntheticLambda0
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m85lambda$onReceive$0$comexamplekbatteryBatteryActivity$1(action, intent);
                    }
                });
            }
        }

        /* JADX WARN: Can't fix incorrect switch cases order, some code will duplicate */
        /* JADX WARN: Removed duplicated region for block: B:14:0x0026  */
        /* renamed from: lambda$onReceive$0$com-example-kbattery-BatteryActivity$1, reason: not valid java name */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        /* synthetic */ void m85lambda$onReceive$0$comexamplekbatteryBatteryActivity$1(java.lang.String r4, android.content.Intent r5) {
            /*
                r3 = this;
                int r0 = r4.hashCode()
                java.lang.String r1 = "android.intent.action.BATTERY_CHANGED"
                switch(r0) {
                    case -1886648615: goto L1c;
                    case -1538406691: goto L14;
                    case 1019184907: goto La;
                    default: goto L9;
                }
            L9:
                goto L26
            La:
                java.lang.String r0 = "android.intent.action.ACTION_POWER_CONNECTED"
                boolean r0 = r4.equals(r0)
                if (r0 == 0) goto L9
                r0 = 1
                goto L27
            L14:
                boolean r0 = r4.equals(r1)
                if (r0 == 0) goto L9
                r0 = 0
                goto L27
            L1c:
                java.lang.String r0 = "android.intent.action.ACTION_POWER_DISCONNECTED"
                boolean r0 = r4.equals(r0)
                if (r0 == 0) goto L9
                r0 = 2
                goto L27
            L26:
                r0 = -1
            L27:
                switch(r0) {
                    case 0: goto L3f;
                    case 1: goto L2b;
                    case 2: goto L2b;
                    default: goto L2a;
                }
            L2a:
                goto L45
            L2b:
                com.example.kbattery.BatteryActivity r0 = com.example.kbattery.BatteryActivity.this
                android.content.IntentFilter r2 = new android.content.IntentFilter
                r2.<init>(r1)
                r1 = 0
                android.content.Intent r0 = r0.registerReceiver(r1, r2)
                if (r0 == 0) goto L45
                com.example.kbattery.BatteryActivity r1 = com.example.kbattery.BatteryActivity.this
                com.example.kbattery.BatteryActivity.m78$$Nest$mupdateBatteryInfo(r1, r0)
                goto L45
            L3f:
                com.example.kbattery.BatteryActivity r0 = com.example.kbattery.BatteryActivity.this
                com.example.kbattery.BatteryActivity.m78$$Nest$mupdateBatteryInfo(r0, r5)
            L45:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.example.kbattery.BatteryActivity.AnonymousClass1.m85lambda$onReceive$0$comexamplekbatteryBatteryActivity$1(java.lang.String, android.content.Intent):void");
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBatteryInfo(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            int level = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", 100);
            if (level >= 0 && scale > 0) {
                int newPercent = (int) ((level / scale) * 100.0f);
                if (newPercent >= 0 && newPercent <= 100) {
                    int status = intent.getIntExtra(NotificationCompat.CATEGORY_STATUS, -1);
                    boolean newCharging = status == 2 || status == 5;
                    boolean isFull = status == 5;
                    if (isFull) {
                        this.lastFullTimestamp = System.currentTimeMillis();
                        Log.i(TAG, "Battery fully charged at: " + this.lastFullTimestamp);
                    }
                    if (this.isCharging != newCharging) {
                        Log.d(TAG, "Charging state changed: " + this.isCharging + " -> " + newCharging);
                        if (newCharging) {
                            this.powerRecords.clear();
                            this.manualPower = 0.0f;
                            Log.d(TAG, "Charging started, cleared power records");
                        } else {
                            this.dischargeStartTime = System.currentTimeMillis();
                            this.lastRecordTime = System.currentTimeMillis();
                            this.lastRecordPercent = newPercent;
                            Log.d(TAG, "Discharging started at " + this.dischargeStartTime);
                        }
                        this.isCharging = newCharging;
                    }
                    if (!this.isCharging && this.lastRecordPercent != -1) {
                        calculatePowerConsumption(newPercent);
                    }
                    if (this.lastRecordPercent == -1) {
                        this.lastRecordTime = System.currentTimeMillis();
                        this.lastRecordPercent = newPercent;
                    }
                    this.currentPercent = newPercent;
                    updateUI();
                    saveState();
                    return;
                }
                Log.w(TAG, "Invalid battery percentage: " + newPercent);
                return;
            }
            Log.w(TAG, "Invalid battery level data");
        } catch (Exception e) {
            Log.e(TAG, "Error updating battery info: " + e.getMessage());
        }
    }

    private void calculatePowerConsumption(int newPercent) {
        float power;
        long now = System.currentTimeMillis();
        long timeDiff = now - this.lastRecordTime;
        int percentDiff = this.lastRecordPercent - newPercent;
        if (percentDiff > 0 || timeDiff >= 60000) {
            float hoursPassed = timeDiff / 3600000.0f;
            if (hoursPassed > 0.001f) {
                if (percentDiff > 0) {
                    float consumed = (this.batteryCapacity * percentDiff) / 100.0f;
                    power = consumed / hoursPassed;
                    Log.d(TAG, String.format("Power calculated: %.1f mAh/h (%.1f%% in %.1fh)", Float.valueOf(power), Float.valueOf(percentDiff), Float.valueOf(hoursPassed)));
                } else {
                    power = estimateStandbyPower();
                    Log.d(TAG, "Using estimated standby power: " + power + " mAh/h");
                }
                this.powerRecords.add(Float.valueOf(filterPower(power)));
                while (this.powerRecords.size() > 30) {
                    this.powerRecords.remove(0);
                }
                this.lastRecordTime = now;
                this.lastRecordPercent = newPercent;
            }
        }
    }

    private float estimateStandbyPower() {
        if (!this.powerRecords.isEmpty()) {
            float sum = 0.0f;
            int count = Math.min(this.powerRecords.size(), 5);
            int i = this.powerRecords.size();
            while (true) {
                i--;
                if (i >= this.powerRecords.size() - count) {
                    sum += this.powerRecords.get(i).floatValue();
                } else {
                    return sum / count;
                }
            }
        } else {
            return 50.0f;
        }
    }

    private float filterPower(float power) {
        if (Float.isNaN(power) || Float.isInfinite(power)) {
            Log.w(TAG, "Invalid power value detected: " + power);
            return 50.0f;
        }
        return Math.max(50.0f, Math.min(MAX_POWER, power));
    }

    private float getAvgPower() {
        if (this.manualPower > 0.0f) {
            return this.manualPower;
        }
        if (this.powerRecords.isEmpty()) {
            return estimateStandbyPower();
        }
        float sum = 0.0f;
        float weightSum = 0.0f;
        for (int i = 0; i < this.powerRecords.size(); i++) {
            float weight = (i + 1) / this.powerRecords.size();
            sum += this.powerRecords.get(i).floatValue() * weight;
            weightSum += weight;
        }
        float avgPower = sum / weightSum;
        Log.d(TAG, "Average power: " + avgPower + " mAh/h (based on " + this.powerRecords.size() + " records)");
        return avgPower;
    }

    private void updateUI() {
        try {
            if (this.batteryPercent != null) {
                this.batteryPercent.setText(this.currentPercent + "%");
            }
            if (this.batteryProgress != null) {
                animateProgressBar(this.currentPercent);
            }
            updateBatteryIcon();
            if (this.isCharging) {
                if (this.statusText != null) {
                    this.statusText.setText("充电中");
                }
                if (this.statusIcon != null) {
                    try {
                        this.statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                    } catch (Exception e) {
                        Log.e(TAG, "Error setting charging icon: " + e.getMessage());
                        try {
                            this.statusIcon.setImageDrawable(null);
                        } catch (Exception e2) {
                        }
                    }
                }
                if (this.usedTime != null) {
                    this.usedTime.setText("--");
                }
                if (this.avgPowerText != null) {
                    this.avgPowerText.setText("--");
                }
                if (this.remainTime != null) {
                    this.remainTime.setText("--");
                }
                if (this.remainingTime != null) {
                    this.remainingTime.setText("--");
                }
                if (this.fullRangeTime != null) {
                    this.fullRangeTime.setText("--");
                }
            } else {
                if (this.statusText != null) {
                    this.statusText.setText("放电中");
                }
                if (this.statusIcon != null) {
                    try {
                        this.statusIcon.setImageResource(android.R.drawable.ic_dialog_info);
                    } catch (Exception e3) {
                        Log.e(TAG, "Error setting discharging icon: " + e3.getMessage());
                        try {
                            this.statusIcon.setImageDrawable(null);
                        } catch (Exception e4) {
                        }
                    }
                }
                if (this.usedTime != null) {
                    String usedTimeStr = formatTimeUsage(System.currentTimeMillis() - this.dischargeStartTime);
                    this.usedTime.setText(usedTimeStr);
                }
                float avgPower = getAvgPower();
                if (this.avgPowerText != null) {
                    this.avgPowerText.setText(String.format(Locale.getDefault(), "%.1f mAh/小时", Float.valueOf(avgPower)));
                }
                String remainingTimeStr = calculateRemainingTime(avgPower);
                if (this.remainTime != null) {
                    this.remainTime.setText(remainingTimeStr);
                }
                if (this.remainingTime != null) {
                    this.remainingTime.setText(remainingTimeStr);
                }
                String fullRangeTimeStr = calculateFullRangeTime(avgPower);
                if (this.fullRangeTime != null) {
                    this.fullRangeTime.setText(fullRangeTimeStr);
                }
            }
            if (this.capacityText != null) {
                this.capacityText.setText(this.batteryCapacity + " mAh");
            }
            updateLastFullChargeTime();
        } catch (Exception e5) {
            Log.e(TAG, "Error updating UI: " + e5.getMessage());
            e5.printStackTrace();
        }
    }

    private void animateProgressBar(final int progress) {
        if (this.batteryProgress != null) {
            try {
                ObjectAnimator progressAnimator = ObjectAnimator.ofInt(this.batteryProgress, "progress", progress);
                progressAnimator.setDuration(1000L);
                progressAnimator.addListener(new AnimatorListenerAdapter() { // from class: com.example.kbattery.BatteryActivity.2
                    @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                    public void onAnimationStart(Animator animation) {
                        if (BatteryActivity.this.batteryProgress != null) {
                            BatteryActivity.this.batteryProgress.setProgress(progress);
                        }
                    }
                });
                progressAnimator.start();
            } catch (Exception e) {
                Log.e(TAG, "Error animating progress bar: " + e.getMessage());
            }
        }
    }

    private String formatTimeUsage(long millis) {
        long hours = millis / 3600000;
        long minutes = (millis % 3600000) / 60000;
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%d小时%d分钟", Long.valueOf(hours), Long.valueOf(minutes));
        }
        return String.format(Locale.getDefault(), "%d分钟", Long.valueOf(minutes));
    }

    private String calculateRemainingTime(float avgPower) {
        if (avgPower > 0.0f) {
            try {
                if (!Float.isNaN(avgPower) && !Float.isInfinite(avgPower) && this.batteryCapacity > 0 && this.currentPercent >= 0 && this.currentPercent <= 100) {
                    float remainingCapacity = (this.batteryCapacity * this.currentPercent) / 100.0f;
                    float remainHours = remainingCapacity / avgPower;
                    if (!Float.isNaN(remainHours) && !Float.isInfinite(remainHours) && remainHours >= 0.0f) {
                        if (remainHours > MAX_PREDICT_HOURS) {
                            return "超过7天";
                        }
                        int remHours = (int) remainHours;
                        int remMins = (int) ((remainHours - remHours) * 60.0f);
                        if (remHours > 0) {
                            return String.format(Locale.getDefault(), "%d小时%d分钟", Integer.valueOf(remHours), Integer.valueOf(remMins));
                        }
                        return String.format(Locale.getDefault(), "%d分钟", Integer.valueOf(Math.max(1, remMins)));
                    }
                    return "计算中...";
                }
                return "计算中...";
            } catch (Exception e) {
                Log.e(TAG, "Error calculating remaining time: " + e.getMessage());
                return "计算中...";
            }
        }
        return "计算中...";
    }

    private String calculateFullRangeTime(float avgPower) {
        if (avgPower > 0.0f) {
            try {
                if (Float.isNaN(avgPower) || Float.isInfinite(avgPower) || this.batteryCapacity <= 0) {
                    return "计算中...";
                }
                float fullCapacity = this.batteryCapacity;
                float fullRangeHours = fullCapacity / avgPower;
                if (!Float.isNaN(fullRangeHours) && !Float.isInfinite(fullRangeHours) && fullRangeHours >= 0.0f) {
                    if (fullRangeHours > MAX_PREDICT_HOURS) {
                        return "超过7天";
                    }
                    int fullHours = (int) fullRangeHours;
                    int fullMins = (int) ((fullRangeHours - fullHours) * 60.0f);
                    if (fullHours > 0) {
                        return String.format(Locale.getDefault(), "%d小时%d分钟", Integer.valueOf(fullHours), Integer.valueOf(fullMins));
                    }
                    return String.format(Locale.getDefault(), "%d分钟", Integer.valueOf(Math.max(1, fullMins)));
                }
                return "计算中...";
            } catch (Exception e) {
                Log.e(TAG, "Error calculating full range time: " + e.getMessage());
                return "计算中...";
            }
        }
        return "计算中...";
    }

    private void updateBatteryIcon() {
        int iconResId;
        if (this.batteryIcon != null) {
            try {
                if (this.isCharging) {
                    if (this.currentPercent >= 90) {
                        iconResId = R.drawable.battery_charging_full;
                    } else {
                        int iconResId2 = this.currentPercent;
                        if (iconResId2 >= 70) {
                            iconResId = R.drawable.battery_charging_70;
                        } else {
                            int iconResId3 = this.currentPercent;
                            if (iconResId3 >= 50) {
                                iconResId = R.drawable.battery_charging_50;
                            } else {
                                int iconResId4 = this.currentPercent;
                                if (iconResId4 >= 30) {
                                    iconResId = R.drawable.battery_charging_30;
                                } else {
                                    iconResId = R.drawable.battery_charging_low;
                                }
                            }
                        }
                    }
                } else {
                    int iconResId5 = this.currentPercent;
                    if (iconResId5 >= 90) {
                        iconResId = R.drawable.battery_full;
                    } else {
                        int iconResId6 = this.currentPercent;
                        if (iconResId6 >= 70) {
                            iconResId = R.drawable.battery_70;
                        } else {
                            int iconResId7 = this.currentPercent;
                            if (iconResId7 >= 50) {
                                iconResId = R.drawable.battery_50;
                            } else {
                                int iconResId8 = this.currentPercent;
                                if (iconResId8 >= 30) {
                                    iconResId = R.drawable.battery_30;
                                } else {
                                    iconResId = R.drawable.battery_low;
                                }
                            }
                        }
                    }
                }
                if (iconResId > 0) {
                    this.batteryIcon.setImageResource(iconResId);
                    this.batteryIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
                    Log.d(TAG, "Battery icon updated: " + getResources().getResourceEntryName(iconResId) + ", percent: " + this.currentPercent);
                    return;
                }
                this.batteryIcon.setImageResource(android.R.drawable.ic_dialog_info);
            } catch (Exception e) {
                Log.e(TAG, "Error updating battery icon: " + e.getMessage());
                try {
                    this.batteryIcon.setImageDrawable(null);
                } catch (Exception ex) {
                    Log.e(TAG, "Failed to clear battery icon: " + ex.getMessage());
                }
            }
        }
    }

    private void updateLastFullChargeTime() {
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleUpdate() {
        try {
            try {
                Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
                if (intent != null) {
                    updateBatteryInfo(intent);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error in scheduled update: " + e.getMessage());
            }
        } finally {
            this.mainHandler.postDelayed(this.updateTask, 30000L);
        }
    }

    private int getBatteryCapacity() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            int savedCapacity = prefs.getInt("capacity", 0);
            if (savedCapacity > 0 && savedCapacity <= 20000) {
                return savedCapacity;
            }
            try {
                BatteryManager bm = (BatteryManager) getSystemService("batterymanager");
                if (bm != null) {
                    bm.getLongProperty(4);
                }
            } catch (Exception e) {
                Log.w(TAG, "BatteryManager capacity detection failed: " + e.getMessage());
            }
            Log.d(TAG, "Using default battery capacity: 4000mAh");
            return 4000;
        } catch (Exception e2) {
            Log.e(TAG, "Error in getBatteryCapacity: " + e2.getMessage());
            return 4000;
        }
    }

    private void showPowerDialog() {
        final EditText input = new EditText(this);
        input.setInputType(8194);
        input.setText(String.valueOf(getAvgPower()));
        input.setPadding(64, 64, 64, 64);
        new MaterialAlertDialogBuilder(this).setTitle((CharSequence) "调整平均功耗").setMessage((CharSequence) String.format(Locale.getDefault(), "请输入当前功耗值（%.0f-%.0f mAh/小时）", Float.valueOf(50.0f), Float.valueOf(MAX_POWER))).setView((View) input).setPositiveButton((CharSequence) "确认", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda1
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) throws NumberFormatException {
                this.f$0.m83lambda$showPowerDialog$2$comexamplekbatteryBatteryActivity(input, dialogInterface, i);
            }
        }).setNegativeButton((CharSequence) "取消", (DialogInterface.OnClickListener) null).setNeutralButton((CharSequence) "重置", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda2
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                this.f$0.m84lambda$showPowerDialog$3$comexamplekbatteryBatteryActivity(dialogInterface, i);
            }
        }).show();
    }

    /* renamed from: lambda$showPowerDialog$2$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m83lambda$showPowerDialog$2$comexamplekbatteryBatteryActivity(EditText input, DialogInterface dialog, int which) throws NumberFormatException {
        try {
            String inputStr = input.getText().toString().trim();
            if (!inputStr.isEmpty()) {
                float power = Float.parseFloat(inputStr);
                if (power >= 50.0f && power <= MAX_POWER) {
                    this.manualPower = power;
                    this.powerRecords.clear();
                    updateUI();
                    saveState();
                    Log.d(TAG, "Manual power set to: " + power + " mAh/h");
                }
            }
        } catch (NumberFormatException e) {
            Log.w(TAG, "Invalid power input: " + ((Object) input.getText()));
        }
    }

    /* renamed from: lambda$showPowerDialog$3$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m84lambda$showPowerDialog$3$comexamplekbatteryBatteryActivity(DialogInterface dialog, int which) {
        this.manualPower = 0.0f;
        this.powerRecords.clear();
        updateUI();
        saveState();
        Log.d(TAG, "Power settings reset");
    }

    private void showCapacityDialog(final boolean force) {
        final EditText input = new EditText(this);
        input.setInputType(2);
        input.setText(String.valueOf(this.batteryCapacity));
        input.setPadding(64, 64, 64, 64);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(this).setTitle((CharSequence) "电池容量设置").setMessage((CharSequence) String.format(Locale.getDefault(), "请输入电池容量（500-%d mAh）", 20000)).setView((View) input).setPositiveButton((CharSequence) "确认", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda0
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) throws NumberFormatException {
                this.f$0.m82lambda$showCapacityDialog$4$comexamplekbatteryBatteryActivity(input, force, dialogInterface, i);
            }
        });
        if (!force) {
            builder.setNegativeButton((CharSequence) "取消", (DialogInterface.OnClickListener) null);
        } else {
            builder.setCancelable(false);
        }
        builder.show();
    }

    /* renamed from: lambda$showCapacityDialog$4$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m82lambda$showCapacityDialog$4$comexamplekbatteryBatteryActivity(EditText input, boolean force, DialogInterface dialog, int which) throws NumberFormatException {
        try {
            String inputStr = input.getText().toString().trim();
            if (!inputStr.isEmpty()) {
                int capacity = Integer.parseInt(inputStr);
                if (capacity >= 500 && capacity <= 20000) {
                    this.batteryCapacity = capacity;
                    this.capacityText.setText(capacity + " mAh");
                    this.powerRecords.clear();
                    saveState();
                    Log.d(TAG, "Battery capacity set to: " + capacity + "mAh");
                } else if (force) {
                    showCapacityDialog(true);
                }
            } else if (force) {
                showCapacityDialog(true);
            }
        } catch (NumberFormatException e) {
            if (force) {
                showCapacityDialog(true);
            }
        }
    }

    private void saveState() {
        try {
            SharedPreferences.Editor editor = getSharedPreferences(PREFS_NAME, 0).edit();
            editor.putLong("lastRecordTime", this.lastRecordTime);
            editor.putInt("lastRecordPercent", this.lastRecordPercent);
            editor.putLong("dischargeStart", this.dischargeStartTime);
            editor.putBoolean("isCharging", this.isCharging);
            editor.putInt("capacity", this.batteryCapacity);
            editor.putLong("lastFull", this.lastFullTimestamp);
            editor.putFloat("manualPower", this.manualPower);
            editor.putBoolean("firstLaunch", false);
            editor.putInt("powerCount", this.powerRecords.size());
            for (int i = 0; i < this.powerRecords.size(); i++) {
                editor.putFloat("power_" + i, this.powerRecords.get(i).floatValue());
            }
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Error saving state: " + e.getMessage());
        }
    }

    private void checkBatteryOptimizationWhitelist() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
        boolean needsRequest = prefs.getBoolean("needsBatteryOptimizationWhitelist", true);
        if (needsRequest) {
            PowerManager powerManager = (PowerManager) getSystemService("power");
            if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent();
                intent.setAction("android.settings.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS");
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, 1001);
                return;
            }
            prefs.edit().putBoolean("needsBatteryOptimizationWhitelist", false).apply();
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, androidx.activity.ComponentActivity, android.app.Activity
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            PowerManager powerManager = (PowerManager) getSystemService("power");
            if (powerManager != null && powerManager.isIgnoringBatteryOptimizations(getPackageName())) {
                prefs.edit().putBoolean("needsBatteryOptimizationWhitelist", false).apply();
                Log.d(TAG, "App added to battery optimization whitelist");
            }
        }
    }

    private void restoreState() {
        try {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, 0);
            this.lastRecordTime = prefs.getLong("lastRecordTime", System.currentTimeMillis());
            this.lastRecordPercent = prefs.getInt("lastRecordPercent", -1);
            this.dischargeStartTime = prefs.getLong("dischargeStart", System.currentTimeMillis());
            this.isCharging = prefs.getBoolean("isCharging", false);
            this.batteryCapacity = prefs.getInt("capacity", getBatteryCapacity());
            this.lastFullTimestamp = prefs.getLong("lastFull", 0L);
            this.manualPower = prefs.getFloat("manualPower", 0.0f);
            this.isFirstLaunch = prefs.getBoolean("firstLaunch", true);
            this.powerRecords.clear();
            int count = prefs.getInt("powerCount", 0);
            for (int i = 0; i < count; i++) {
                float power = prefs.getFloat("power_" + i, 0.0f);
                if (power > 0.0f) {
                    this.powerRecords.add(Float.valueOf(power));
                }
            }
            Log.d(TAG, "State restored: " + this.powerRecords.size() + " power records, capacity: " + this.batteryCapacity + "mAh");
        } catch (Exception e) {
            Log.e(TAG, "Error restoring state: " + e.getMessage());
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onResume() {
        super.onResume();
        this.mainHandler.post(new Runnable() { // from class: com.example.kbattery.BatteryActivity$$ExternalSyntheticLambda4
            @Override // java.lang.Runnable
            public final void run() {
                this.f$0.m81lambda$onResume$5$comexamplekbatteryBatteryActivity();
            }
        });
    }

    /* renamed from: lambda$onResume$5$com-example-kbattery-BatteryActivity, reason: not valid java name */
    /* synthetic */ void m81lambda$onResume$5$comexamplekbatteryBatteryActivity() {
        Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (intent != null) {
            updateBatteryInfo(intent);
        }
    }

    @Override // androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onPause() {
        super.onPause();
        saveState();
    }

    @Override // androidx.appcompat.app.AppCompatActivity, androidx.fragment.app.FragmentActivity, android.app.Activity
    protected void onDestroy() {
        super.onDestroy();
        try {
            unregisterReceiver(this.batteryReceiver);
            this.mainHandler.removeCallbacks(this.updateTask);
            saveState();
        } catch (Exception e) {
            Log.e(TAG, "Error during destruction: " + e.getMessage());
        }
    }
}
