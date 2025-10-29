package com.example.kbattery;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/* loaded from: classes3.dex */
public class BatteryMonitorService extends Service {
    private static final String CHANNEL_ID = "battery_monitor_channel";
    private static final float MAX_POWER = 3000.0f;
    private static final int MAX_RECORDS = 30;
    private static final float MIN_POWER = 50.0f;
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "BatteryMonitorService";
    private static final long UPDATE_INTERVAL = 60000;
    private BatteryManager batteryManager;
    private BroadcastReceiver batteryReceiver;
    private SharedPreferences preferences;
    private PowerManager.WakeLock wakeLock;
    private int currentPercent = 0;
    private boolean isCharging = false;
    private long lastRecordTime = 0;
    private int lastRecordPercent = -1;
    private long dischargeStartTime = 0;
    private final List<Float> powerRecords = new ArrayList(30);
    private int batteryCapacity = 4000;

    @Override // android.app.Service
    public void onCreate() {
        try {
            super.onCreate();
            Log.d(TAG, "Service onCreate");
            this.batteryManager = (BatteryManager) getSystemService("batterymanager");
            this.preferences = getSharedPreferences("BatteryPrefs", 0);
            loadBatteryCapacity();
            acquireWakeLock();
            createNotificationChannel();
            Notification notification = createNotification();
            if (notification == null) {
                Log.e(TAG, "Failed to create notification, cannot start foreground service");
                stopSelf();
            } else {
                startForeground(1, notification);
                registerBatteryReceiver();
                requestBatteryOptimizationWhitelist();
                updateBatteryState();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in service onCreate: " + e.getMessage());
            e.printStackTrace();
            stopSelf();
        }
    }

    private void loadBatteryCapacity() {
        int detectedCapacity;
        this.batteryCapacity = this.preferences.getInt("batteryCapacity", 4000);
        try {
            if (this.batteryManager != null) {
                long capacity = this.batteryManager.getLongProperty(1);
                if (capacity > 0 && (detectedCapacity = (int) (capacity / 1000)) > 0 && detectedCapacity < 20000) {
                    this.batteryCapacity = detectedCapacity;
                    this.preferences.edit().putInt("batteryCapacity", detectedCapacity).apply();
                    Log.d(TAG, "Loaded battery capacity from system: " + detectedCapacity + "mAh");
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Failed to get battery capacity from system: " + e.getMessage());
        }
    }

    private void acquireWakeLock() {
        try {
            PowerManager pm = (PowerManager) getSystemService("power");
            if (pm != null) {
                this.wakeLock = pm.newWakeLock(1, "BatteryMonitorService:WakeLock");
                this.wakeLock.acquire(600000L);
                Log.d(TAG, "WakeLock acquired");
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to acquire WakeLock: " + e.getMessage());
        }
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "电池监控服务", 2);
        channel.setDescription("用于持续监控电池状态和功率消耗");
        channel.setSound(null, null);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    private Notification createNotification() {
        String powerInfo;
        String str = "充电";
        try {
            Intent notificationIntent = new Intent(this, (Class<?>) BatteryActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 201326592);
            String chargingStatus = this.isCharging ? "充电" : "放电";
            if (this.isCharging) {
                powerInfo = "-";
            } else {
                powerInfo = this.powerRecords.isEmpty() ? "计算中..." : String.format("%.1f mAh/小时", Float.valueOf(calculateAveragePower()));
            }
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(R.drawable.ic_battery_24).setContentTitle("KBattery 电池监控").setContentText("电池状态: " + chargingStatus + " | 功耗: " + powerInfo).setPriority(1).setContentIntent(pendingIntent).setOngoing(true).setOnlyAlertOnce(true).setShowWhen(true).setVisibility(1);
            builder.setColor(-16738680);
            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.setBigContentTitle("KBattery 电池监控").bigText("当前电量: " + this.currentPercent + "%\n电池状态: " + chargingStatus + "\n当前功耗: " + powerInfo + "\n电池容量: " + this.batteryCapacity + " mAh\n更新时间: " + getCurrentTimeString()).setSummaryText("点击查看详细信息");
            builder.setStyle(bigTextStyle);
            builder.setAutoCancel(false);
            return builder.build();
        } catch (Exception e) {
            Log.e(TAG, "Failed to create notification: " + e.getMessage());
            try {
                NotificationCompat.Builder contentTitle = new NotificationCompat.Builder(this, CHANNEL_ID).setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle("KBattery 电池监控");
                StringBuilder sbAppend = new StringBuilder().append("电池状态: ");
                if (!this.isCharging) {
                    str = "放电";
                }
                NotificationCompat.Builder fallbackBuilder = contentTitle.setContentText(sbAppend.append(str).append(" | 电量: ").append(this.currentPercent).append("%").toString()).setPriority(1).setOngoing(true).setOnlyAlertOnce(true).setShowWhen(true).setAutoCancel(false);
                return fallbackBuilder.build();
            } catch (Exception fallbackE) {
                Log.e(TAG, "Failed to create fallback notification: " + fallbackE.getMessage());
                return null;
            }
        }
    }

    private String getCurrentTimeString() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }

    private float calculateAveragePower() {
        if (this.powerRecords.isEmpty()) {
            return 0.0f;
        }
        float sum = 0.0f;
        Iterator<Float> it = this.powerRecords.iterator();
        while (it.hasNext()) {
            float power = it.next().floatValue();
            sum += power;
        }
        return sum / this.powerRecords.size();
    }

    private void registerBatteryReceiver() {
        this.batteryReceiver = new BroadcastReceiver() { // from class: com.example.kbattery.BatteryMonitorService.1
            @Override // android.content.BroadcastReceiver
            public void onReceive(Context context, Intent intent) {
                if ("android.intent.action.BATTERY_CHANGED".equals(intent.getAction())) {
                    BatteryMonitorService.this.handleBatteryChanged(intent);
                }
            }
        };
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.BATTERY_CHANGED");
        filter.addAction("android.intent.action.ACTION_POWER_CONNECTED");
        filter.addAction("android.intent.action.ACTION_POWER_DISCONNECTED");
        registerReceiver(this.batteryReceiver, filter);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void handleBatteryChanged(Intent intent) {
        if (intent == null) {
            return;
        }
        try {
            int level = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", 100);
            if (level >= 0 && scale > 0) {
                int newPercent = (int) ((level / scale) * 100.0f);
                int status = intent.getIntExtra(NotificationCompat.CATEGORY_STATUS, -1);
                boolean newCharging = status == 2 || status == 5;
                if (this.isCharging != newCharging) {
                    this.isCharging = newCharging;
                    if (newCharging) {
                        this.powerRecords.clear();
                        Log.d(TAG, "Charging started, cleared power records");
                    } else {
                        this.dischargeStartTime = System.currentTimeMillis();
                        this.lastRecordTime = System.currentTimeMillis();
                        this.lastRecordPercent = newPercent;
                        Log.d(TAG, "Discharging started");
                    }
                }
                if (!this.isCharging && this.lastRecordPercent != -1 && newPercent != this.lastRecordPercent) {
                    calculatePowerConsumption(newPercent);
                }
                if (this.lastRecordPercent == -1) {
                    this.lastRecordTime = System.currentTimeMillis();
                    this.lastRecordPercent = newPercent;
                }
                this.currentPercent = newPercent;
                updateNotification();
                saveState();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling battery changed: " + e.getMessage());
        }
    }

    private void calculatePowerConsumption(int newPercent) {
        long currentTime = System.currentTimeMillis();
        long timeDiff = currentTime - this.lastRecordTime;
        int percentDiff = this.lastRecordPercent - newPercent;
        if (timeDiff > 0 && percentDiff > 0) {
            float timeHours = timeDiff / 3600000.0f;
            float capacityUsed = (this.batteryCapacity * percentDiff) / 100.0f;
            float power = Math.max(50.0f, Math.min(MAX_POWER, capacityUsed / timeHours));
            if (this.powerRecords.size() >= 30) {
                this.powerRecords.remove(0);
            }
            this.powerRecords.add(Float.valueOf(power));
            Log.d(TAG, "Calculated power consumption: " + power + " mAh/h");
            this.lastRecordTime = currentTime;
            this.lastRecordPercent = newPercent;
        }
    }

    private void updateNotification() {
        try {
            NotificationManager notificationManager = (NotificationManager) getSystemService("notification");
            if (notificationManager == null) {
                Log.e(TAG, "Failed to get NotificationManager");
                return;
            }
            if (Build.VERSION.SDK_INT >= 33) {
                NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
                if (!managerCompat.areNotificationsEnabled()) {
                    Log.w(TAG, "Notification permission not granted, cannot update notification");
                    return;
                }
            }
            Notification notification = createNotification();
            if (notification == null) {
                Log.e(TAG, "Cannot update notification: notification is null");
                return;
            }
            try {
                notificationManager.notify(1, notification);
                Log.d(TAG, "Notification updated successfully");
            } catch (IllegalArgumentException e) {
                Log.e(TAG, "Invalid notification argument: " + e.getMessage());
            } catch (SecurityException e2) {
                Log.e(TAG, "Security exception when notifying: " + e2.getMessage());
            } catch (Exception e3) {
                Log.e(TAG, "Unexpected error updating notification: " + e3.getMessage());
            }
        } catch (Exception e4) {
            Log.e(TAG, "Failed to update notification: " + e4.getMessage());
        }
    }

    private void updateBatteryState() {
        Intent intent = registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (intent != null) {
            handleBatteryChanged(intent);
        }
    }

    private void saveState() {
        try {
            SharedPreferences.Editor editor = this.preferences.edit();
            editor.putInt("currentPercent", this.currentPercent);
            editor.putBoolean("isCharging", this.isCharging);
            editor.putLong("lastRecordTime", this.lastRecordTime);
            editor.putInt("lastRecordPercent", this.lastRecordPercent);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to save state: " + e.getMessage());
        }
    }

    private void requestBatteryOptimizationWhitelist() {
        this.preferences.edit().putBoolean("needsBatteryOptimizationWhitelist", true).apply();
        Log.d(TAG, "Marked for battery optimization whitelist request");
    }

    @Override // android.app.Service
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");
        try {
            Notification notification = createNotification();
            if (notification != null) {
                startForeground(1, notification);
                Log.d(TAG, "Foreground service ensured in onStartCommand");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error ensuring foreground service: " + e.getMessage());
        }
        return 1;
    }

    @Override // android.app.Service
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override // android.app.Service
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service onDestroy");
        if (this.wakeLock != null && this.wakeLock.isHeld()) {
            this.wakeLock.release();
        }
        if (this.batteryReceiver != null) {
            try {
                unregisterReceiver(this.batteryReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Error unregistering receiver: " + e.getMessage());
            }
        }
        saveState();
    }

    public static void startService(Context context) {
        Intent serviceIntent = new Intent(context, (Class<?>) BatteryMonitorService.class);
        context.startForegroundService(serviceIntent);
    }

    public static void stopService(Context context) {
        Intent serviceIntent = new Intent(context, (Class<?>) BatteryMonitorService.class);
        context.stopService(serviceIntent);
    }
}
