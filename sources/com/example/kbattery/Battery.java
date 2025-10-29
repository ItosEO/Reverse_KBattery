package com.example.kbattery;

import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;

/* loaded from: classes3.dex */
public class Battery {
    public static final int HEALTH_COLD = 6;
    public static final int HEALTH_DEAD = 3;
    public static final int HEALTH_GOOD = 1;
    public static final int HEALTH_OVERHEAT = 2;
    public static final int HEALTH_OVER_VOLTAGE = 4;
    public static final int HEALTH_UNKNOWN = 0;
    public static final int HEALTH_UNSPECIFIED_FAILURE = 5;
    public static final int STATUS_CHARGING = 1;
    public static final int STATUS_DISCHARGING = 2;
    public static final int STATUS_FULL = 4;
    public static final int STATUS_NOT_CHARGING = 3;
    public static final int STATUS_UNKNOWN = 0;
    private static final String TAG = "Battery";
    private int health;
    private boolean isCharging;
    private int level;
    private int scale;
    private int status;
    private String technology;
    private int temperature;
    private long timestamp;
    private int voltage;

    public Battery() {
        this.timestamp = System.currentTimeMillis();
    }

    public Battery(int level, int scale, boolean isCharging) {
        this();
        setLevel(level);
        setScale(scale);
        setCharging(isCharging);
    }

    public int getPercentage() {
        if (this.scale <= 0 || this.level < 0) {
            return 0;
        }
        float percentage = (this.level / this.scale) * 100.0f;
        int result = Math.round(percentage);
        return Math.max(0, Math.min(100, result));
    }

    public float getExactPercentage() {
        if (this.scale <= 0) {
            return 0.0f;
        }
        return Math.max(0.0f, Math.min(100.0f, (this.level / this.scale) * 100.0f));
    }

    public int getLevel() {
        return this.level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
        this.timestamp = System.currentTimeMillis();
    }

    public int getScale() {
        return this.scale;
    }

    public void setScale(int scale) {
        this.scale = Math.max(1, scale);
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isCharging() {
        return this.isCharging;
    }

    public void setCharging(boolean charging) {
        this.isCharging = charging;
        this.timestamp = System.currentTimeMillis();
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
        boolean z = true;
        if (status != 1 && status != 4) {
            z = false;
        }
        this.isCharging = z;
        this.timestamp = System.currentTimeMillis();
    }

    public int getHealth() {
        return this.health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    public int getVoltage() {
        return this.voltage;
    }

    public void setVoltage(int voltage) {
        this.voltage = Math.max(0, voltage);
    }

    public int getTemperature() {
        return this.temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public float getTemperatureCelsius() {
        return this.temperature / 10.0f;
    }

    public String getTechnology() {
        return this.technology != null ? this.technology : "Unknown";
    }

    public void setTechnology(String technology) {
        this.technology = technology;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public boolean isFull() {
        return getPercentage() >= 100 || this.status == 4;
    }

    public boolean isLow() {
        return getPercentage() <= 15;
    }

    public boolean isCritical() {
        return getPercentage() <= 5;
    }

    public boolean isValid() {
        return this.level >= 0 && this.scale > 0 && this.level <= this.scale && this.timestamp > 0;
    }

    public String getStatusDescription() {
        switch (this.status) {
            case 1:
                return "充电中";
            case 2:
                return "放电中";
            case 3:
                return "未充电";
            case 4:
                return "已充满";
            default:
                return this.isCharging ? "充电中" : "放电中";
        }
    }

    public String getHealthDescription() {
        switch (this.health) {
            case 1:
                return "良好";
            case 2:
                return "过热";
            case 3:
                return "损坏";
            case 4:
                return "电压过高";
            case 5:
                return "故障";
            case 6:
                return "过冷";
            default:
                return "未知";
        }
    }

    public String getVoltageString() {
        return String.format("%.2f V", Double.valueOf(this.voltage / 1000.0d));
    }

    public String getTemperatureString() {
        return String.format("%.1f°C", Float.valueOf(getTemperatureCelsius()));
    }

    public static Battery fromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }
        Battery battery = new Battery();
        try {
            int level = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", 100);
            battery.setLevel(level);
            battery.setScale(scale);
            int status = intent.getIntExtra(NotificationCompat.CATEGORY_STATUS, -1);
            battery.setStatus(status);
            int health = intent.getIntExtra("health", 0);
            battery.setHealth(health);
            int voltage = intent.getIntExtra("voltage", 0);
            battery.setVoltage(voltage);
            int temperature = intent.getIntExtra("temperature", 0);
            battery.setTemperature(temperature);
            String technology = intent.getStringExtra("technology");
            battery.setTechnology(technology);
        } catch (Exception e) {
            Log.e(TAG, "Error creating Battery from intent: " + e.getMessage());
        }
        return battery;
    }

    public Battery copy() {
        Battery copy = new Battery();
        copy.level = this.level;
        copy.scale = this.scale;
        copy.isCharging = this.isCharging;
        copy.status = this.status;
        copy.health = this.health;
        copy.voltage = this.voltage;
        copy.temperature = this.temperature;
        copy.technology = this.technology;
        copy.timestamp = this.timestamp;
        return copy;
    }

    public String toString() {
        return String.format("Battery{level=%d, scale=%d, percentage=%d%%, charging=%s, status=%s, health=%s, voltage=%s, temp=%s}", Integer.valueOf(this.level), Integer.valueOf(this.scale), Integer.valueOf(getPercentage()), Boolean.valueOf(this.isCharging), getStatusDescription(), getHealthDescription(), getVoltageString(), getTemperatureString());
    }

    public boolean equalsIgnoringTimestamp(Battery other) {
        if (other == null || this.level != other.level || this.scale != other.scale || this.isCharging != other.isCharging || this.status != other.status || this.health != other.health || this.voltage != other.voltage || this.temperature != other.temperature) {
            return false;
        }
        if (this.technology == null) {
            if (other.technology != null) {
                return false;
            }
        } else if (!this.technology.equals(other.technology)) {
            return false;
        }
        return true;
    }
}
