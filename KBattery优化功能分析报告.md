# KBattery 优化功能逆向分析报告

> **版本**: V1.18.2  
> **包名**: com.example.kbattery  
> **目标SDK**: Android 35 (Android 15)  
> **最低SDK**: Android 30  
> **特殊权限**: 需要Shizuku服务支持

---

## 目录

- [1. 项目架构概览](#1-项目架构概览)
- [2. 模式优化功能（ModeActivity）](#2-模式优化功能modeactivity)
- [3. 高级优化功能（Fragment3）](#3-高级优化功能fragment3)
- [4. 电池监控功能（Fragment1 & BatteryMonitorService）](#4-电池监控功能fragment1--batterymonitorservice)
- [5. 关于页面（Fragment2）](#5-关于页面fragment2)
- [6. 技术实现细节](#6-技术实现细节)
- [7. 总结](#7-总结)

---

## 1. 项目架构概览

### 1.1 整体结构

该应用采用 **单Activity多Fragment** 架构：

```
MainActivity (主入口)
├── Fragment1 (电池状态监控)
├── Fragment2 (关于页面)
└── Fragment3 (高级优化设置)

+ ModeActivity (模式切换)
+ BatteryActivity (续航预测)
+ AuthorActivity (作者信息)
+ BatteryMonitorService (后台监控服务)
```

### 1.2 核心依赖

- **Shizuku**: 用于执行需要root权限的系统命令
- **Material Design Components**: UI框架
- **AndroidX WorkManager**: 后台任务管理
- **Gson**: 数据序列化
- **OkHttp**: 网络请求（检查更新）

### 1.3 设备兼容性检测

**支持的设备制造商**：
- Vivo/iQoo
- Oppo/OnePlus/Realme (ColorOS系列)
- Xiaomi/Redmi (HyperOS系列)

```java
// 设备检测代码片段
String manufacturer = Build.MANUFACTURER.toLowerCase();
boolean isVivoDevice = manufacturer.contains("vivo") || manufacturer.contains("iqoo");
boolean isOppoRelated = manufacturer.contains("oppo") || manufacturer.contains("oneplus");
boolean isXiaomiRelated = manufacturer.contains("xiaomi") || manufacturer.contains("redmi");
```

---

## 2. 模式优化功能（ModeActivity）

### 2.1 功能概述

`ModeActivity` 提供4种系统优化模式，通过Shizuku执行shell脚本修改系统设置。

### 2.2 模式1：省电模式

```bash
echo 续航脚本3.0启用中...
echo "防冲突进行中..."
settings delete system power_sleep_mode_enabled
settings delete global adaptive_battery_management_enabled
settings delete global app_standby_enabled
settings delete system light_effect_power_saving_strategy_switch
settings delete global touch_power_saving_mode
settings delete secure basic_sleep_enabled
settings delete system soft_wakeup_enhance_enabled
settings delete system pem_network_policy_start
settings delete global standby_battery_saving_enabled
settings delete global standby_power_saving_enabled
settings delete secure enable_bg_activity_feature
settings delete secure pem_in_sleepmode
settings delete system call_log
settings delete global activity_starts_logging_enabled
settings delete global ecg_disable_logging
settings delete global battery_saver_constants >/dev/null 2>&1
settings delete global device_idle_constants >/dev/null 2>&1
device_config delete activity_manager_native_boot low_power_mode

echo 开启睡眠模式1
settings put system power_sleep_mode_enabled 1

echo 开启自适应电池管理
settings put global adaptive_battery_management_enabled 1

echo 开启应用待机优化
settings put global app_standby_enabled 1

echo 开启灯光省电策略
settings put system light_effect_power_saving_strategy_switch 1

echo 开启触摸省电
settings put global touch_power_saving_mode 1

echo 开启睡眠模式
settings put secure basic_sleep_enabled 1

echo 关闭软唤醒增强
settings put system soft_wakeup_enhance_enabled 0

echo 开启PEM网络策略
settings put system pem_network_policy_start 1

echo 开启待机电池节省
settings put global standby_battery_saving_enabled 1

echo 开启待机功耗节省
settings put global standby_power_saving_enabled 1

echo 开启后台活动优化
settings put secure enable_bg_activity_feature 1

echo 进入深度睡眠模式
settings put secure pem_in_sleepmode 1

echo 关闭系统日志
settings put system call_log 0
settings put global activity_starts_logging_enabled 0
settings put global ecg_disable_logging 1

echo doze 模式
settings put global battery_saver_constants datasayer_disabled=false,disable_animation=true,vibration_disabled=true,disable_vibration=true,force_all_apps_standby=true,enable_quick_doze=true,animation_disabled=true,enable_datasayer=true,quick_doze_enabled=true,force_background_check=true,advertise_is_enabled >/dev/null 2>&1

echo doze 配置
settings put global device_idle_constants inactive_to=1800000,motion_inactive_to=900000,light_after_inactive_to=150000,light_pre_idle_to=300000,light_max_idle_to=432000000,light_idle_to=360000000,light_idle_factor=8,light_idle_maintenance_max_budget=500,light_idle_maintenance_min_budget=200,min_light_maintenance_time=500,min_time_to_alarm=300000 >/dev/null 2>&1

echo 激活活动省电
device_config put activity_manager_native_boot low_power_mode true

echo 启用活动管理器冻结功能
device_config put activity_manager_native_boot use_freezer true

echo 完毕
```

评估: ✅ 真实优化 - 基于Android原生Doze机制

### 2.3 模式2：性能模式

```bash
echo 续航脚本3.0启用中...
echo "防冲突进行中..."
settings delete system power_sleep_mode_enabled
settings delete global adaptive_battery_management_enabled
settings delete global app_standby_enabled
settings delete system light_effect_power_saving_strategy_switch
settings delete global touch_power_saving_mode
settings delete secure basic_sleep_enabled
settings delete system soft_wakeup_enhance_enabled
settings delete system pem_network_policy_start
settings delete global standby_battery_saving_enabled
settings delete global standby_power_saving_enabled
settings delete secure enable_bg_activity_feature
settings delete secure pem_in_sleepmode
settings delete system call_log
settings delete global activity_starts_logging_enabled
settings delete global ecg_disable_logging
settings delete global battery_saver_constants >/dev/null 2>&1
settings delete global device_idle_constants >/dev/null 2>&1

settings put system is_game_mode 1

echo 强制开启 GPU 加速
settings put system game_enable_gpu_turbo 1

echo 强制开启 CPU 加速
settings put system game_enable_cpu_turbo 1

echo 允许 60FPS 高帧率模式
settings put system game_enable_60_fps 1

echo 性能增强
settings put system game_performance_lingfan_model 1

echo 开启系统级优化
settings put system game_performance_model_mysterious_bonus 1

echo 强制启用动态分辨率
settings put system game_dynamic_resolution 1

echo 开启MEMC 运动补偿
settings put system gamecube_frame_interpolation 1

echo 启用 AI 情景优化
settings put secure vivo_gamecube_ai_situation_stage 1

echo 关闭充电限制
settings put secure battery_charge_director_game_cube 0

echo 开启 60FPS+ 高帧率模式
settings put secure game_enable_high_fps 1

echo 完毕
```

评估: ⚠️ Vivo设备专属功能

### 2.4 模式3：日常模式

```bash
echo 防冲突进行中...
echo 续航脚本3.0启用中...
settings delete system power_sleep_mode_enabled
settings delete global adaptive_battery_management_enabled
settings delete global app_standby_enabled
settings delete system light_effect_power_saving_strategy_switch
settings delete global touch_power_saving_mode
settings delete secure basic_sleep_enabled
settings delete system soft_wakeup_enhance_enabled
settings delete system pem_network_policy_start
settings delete global standby_battery_saving_enabled
settings delete global standby_power_saving_enabled
settings delete secure enable_bg_activity_feature
settings delete secure pem_in_sleepmode
settings delete system call_log
settings delete global activity_starts_logging_enabled
settings delete global ecg_disable_logging
settings delete global battery_saver_constants >/dev/null 2>&1
settings delete global device_idle_constants >/dev/null 2>&1

sleep 2

echo 原生均衡模式配置
settings put secure power_save_type 1
settings put system power_save_type 1
settings put secure system_property_power_mode_type 1
settings put system powermonitor 1
settings put system pempsmstate 1
settings put system haptic_feedback_enabled 1
settings put system unlock_enter_launcher_animation 3
settings put system screen_off_remind 1
settings put system aod_tp_support_switch 1
settings put system nightpearl_is_showing 1
settings put secure enter_exit_app_realtime_blur_state 1
settings put system motion_blur_state 1
settings put system enhanced_dynamic_effects 1
settings put secure launcher_widget_support_blur 1

echo 临时关闭LTPO
settings put system vivo_ltpo_setting 0
device_config put core_graphics com.android.graphics.surfaceflinger.flags.vrr_config false

echo 硬件加速提速
setprop debug.egl.hw 1
setprop debug.sf.hw 1

echo 完毕
```

评估: ⚠️ 部分Vivo专属配置

### 2.5 模式4：默认模式（还原）

```bash
echo 配置还原脚本启动中...
echo 正在清除省电/性能/日常模式残留配置...

# 清除省电模式相关设置
settings delete system power_sleep_mode_enabled
settings delete global adaptive_battery_management_enabled
settings delete global app_standby_enabled
settings delete system light_effect_power_saving_strategy_switch
settings delete global touch_power_saving_mode
settings delete secure basic_sleep_enabled
settings delete system soft_wakeup_enhance_enabled
settings delete system pem_network_policy_start
settings delete global standby_battery_saving_enabled
settings delete global standby_power_saving_enabled
settings delete secure enable_bg_activity_feature
settings delete secure pem_in_sleepmode
settings delete system call_log
settings delete global activity_starts_logging_enabled
settings delete global ecg_disable_logging
settings delete global battery_saver_constants >/dev/null 2>&1
settings delete global device_idle_constants >/dev/null 2>&1
device_config delete activity_manager_native_boot low_power_mode
device_config delete activity_manager_native_boot use_freezer

# 清除性能模式相关设置
settings delete system is_game_mode
settings delete system game_enable_gpu_turbo
settings delete system game_enable_cpu_turbo
settings delete system game_enable_60_fps
settings delete system game_performance_lingfan_model
settings delete system game_performance_model_mysterious_bonus
settings delete system game_dynamic_resolution
settings delete system gamecube_frame_interpolation
settings delete secure vivo_gamecube_ai_situation_stage
settings delete secure battery_charge_director_game_cube
settings delete secure game_enable_high_fps

# 清除日常模式相关设置
settings delete secure power_save_type
settings delete system power_save_type
settings delete secure system_property_power_mode_type
settings delete system powermonitor
settings delete system pempsmstate
settings delete system haptic_feedback_enabled
settings delete system unlock_enter_launcher_animation
settings delete system screen_off_remind
settings delete system aod_tp_support_switch
settings delete system nightpearl_is_showing
settings delete secure enter_exit_app_realtime_blur_state
settings delete system motion_blur_state
settings delete system enhanced_dynamic_effects
settings delete secure launcher_widget_support_blur
settings delete system vivo_ltpo_setting
device_config delete core_graphics com.android.graphics.surfaceflinger.flags.vrr_config

sleep 1
echo 还原完毕
```

评估: ✅ 功能完整

---

## 3. 高级优化功能（Fragment3）

### 3.1 功能概述

`Fragment3` 是整个应用的**核心优化模块**，包含大量细粒度优化开关。

**UI布局**: `fragment_3.xml` (1037行代码)

**功能分类**：
1. 实用功能（墓碑/充电/网络）
2. 显示设置（原彩/渲染/AI）
3. 游戏设置（光追/插帧/抗闪烁）
4. A+调度设置
5. ColorOS专区
6. HyperOS专区
7. 应用增强设置

### 3.2 实用功能

#### 3.2.1 墓碑机制

**开关ID**: `switch_tombstone`

**启用命令**:
```bash
echo 墓碑已开启
settings put global cached_apps_freezer enabled
```

**关闭命令**:
```bash
echo 墓碑已关闭
settings put global cached_apps_freezer disabled
```

**原理**: Android 11引入的Cached Apps Freezer机制，冻结后台应用进程以节省内存和电量

**评估**: ✅ **真实优化** - 原生Android功能，可有效降低后台功耗

---

#### 3.2.2 充电加速

**开关ID**: `switch_refresh_rate`

**启用命令**:
```bash
echo 充电加速已开启
settings put secure vivo_keyguard_super_speed_charge_switch 1
settings put system balance_charge_switch 1
settings put system vivo_keyguard_show_charge_mode_switch 1
settings put system default_charge_acceleration 1
```

**评估**: ⚠️ **厂商专属** - 仅Vivo设备支持，其他设备无效

---

#### 3.2.3 WLAN提升

**开关ID**: `switch_wlan_optimize`

**启用命令**:
```bash
echo 已开启WLAN优化
settings put secure vivo_career_mode_order_assistance_toggle_state 1
settings put system is_network_enhancement_enable 1
settings put global pem_5g_policy_enable 1
settings put system pem_network_policy_start 1
```

**评估**: ⚠️ **厂商专属** - Vivo职业模式网络加速

---

### 3.3 显示设置

#### 3.3.1 原彩显示

**开关ID**: `switch_true_tone`

**启用命令**:
```bash
echo 原彩显示开启
settings put system vivo_auto_eye_cct_enable 1
```

**评估**: ⚠️ **厂商专属** - Vivo屏幕色温自适应

---

#### 3.3.2 Vulkan渲染

**开关ID**: `switch_memory_recycle`

**启用命令**:
```bash
echo vulkan已切换
device_config put core_graphics com.android.graphics.surfaceflinger.flags.vulkan_renderengine true
setprop debug.hwui.renderer skiavk
setprop debug.renderengine.backend skiavk
```

**关闭命令**:
```bash
echo vulkan已关闭
device_config put core_graphics com.android.graphics.surfaceflinger.flags.vulkan_renderengine false
setprop debug.hwui.renderer opengl
setprop debug.renderengine.backend opengl
```

**原理**: 将Android渲染引擎从OpenGL切换到Vulkan API

**评估**: ✅ **真实优化** - Vulkan理论上可降低功耗并提升性能，但需设备GPU支持

---

#### 3.3.3 AI策略

**开关ID**: `switch_ai_strategy`

**命令**:
```bash
echo AI策略已开启 / echo AI策略已关闭
```

**评估**: ❌ **空壳功能** - 仅输出echo，无实际命令

---

### 3.4 游戏设置

#### 3.4.1 光线追踪

**开关ID**: `switch_light_tracing`

**命令**:
```bash
echo 光线追踪已开启 / echo 光线追踪已关闭
```

**评估**: ❌ **空壳功能** - 仅输出echo，无实际命令

---

#### 3.4.2 全局插帧

**开关ID**: `switch_pin_function`

**启用命令**:
```bash
echo 全局插针开启
settings put system gamecube_frame_interpolation 1:1:0:48:144
```

**关闭命令**:
```bash
echo 全局插针已关闭
settings put system gamecube_frame_interpolation 0:-1:0:0:0
```

**配套功能**: NEON帧差计算优化（伪装成技术亮点）

```java
private String executeNeonOptimization() {
    byte[] prevFrame = new byte[64];
    byte[] currFrame = new byte[64];
    // 生成随机帧数据
    for (int i = 0; i < 64; i++) {
        prevFrame[i] = (byte) random.nextInt(256);
        currFrame[i] = (byte) Math.min(255, (prevFrame[i] + random.nextInt(10)) - 5);
    }
    // 计算帧差异
    int difference = calculateFrameDifference(prevFrame, currFrame);
    return String.format("帧优化运行中\n帧差异值: %d\n执行时间: %.3f ms", difference, executionTimeMs);
}
```

**评估**: ⚠️ **部分伪装** - 
- 系统设置确实存在（Vivo游戏魔盒功能）
- 但NEON优化纯属视觉效果，随机生成数据无实际作用

---

#### 3.4.3 VAA抗闪烁

**开关ID**: `switch_virtual_vaa`

**启用命令**:
```bash
echo 已开启VAA抗闪烁
settings put global vaa_value_from_gamecube 1
```

**评估**: ⚠️ **厂商专属** - Vivo游戏抗闪烁功能

---

### 3.5 A+调度设置

**RadioGroup ID**: `radio_group_k_scheduler`

**选项**：
1. 关闭调度（默认）
2. 流畅模式
3. 简洁模式

---

#### 3.5.1 流畅模式

**执行命令** (超长脚本，核心内容)：

```bash
# SurfaceFlinger优化
setprop debug.sf.early.app.duration 1000000
setprop debug.sf.early.sf.duration 1000000
setprop debug.sf.earlyGl.app.duration 1000000
setprop debug.sf.earlyGl.sf.duration 1000000

# SQLite优化
setprop debug.sqlite.wal.syncmode off
setprop debug.sqlite.syncmode off
setprop debug.sqlite.journalmode off

# 硬件加速
setprop debug.sf.hw 1
setprop debug.egl.hw 1

# Vivo系统广播清理
am broadcast -a com.vivo.gamewatch.modules.namelist.appFpsSettings
am broadcast -a com.vivo.gamewatch.modules.namelist.TempFpsSettings
...

# 日志清理
logcat -c
logcat -b all -c

# 应用编译优化
pm clear com.vivo.gamewatch
cmd package compile -m speed-profile com.vivo.gamewatch

# 存储优化
sm defragment run
sm idle-maint run
sm fstrim
```

**评估**: ✅ **真实优化** - 包含大量有效的系统级优化命令

---

#### 3.5.2 简洁模式

**执行命令**:
```bash
#!/system/bin/sh
set -eo pipefail

# 定义需要清理的特殊前缀
TARGET_PREFIXES="
  vendor. sys. ro. persist. 
  init. debug. dev. service.
  temp. app_
"

# 1. 重置基础设置
settings reset global 0
settings reset secure 0

# 2. 清理全部分区的null值键
for type in secure system global; do
  settings list "$type" | while IFS='=' read -r key value; do
    if [ -n "$key" ] && [ "$value" = "null" ]; then
      settings delete "$type" "$key" >/dev/null 2>&1
    fi
  done
done

# 3. 清理system分区目标前缀键
settings list system | while IFS='=' read -r key _; do
  if [ -z "$key" ]; then
    continue
  fi
  for prefix in $TARGET_PREFIXES; do
    if [[ "$key" == "${prefix}"* ]]; then
      settings delete system "$key" >/dev/null 2>&1
      break
    fi
  done
done
```

**评估**: ⚠️ **风险较高** - 批量删除系统设置可能导致系统异常

---

#### 3.5.3 关闭调度

**执行命令**:
```bash
# 删除Vivo游戏优化相关设置
settings delete secure vivo_gamecube_ai_strategy
settings delete secure vivo_gamecube_ai_situation_stage
settings delete secure vivo_gamecube_solo_stage
...
sleep 3
echo 即将关闭设备
reboot
```

**评估**: ✅ **功能完整** - 清理所有游戏优化配置并重启

---

### 3.6 ColorOS专区

#### 3.6.1 流畅插件

**开关ID**: `switch_coloros_1`

**启用命令**:
```bash
echo 流畅插件已启用
settings put global oplus_customize_system_stable_plan_switch 1
setprop debug.sf.latch_unsignaled true
settings put system log_switch_type 0
settings put system osync_toast_ready 0
settings put system osync_temperature_rate 0
```

**评估**: ⚠️ **厂商专属** - OPPO/OnePlus/Realme设备

---

#### 3.6.2 一键墓碑

**开关ID**: `switch_coloros_2`

**命令**: 同上文墓碑机制

---

#### 3.6.3 未开放功能

**开关ID**: `switch_coloros_3`

**状态**: `android:enabled="false"` （禁用状态）

**评估**: ❌ **占位空壳**

---

### 3.7 HyperOS专区

#### 3.7.1 极致电竞

**开关ID**: `switch_hyperos_gaming`

**启用命令** (部分核心设置):
```bash
settings put system speed_mode 1
settings put secure speed_mode_enable 1
settings put global VRS_SOC 1
settings put system is_kaware_mode 3
settings put global x7_thermal_limit_switch 0
settings put global GPUTUNER_SWITCH true
settings put global game_hdr 1
setprop debug.config.media.video.frc.support true
settings put secure audio_game_4d 1
```

**评估**: ⚠️ **厂商专属** - 小米/Redmi HyperOS游戏优化

---

### 3.8 应用增强设置

#### 3.8.1 悬浮窗显示

**功能**: 在应用上方显示优化状态悬浮窗

**实现**:
```java
private void showFloatWindow() {
    this.floatView = LayoutInflater.from(requireContext()).inflate(R.layout.float_window, null);
    WindowManager.LayoutParams params = new WindowManager.LayoutParams(
        WindowManager.LayoutParams.WRAP_CONTENT,
        WindowManager.LayoutParams.WRAP_CONTENT,
        getOverlayType(),
        WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
        PixelFormat.TRANSLUCENT
    );
    params.gravity = Gravity.TOP | Gravity.RIGHT;
    windowManager.addView(floatView, params);
}
```

**评估**: ✅ **功能完整** - 使用WindowManager实现真实悬浮窗

---

#### 3.8.2 应用优化（开启）

**按钮ID**: `btn_enable`

**执行命令** (针对选中的应用包名):
```bash
echo Monster+ 开启
settings put secure monster_plus_app_list <package_name>

echo 游戏模式配置
settings put system is_game_mode 1
...

echo 应用优化完成
```

**评估**: ⚠️ **厂商专属** - Vivo Monster+性能模式

---

#### 3.8.3 应用优化（关闭）

**按钮ID**: `btn_disable`

**执行命令**: 删除所有应用增强配置

---

#### 3.8.4 ColorOS应用优化

**按钮ID**: `btn_enable_color` / `btn_disable_color`

```bash
echo 已开启ColorOS游戏优化
```

**说明**: ColorOS版本的应用优化命令为空壳，仅输出echo，无实际优化作用。

---

#### 3.8.5 直驱供电

**开关ID**: `switch_direct_power`

**启用命令**:
```bash
echo 直驱供电已开启
settings put secure power_engine_and_plugged_state_and_charging_aod 1,1,true
settings put secure settings_battery_charge_director 1
settings put secure settings_battery_charge_director_game_cube 1
settings put secure vivo_keyguard_super_speed_charge_switch 0
settings put system balance_charge_switch 0
settings put system vivo_keyguard_show_charge_mode_switch 0
current_level=$(dumpsys battery | awk '/level/{print $2}')
settings put secure smart_charge_upper_value $current_level
```

**原理**: 充电时电源直接供电给设备，绕过电池充电

**评估**: ⚠️ **厂商专属** - Vivo直驱供电功能，需硬件支持

---

### 3.9 命令执行机制

**对话框显示命令输出**:

```java
private void executeCommandWithProgress(String command) {
    // 创建对话框
    View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_command_output, null);
    commandOutputTextView = dialogView.findViewById(R.id.command_output_text_view);
    commandProgressBar = dialogView.findViewById(R.id.command_progress_bar);
    
    // 异步执行命令
    new Thread(() -> {
        try {
            Process process = Shizuku.newProcess(new String[]{"sh"}, null, null);
            OutputStream out = process.getOutputStream();
            out.write((command + "\n").getBytes());
            out.flush();
            
            // 读取输出
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                String finalLine = line;
                outputHandler.post(() -> {
                    commandOutputTextView.append(finalLine + "\n");
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "命令执行失败", e);
        }
    }).start();
}
```

---

## 4. 电池监控功能（Fragment1 & BatteryMonitorService）

### 4.1 电池图表（Fragment1）

**UI布局**: `fragment_1.xml`

**功能**:
- 实时显示电池百分比
- 动画显示电量变化
- 绘制24小时电量柱状图
- 点击柱形查看详细信息

**数据存储**:
```java
private void saveChartDataToPrefs() {
    SharedPreferences prefs = context.getSharedPreferences("BatteryChartData", 0);
    String json = gson.toJson(chartDataList);
    prefs.edit().putString("chart_data_list", json).apply();
}
```

**图表绘制**:
```java
private void addChartData(int percent, int state, boolean withAnimation) {
    int barWidth = dpToPx(8);
    int maxHeight = dpToPx(150);
    int targetHeight = Math.min(maxHeight, Math.max(dpToPx(15), (int) ((percent / 100.0f) * maxHeight)));
    
    View bar = new View(getContext());
    GradientDrawable shape = new GradientDrawable();
    shape.setShape(GradientDrawable.RECTANGLE);
    shape.setCornerRadius(dpToPx(4));
    shape.setColor(getBarColor(state)); // 充电=绿色, 低电量=红色, 正常=蓝色
    bar.setBackground(shape);
    
    if (withAnimation) {
        animateBarHeight(bar, (int) (targetHeight * 0.1f), targetHeight);
    }
    
    chartContainer.addView(bar);
}
```

**评估**: ✅ **功能完整** - 使用Android原生API实现电池监控

---

### 4.2 后台监控服务（BatteryMonitorService）

**服务类型**: 前台服务 (Foreground Service)

**功能**:
1. 持续监听电池状态变化
2. 计算平均功耗
3. 显示通知栏电池信息
4. 持久化电池数据

**功耗计算**:
```java
private void calculatePowerConsumption(int newPercent) {
    long currentTime = System.currentTimeMillis();
    long timeDiff = currentTime - lastRecordTime;
    int percentDiff = lastRecordPercent - newPercent;
    
    if (timeDiff > 0 && percentDiff > 0) {
        float timeHours = timeDiff / 3600000.0f;
        float capacityUsed = (batteryCapacity * percentDiff) / 100.0f;
        float power = Math.max(50.0f, Math.min(3000.0f, capacityUsed / timeHours));
        
        powerRecords.add(power);
        if (powerRecords.size() >= 30) {
            powerRecords.remove(0);
        }
    }
}
```

**通知显示**:
```java
private Notification createNotification() {
    String powerInfo = powerRecords.isEmpty() ? "计算中..." : 
        String.format("%.1f mAh/小时", calculateAveragePower());
    
    return new NotificationCompat.Builder(this, CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_battery_24)
        .setContentTitle("KBattery 电池监控")
        .setContentText("电池状态: " + chargingStatus + " | 功耗: " + powerInfo)
        .setStyle(new NotificationCompat.BigTextStyle()
            .bigText("当前电量: " + currentPercent + "%\n" +
                     "电池状态: " + chargingStatus + "\n" +
                     "当前功耗: " + powerInfo + "\n" +
                     "电池容量: " + batteryCapacity + " mAh"))
        .build();
}
```

**评估**: ✅ **功能完整** - 基于BatteryManager API实现真实功耗监控

---

## 5. 关于页面（Fragment2）

**UI布局**: `fragment_2.xml`

**功能卡片**:
1. 出品人员 - 跳转AuthorActivity
2. 前往捐赠 - 打开爱发电链接
3. 使用手册 - 打开官网 https://kbattery.mtaokj.com/
4. 提交反馈 - 打开QQ群链接
5. 加入群聊 - 打开QQ群分享链接
6. 更新日志 - 弹窗显示完整版本历史

**评估**: ✅ **标准功能** - 常规的关于页面实现

---

## 6. 技术实现细节

### 6.1 Shizuku权限检查

```java
private boolean checkShizukuPermission() {
    if (!MainActivity.isShizukuAvailable || !MainActivity.isShizukuActive) {
        showAlertDialog("Shizuku未运行", "请先启动Shizuku服务");
        return false;
    }
    if (!MainActivity.isShizukuGranted) {
        showAlertDialog("权限未授予", "请授予Shizuku权限");
        return false;
    }
    return true;
}
```

### 6.2 设备兼容性适配

```java
private boolean checkDeviceCompatibility() {
    String manufacturer = Build.MANUFACTURER.toLowerCase();
    
    // Vivo设备显示所有功能
    if (manufacturer.contains("vivo")) {
        setVisibility(kSchedulerCard, View.VISIBLE);
        setVisibility(memorySettingsCard, View.VISIBLE);
        setVisibility(displaySettingsCard, View.VISIBLE);
        setVisibility(colorOsCard, View.GONE);
        setVisibility(hyperOsCard, View.GONE);
        return true;
    }
    
    // OPPO设备显示ColorOS功能
    if (manufacturer.contains("oppo")) {
        setVisibility(colorOsCard, View.VISIBLE);
        setVisibility(memorySettingsCard, View.GONE);
        setVisibility(displaySettingsCard, View.GONE);
        setVisibility(hyperOsCard, View.GONE);
        return true;
    }
    
    // 小米设备显示HyperOS功能
    if (manufacturer.contains("xiaomi")) {
        setVisibility(hyperOsCard, View.VISIBLE);
        setVisibility(memorySettingsCard, View.GONE);
        setVisibility(displaySettingsCard, View.GONE);
        setVisibility(colorOsCard, View.GONE);
        return true;
    }
    
    // 不支持的设备
    showUnsupportedDeviceDialog();
    return false;
}
```

### 6.3 数据持久化

**SharedPreferences使用场景**:
1. 用户选择的优化模式
2. 开关状态保存
3. 选中的应用包名列表
4. 电池图表历史数据（JSON格式）
5. 首次启动标记

```java
// 保存选中的应用列表
private void saveSelectedAppsToPrefs() {
    Set<String> packageSet = new HashSet<>(selectedPackages);
    sharedPreferences.edit()
        .putStringSet(KEY_SELECTED_APPS, packageSet)
        .apply();
}

// 保存开关状态
private void setupSwitch(View view, int switchId, String enableCmd, String disableCmd) {
    CompoundButton switchView = view.findViewById(switchId);
    SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, 0);
    boolean savedChecked = prefs.getBoolean(String.valueOf(switchId), false);
    switchView.setChecked(savedChecked);
    
    switchView.setOnCheckedChangeListener((buttonView, isChecked) -> {
        prefs.edit().putBoolean(String.valueOf(switchId), isChecked).apply();
        executeCommand(isChecked ? enableCmd : disableCmd);
    });
}
```

### 6.4 动画实现

**电量百分比数字动画**:
```java
private void animatePercentChange(int targetPercent) {
    ValueAnimator animator = ValueAnimator.ofInt(startPercent, targetPercent);
    animator.setDuration(300);
    animator.setInterpolator(new AccelerateDecelerateInterpolator());
    animator.addUpdateListener(animation -> {
        int value = (int) animation.getAnimatedValue();
        batteryPercent.setText(value + "%");
    });
    animator.start();
}
```

**柱状图高度动画**:
```java
private void animateBarHeight(View bar, int startHeight, int targetHeight) {
    ValueAnimator animator = ValueAnimator.ofInt(startHeight, targetHeight);
    animator.setDuration(1000);
    animator.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f));
    animator.addUpdateListener(animation -> {
        int animatedValue = (int) animation.getAnimatedValue();
        ViewGroup.LayoutParams params = bar.getLayoutParams();
        params.height = animatedValue;
        bar.setLayoutParams(params);
    });
    animator.start();
}
```

### 6.5 更新检查

```java
private void checkForUpdate() {
    new Thread(() -> {
        try {
            String sourceCode = fetchWebSource("https://sharechain.qq.com/ca3a9cdfc255301103afe5deb332af16");
            String versionCode = extractContent(sourceCode, "【", "】");
            String updateLog = extractContent(sourceCode, "《", "》");
            
            if (Double.parseDouble(versionCode) == 1.182) {
                showToast("已是最新版本");
            } else {
                showUpdateDialog(updateLog);
            }
        } catch (IOException e) {
            showToast("检查更新失败");
        }
    }).start();
}
```

---

## 7. 总结

### 7.1 优化功能分类统计

| 类别 | 功能数量 | 真实优化 | 厂商专属 | 空壳功能 |
|------|---------|---------|---------|---------|
| 模式优化 | 4 | 2 | 2 | 0 |
| 实用功能 | 3 | 1 | 2 | 0 |
| 显示设置 | 3 | 1 | 1 | 1 |
| 游戏设置 | 3 | 0 | 2 | 1 |
| A+调度 | 3 | 2 | 0 | 1 |
| ColorOS专区 | 3 | 1 | 1 | 1 |
| HyperOS专区 | 1 | 0 | 1 | 0 |
| 应用增强 | 5 | 1 | 3 | 1 |
| **总计** | **25** | **8 (32%)** | **12 (48%)** | **5 (20%)** |

### 7.2 真实有效的优化功能

✅ **真正能产生优化效果的功能**：

1. **省电模式** - 基于Android Doze机制
2. **默认模式** - 还原系统设置
3. **墓碑机制** - Android 11+ Cached Apps Freezer
4. **Vulkan渲染** - 切换渲染引擎
5. **A+调度-流畅模式** - 系统级优化命令集合
6. **A+调度-简洁模式** - 清理系统设置残留
7. **悬浮窗显示** - WindowManager实现
8. **电池监控** - BatteryManager API

### 7.3 厂商专属功能

⚠️ **仅特定厂商设备生效**：

**Vivo/iQoo专属** (8项):
- 性能模式（游戏魔盒）
- 充电加速
- WLAN提升
- 原彩显示
- 全局插帧
- VAA抗闪烁
- 应用Monster+优化
- 直驱供电

**ColorOS专属** (2项):
- 流畅插件
- ColorOS应用优化

**HyperOS专属** (1项):
- 极致电竞模式

### 7.4 空壳功能

❌ **仅输出echo或无实际作用**：

1. **AI策略** - 仅echo
2. **光线追踪** - 仅echo
3. **全局插帧的NEON优化** - 随机数据计算，无实际作用
4. **A+调度-关闭调度** - 仅删除配置+重启
5. **ColorOS未开放功能** - 禁用状态

### 7.5 技术亮点

✨ **值得学习的技术点**：

1. **Shizuku集成** - 优雅地实现免root执行系统命令
2. **动态适配** - 根据设备品牌显示不同功能模块
3. **命令执行框架** - 异步执行shell命令并实时显示输出
4. **数据可视化** - 自绘电池图表（柱状图）
5. **前台服务** - 正确实现持久化电池监控
6. **Material Design** - 完整的MD3风格UI

### 7.6 风险提示

⚠️ **使用本应用的潜在风险**：

1. **系统稳定性** - 修改大量系统设置可能导致异常
2. **保修影响** - 需要Shizuku可能影响设备保修
3. **功能失效** - 厂商系统更新后优化可能失效
4. **电池健康** - 直驱供电等功能可能影响电池寿命
5. **兼容性问题** - 非目标设备强制使用可能导致系统异常

### 7.7 最终评价

**优点**：
- ✅ 部分优化功能确实基于Android原生机制
- ✅ 电池监控功能真实有效
- ✅ UI设计精美，用户体验良好
- ✅ 代码架构清晰，模块化设计

**缺点**：
- ❌ 约20%的功能为空壳（仅显示效果）
- ❌ 约48%的功能仅特定厂商设备可用
- ❌ 缺少功能有效性验证机制
- ❌ 部分优化命令可能引入系统风险

**总体评分**: 6.5/10

**适用人群**: 
- Vivo/iQoo设备用户
- 愿意承担风险的高级玩家
- 对系统优化有一定了解的用户

---

## 附录

### A. 完整功能清单

#### A.1 模式优化（ModeActivity）

| 模式 | 启用命令关键点 | 效果评估 |
|------|--------------|---------|
| 省电模式 | `adaptive_battery_management_enabled=1`<br>`app_standby_enabled=1`<br>`pem_in_sleepmode=1` | ✅ 真实优化 |
| 性能模式 | `game_enable_gpu_turbo=1`<br>`game_enable_cpu_turbo=1`<br>`gamecube_frame_interpolation=1` | ⚠️ Vivo专属 |
| 日常模式 | `power_save_type=1`<br>`motion_blur_state=1`<br>`debug.egl.hw=1` | ⚠️ 部分生效 |
| 默认模式 | 删除所有优化配置 | ✅ 功能完整 |

#### A.2 高级优化（Fragment3）

| 分类 | 功能 | 命令关键字 | 评估 |
|------|------|-----------|------|
| 实用 | 墓碑机制 | `cached_apps_freezer` | ✅ 真实 |
| 实用 | 充电加速 | `vivo_keyguard_super_speed_charge_switch` | ⚠️ Vivo |
| 实用 | WLAN提升 | `is_network_enhancement_enable` | ⚠️ Vivo |
| 显示 | 原彩显示 | `vivo_auto_eye_cct_enable` | ⚠️ Vivo |
| 显示 | Vulkan渲染 | `debug.hwui.renderer=skiavk` | ✅ 真实 |
| 显示 | AI策略 | echo only | ❌ 空壳 |
| 游戏 | 光线追踪 | echo only | ❌ 空壳 |
| 游戏 | 全局插帧 | `gamecube_frame_interpolation` | ⚠️ Vivo |
| 游戏 | VAA抗闪烁 | `vaa_value_from_gamecube` | ⚠️ Vivo |
| 调度 | 流畅模式 | 多命令组合 | ✅ 真实 |
| 调度 | 简洁模式 | settings批量清理 | ⚠️ 风险 |
| ColorOS | 流畅插件 | `oplus_customize_system_stable_plan_switch` | ⚠️ OPPO |
| ColorOS | 一键墓碑 | `cached_apps_freezer` | ✅ 真实 |
| HyperOS | 极致电竞 | `speed_mode=1` | ⚠️ 小米 |
| 应用 | 悬浮窗 | WindowManager | ✅ 真实 |
| 应用 | 直驱供电 | `settings_battery_charge_director` | ⚠️ Vivo |

### B. 系统设置键值对照表

| Settings命令 | 作用域 | 说明 |
|-------------|-------|------|
| `settings put global adaptive_battery_management_enabled` | global | 自适应电池管理 |
| `settings put global cached_apps_freezer` | global | 应用冻结机制 |
| `settings put system is_game_mode` | system | 游戏模式标识 |
| `settings put secure monster_plus_app_list` | secure | Monster+应用列表 |
| `setprop debug.hwui.renderer` | property | HWUI渲染器 |
| `device_config put core_graphics` | device_config | 图形配置 |

### C. Shizuku命令执行流程图

```
用户点击功能开关
    ↓
检查Shizuku权限
    ↓
[权限OK] → 创建Shell进程 (Shizuku.newProcess)
    ↓
写入Shell命令到OutputStream
    ↓
异步读取stdout和stderr
    ↓
在UI线程更新TextView显示输出
    ↓
执行完成，关闭进程
    ↓
保存状态到SharedPreferences
```

### D. 参考资料

1. **Android Developer文档**: https://developer.android.com/
2. **Shizuku官方文档**: https://shizuku.rikka.app/
3. **Android Settings数据库**: https://android.googlesource.com/
4. **Vivo开发者平台**: https://dev.vivo.com.cn/
5. **本应用官网**: https://kbattery.mtaokj.com/

---

**报告生成时间**: 2025-10-29  
**分析工具**: 人工逆向分析  
**项目路径**: `D:\逆向\Android\KBattery`  
**应用版本**: V1.18.2 (versionCode=1)

