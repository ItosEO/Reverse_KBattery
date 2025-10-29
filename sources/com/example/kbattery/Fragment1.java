package com.example.kbattery;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.PathInterpolator;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.work.Constraints;
import androidx.work.ListenableWorker;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.example.kbattery.utils.DialogUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/* loaded from: classes3.dex */
public class Fragment1 extends Fragment {
    private static final int ANIMATION_DURATION = 1000;
    private static final float ANIMATION_START_HEIGHT_RATIO = 0.1f;
    private static final int BAR_CORNER_RADIUS = 4;
    private static final int BAR_MARGIN = 3;
    private static final int BAR_STROKE_WIDTH = 1;
    private static final int BAR_WIDTH = 8;
    private static final int BASE_HEIGHT = 0;
    private static final String CHART_DATA_KEY = "chart_data_list";
    private static final long INTERVAL_24_HOURS = 86400000;
    private static final long INTERVAL_3_MINUTES = 180000;
    private static final String LAST_UPDATED_KEY = "last_updated_time";
    private static final int LOW_THRESHOLD = 20;
    private static final int MAX_HEIGHT = 150;
    private static final int MIN_BAR_HEIGHT = 15;
    private static final long MIN_RECORD_GAP = 60000;
    private static final String PREFS_NAME = "BatteryChartData";
    private static final int STATE_CHARGING = 1;
    private static final int STATE_LOW = 2;
    private static final int STATE_NORMAL = 0;
    private static final String TAG = "BatteryMonitor";
    private Runnable addBarRunnable;
    private TextView batteryCapacityInfo;
    private TextView batteryPercent;
    private LinearLayout chartContainer;
    private Runnable clearChartRunnable;
    private Gson gson;
    private Handler mainHandler;
    private int lastPercent = -1;
    private int lastState = -1;
    private long lastRecordTime = 0;
    private List<BatteryData> chartDataList = new ArrayList();
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() { // from class: com.example.kbattery.Fragment1.1
        @Override // android.content.BroadcastReceiver
        public void onReceive(Context context, Intent intent) throws NumberFormatException {
            long currentTime = System.currentTimeMillis();
            if (currentTime - Fragment1.this.lastUpdateTime < 1000) {
                return;
            }
            Fragment1.this.updateBatteryPercent(intent);
            Fragment1.this.updateBatteryInfo(intent);
            Fragment1.this.lastUpdateTime = currentTime;
        }
    };
    private long lastUpdateTime = 0;
    private ValueAnimator percentAnimator = null;

    private static class BatteryData {
        int percent;
        int state;
        long timestamp;

        public BatteryData(int percent, int state, long timestamp) {
            this.percent = percent;
            this.state = state;
            this.timestamp = timestamp;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.gson = new Gson();
        initRunnables();
        loadChartDataFromPrefs();
        setupWorkManager();
    }

    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            View view = inflater.inflate(R.layout.fragment_1, container, false);
            if (view == null) {
                Log.e(TAG, "Failed to inflate fragment_1 layout");
                return null;
            }
            initViews(view);
            setClickListeners(view);
            view.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda11
                @Override // java.lang.Runnable
                public final void run() {
                    this.f$0.initOnEnter();
                }
            }, 100L);
            return view;
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            return null;
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onResume() {
        super.onResume();
        try {
            if (isAdded() && getActivity() != null && getView() != null) {
                getView().postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda10
                    @Override // java.lang.Runnable
                    public final void run() throws NumberFormatException {
                        this.f$0.m103lambda$onResume$0$comexamplekbatteryFragment1();
                    }
                }, 100L);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in onResume", e);
        }
    }

    /* renamed from: lambda$onResume$0$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m103lambda$onResume$0$comexamplekbatteryFragment1() throws NumberFormatException {
        Intent batteryIntent;
        if (isAdded() && getActivity() != null) {
            checkAndFillMissingData();
            redrawChart();
            startTimedTasks();
            long currentTime = System.currentTimeMillis();
            if (getContext() != null && currentTime - this.lastUpdateTime > 2000 && (batteryIntent = getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null) {
                updateBatteryPercent(batteryIntent);
                updateBatteryInfo(batteryIntent);
                this.lastUpdateTime = currentTime;
            }
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onPause() {
        super.onPause();
        try {
            stopTimedTasks();
            saveChartDataToPrefs();
            try {
                if (getActivity() != null) {
                    getActivity().unregisterReceiver(this.batteryReceiver);
                }
            } catch (Exception e) {
                Log.e(TAG, "注销电池广播失败", e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "Error in onPause", e2);
        }
    }

    @Override // androidx.fragment.app.Fragment
    public void onDestroy() {
        super.onDestroy();
        try {
            stopTimedTasks();
            saveChartDataToPrefs();
            if (this.mainHandler != null) {
                this.mainHandler.removeCallbacksAndMessages(null);
                this.mainHandler = null;
            }
            try {
                if (getActivity() != null) {
                    getActivity().unregisterReceiver(this.batteryReceiver);
                }
            } catch (Exception e) {
                Log.e(TAG, "销毁时注销电池广播失败", e);
            }
            this.batteryPercent = null;
            this.batteryCapacityInfo = null;
            this.chartContainer = null;
            this.chartDataList = null;
            this.addBarRunnable = null;
            this.clearChartRunnable = null;
        } catch (Exception e2) {
            Log.e(TAG, "Error in onDestroy", e2);
        }
    }

    private void initViews(View view) {
        try {
            if (view == null) {
                Log.e(TAG, "初始化视图失败：view为null");
                return;
            }
            this.batteryPercent = (TextView) view.findViewById(R.id.battery_percent);
            this.batteryCapacityInfo = (TextView) view.findViewById(R.id.battery_health);
            this.chartContainer = (LinearLayout) view.findViewById(R.id.chart_container);
            if (this.batteryPercent == null) {
                Log.e(TAG, "布局中未找到 battery_percent 控件！");
            }
            if (this.batteryCapacityInfo == null) {
                Log.e(TAG, "布局中未找到 battery_health 控件！");
            }
            if (this.chartContainer == null) {
                Log.e(TAG, "布局中未找到 chart_container 控件！");
            }
            if (this.chartContainer != null) {
                this.chartContainer.removeAllViews();
                if (this.chartContainer.getBackground() == null) {
                    this.chartContainer.setBackgroundColor(0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "初始化视图时发生错误", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void initOnEnter() {
        Intent batteryIntent;
        try {
            if (isAdded() && getActivity() != null) {
                registerBatteryReceiver();
                if (this.batteryCapacityInfo != null) {
                    this.batteryCapacityInfo.setText("电池信息: 监控中");
                }
                if (getContext() != null && (batteryIntent = getContext().registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null) {
                    updateBatteryPercent(batteryIntent);
                    updateBatteryInfo(batteryIntent);
                }
                return;
            }
            Log.w(TAG, "Fragment未附加到Activity，跳过初始化");
        } catch (Exception e) {
            Log.e(TAG, "初始化进入状态时发生错误", e);
        }
    }

    private void initRunnables() {
        this.addBarRunnable = new Runnable() { // from class: com.example.kbattery.Fragment1.2
            @Override // java.lang.Runnable
            public void run() {
                if (Fragment1.this.isAdded() && Fragment1.this.getContext() != null) {
                    Fragment1.this.addTimedBar();
                }
                Fragment1.this.scheduleNextAddBar();
            }
        };
        this.clearChartRunnable = new Runnable() { // from class: com.example.kbattery.Fragment1.3
            @Override // java.lang.Runnable
            public void run() {
                Fragment1.this.clearExpiredData();
                Fragment1.this.scheduleNextClearChart();
            }
        };
    }

    private void setupWorkManager() {
        Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_REQUIRED).setRequiresBatteryNotLow(false).build();
        PeriodicWorkRequest batteryWork = new PeriodicWorkRequest.Builder((Class<? extends ListenableWorker>) BatteryWorker.class, 3L, TimeUnit.MINUTES).setConstraints(constraints).build();
        WorkManager.getInstance(requireContext()).enqueue(batteryWork);
    }

    private void loadChartDataFromPrefs() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String json = prefs.getString(CHART_DATA_KEY, "");
        this.lastRecordTime = prefs.getLong(LAST_UPDATED_KEY, 0L);
        if (!json.isEmpty()) {
            Type type = new TypeToken<List<BatteryData>>() { // from class: com.example.kbattery.Fragment1.4
            }.getType();
            List<BatteryData> savedList = (List) this.gson.fromJson(json, type);
            if (savedList != null) {
                this.chartDataList = filterExpiredData(savedList);
            }
        }
    }

    private void saveChartDataToPrefs() {
        Context context = getContext();
        if (context == null) {
            return;
        }
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = prefs.edit();
        String json = this.gson.toJson(this.chartDataList);
        editor.putString(CHART_DATA_KEY, json);
        editor.putLong(LAST_UPDATED_KEY, this.lastRecordTime);
        editor.apply();
    }

    private void checkAndFillMissingData() {
        long currentTime = System.currentTimeMillis();
        if (this.lastRecordTime > 0 && currentTime - this.lastRecordTime > 240000) {
            Log.i(TAG, "检测到可能遗漏的数据，尝试补充");
            addMissingBatteryData();
        }
    }

    private void addMissingBatteryData() {
        Intent batteryIntent;
        Context context = getContext();
        if (context != null && (batteryIntent = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"))) != null) {
            int level = batteryIntent.getIntExtra("level", -1);
            int scale = batteryIntent.getIntExtra("scale", -1);
            int currentPercent = (level == -1 || scale == -1) ? 50 : (level * 100) / scale;
            int state = getBatteryState(batteryIntent);
            BatteryData newData = new BatteryData(currentPercent, state, System.currentTimeMillis());
            this.chartDataList.add(newData);
            this.lastRecordTime = System.currentTimeMillis();
            saveChartDataToPrefs();
            addChartData(currentPercent, state);
        }
    }

    private List<BatteryData> filterExpiredData(List<BatteryData> dataList) {
        long currentTime = System.currentTimeMillis();
        List<BatteryData> validData = new ArrayList<>();
        for (BatteryData data : dataList) {
            if (currentTime - data.timestamp < INTERVAL_24_HOURS) {
                validData.add(data);
            }
        }
        return validData;
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void clearExpiredData() {
        this.chartDataList = filterExpiredData(this.chartDataList);
        saveChartDataToPrefs();
        redrawChart();
    }

    private void startTimedTasks() {
        if (this.mainHandler == null) {
            return;
        }
        addTimedBar();
        scheduleNextAddBar();
        scheduleNextClearChart();
    }

    private void stopTimedTasks() {
        if (this.mainHandler == null) {
            return;
        }
        this.mainHandler.removeCallbacks(this.addBarRunnable);
        this.mainHandler.removeCallbacks(this.clearChartRunnable);
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleNextAddBar() {
        if (this.mainHandler != null) {
            this.mainHandler.postDelayed(this.addBarRunnable, INTERVAL_3_MINUTES);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void scheduleNextClearChart() {
        if (this.mainHandler != null) {
            this.mainHandler.postDelayed(this.clearChartRunnable, INTERVAL_24_HOURS);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void addTimedBar() {
        int level;
        Context context = getContext();
        if (context == null) {
            Log.w(TAG, "上下文为空，无法获取电池状态");
            return;
        }
        long currentTime = System.currentTimeMillis();
        if (currentTime - this.lastRecordTime < MIN_RECORD_GAP) {
            Log.i(TAG, "记录间隔过短，跳过本次记录");
            return;
        }
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        if (batteryIntent == null) {
            Log.w(TAG, "无法获取电池状态，使用上次数据生成柱形");
            currentPercent = this.lastPercent != -1 ? this.lastPercent : 50;
            level = this.lastState != -1 ? this.lastState : 0;
        } else {
            int level2 = batteryIntent.getIntExtra("level", -1);
            int scale = batteryIntent.getIntExtra("scale", -1);
            if (level2 != -1 && scale != -1) {
                currentPercent = (level2 * 100) / scale;
            }
            int state = getBatteryState(batteryIntent);
            this.lastPercent = currentPercent;
            this.lastState = state;
            level = state;
        }
        BatteryData newData = new BatteryData(currentPercent, level, currentTime);
        this.chartDataList.add(newData);
        this.lastRecordTime = currentTime;
        saveChartDataToPrefs();
        addChartData(currentPercent, level);
    }

    private void redrawChart() {
        try {
            if (this.chartContainer == null || !isAdded() || getActivity() == null) {
                return;
            }
            try {
                this.chartContainer.removeAllViews();
                if (this.chartDataList != null && !this.chartDataList.isEmpty()) {
                    final int totalDataCount = this.chartDataList.size();
                    for (int i = 0; i < totalDataCount; i += 50) {
                        final int startIndex = i;
                        final int endIndex = Math.min(i + 50, totalDataCount);
                        if (this.mainHandler != null) {
                            this.mainHandler.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda0
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f$0.m104lambda$redrawChart$1$comexamplekbatteryFragment1(startIndex, endIndex, totalDataCount);
                                }
                            }, (i / 50) * 50);
                        }
                    }
                    return;
                }
                Log.i(TAG, "图表数据为空，不进行重绘");
            } catch (Exception e) {
                Log.e(TAG, "清除图表视图失败", e);
            }
        } catch (Exception e2) {
            Log.e(TAG, "重绘图表时发生错误", e2);
        }
    }

    /* renamed from: lambda$redrawChart$1$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m104lambda$redrawChart$1$comexamplekbatteryFragment1(int startIndex, int endIndex, int totalDataCount) {
        try {
            if (isAdded() && this.chartContainer != null) {
                for (int j = startIndex; j < endIndex && j < this.chartDataList.size(); j++) {
                    BatteryData data = this.chartDataList.get(j);
                    if (data != null) {
                        addChartData(data.percent, data.state, false);
                    }
                }
                if (endIndex >= totalDataCount) {
                    scrollToLatest();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "分批添加图表数据失败", e);
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBatteryInfo(Intent intent) {
        if (this.batteryCapacityInfo == null || !isAdded() || getActivity() == null) {
            return;
        }
        int health = intent.getIntExtra("health", 1);
        String healthStatus = getHealthStatusString(health);
        String currentText = this.batteryCapacityInfo.getText().toString();
        String newText = "电池健康: " + healthStatus;
        if (!currentText.equals(newText)) {
            this.batteryCapacityInfo.setText(newText);
        }
    }

    private String getHealthStatusString(int health) {
        switch (health) {
            case 2:
                return "良好";
            case 3:
                return "过热";
            case 4:
                return "已耗尽";
            case 5:
                return "电压过高";
            case 6:
                return "未知故障";
            case 7:
                return "温度过低";
            default:
                return "未知";
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void updateBatteryPercent(Intent intent) throws NumberFormatException {
        if (this.batteryPercent == null || !isAdded() || getActivity() == null) {
            return;
        }
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        if (level == -1 || scale == -1) {
            return;
        }
        int currentPercent = (level * 100) / scale;
        int state = getBatteryState(intent);
        if (currentPercent != this.lastPercent) {
            animatePercentChange(currentPercent);
            this.lastPercent = currentPercent;
        }
        if (state != this.lastState) {
            this.batteryPercent.setTextColor(getBarColor(state));
            this.lastState = state;
        }
    }

    private void animatePercentChange(int targetPercent) throws NumberFormatException {
        try {
            if (this.batteryPercent != null && isAdded() && getActivity() != null) {
                if (this.percentAnimator != null && this.percentAnimator.isRunning()) {
                    this.percentAnimator.cancel();
                }
                String currentText = "";
                try {
                    currentText = this.batteryPercent.getText().toString().replace("%", "");
                } catch (Exception e) {
                    Log.w(TAG, "获取当前百分比文本失败", e);
                }
                int startPercent = targetPercent;
                try {
                    if (!currentText.isEmpty()) {
                        startPercent = Integer.parseInt(currentText);
                    }
                } catch (NumberFormatException e2) {
                    Log.w(TAG, "解析百分比文本失败", e2);
                }
                if (startPercent == targetPercent) {
                    return;
                }
                this.percentAnimator = ValueAnimator.ofInt(startPercent, targetPercent);
                if (this.percentAnimator == null) {
                    return;
                }
                this.percentAnimator.setDuration(300L);
                this.percentAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
                this.percentAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda9
                    @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                    public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                        this.f$0.m102lambda$animatePercentChange$3$comexamplekbatteryFragment1(valueAnimator);
                    }
                });
                getLifecycle().addObserver(new LifecycleObserver() { // from class: com.example.kbattery.Fragment1.5
                    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                    public void onDestroy() {
                        if (Fragment1.this.percentAnimator != null && Fragment1.this.percentAnimator.isRunning()) {
                            Fragment1.this.percentAnimator.cancel();
                        }
                    }
                });
                this.percentAnimator.start();
            }
        } catch (Exception e3) {
            Log.e(TAG, "创建百分比动画时发生错误", e3);
        }
    }

    /* renamed from: lambda$animatePercentChange$3$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m102lambda$animatePercentChange$3$comexamplekbatteryFragment1(ValueAnimator animation) {
        try {
            if (this.batteryPercent != null && animation != null && isAdded() && getActivity() != null) {
                Object valueObj = animation.getAnimatedValue();
                if (valueObj instanceof Integer) {
                    final int value = ((Integer) valueObj).intValue();
                    this.batteryPercent.post(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda3
                        @Override // java.lang.Runnable
                        public final void run() {
                            this.f$0.m101lambda$animatePercentChange$2$comexamplekbatteryFragment1(value);
                        }
                    });
                }
                return;
            }
            if (this.percentAnimator != null && this.percentAnimator.isRunning()) {
                this.percentAnimator.cancel();
            }
        } catch (Exception e) {
            Log.e(TAG, "百分比动画更新失败", e);
            if (this.percentAnimator != null && this.percentAnimator.isRunning()) {
                this.percentAnimator.cancel();
            }
        }
    }

    /* renamed from: lambda$animatePercentChange$2$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m101lambda$animatePercentChange$2$comexamplekbatteryFragment1(int value) {
        if (this.batteryPercent != null && isAdded()) {
            this.batteryPercent.setText(value + "%");
        }
    }

    private int getBatteryState(Intent intent) {
        int plugged = intent.getIntExtra("plugged", 0);
        boolean isCharging = plugged != 0;
        int level = intent.getIntExtra("level", -1);
        int scale = intent.getIntExtra("scale", -1);
        int percent = (level == -1 || scale == -1) ? 100 : (level * 100) / scale;
        if (isCharging) {
            return 1;
        }
        return percent < 20 ? 2 : 0;
    }

    private void addChartData(int percent, int state) {
        addChartData(percent, state, true);
    }

    private void addChartData(final int percent, final int state, boolean withAnimation) {
        try {
            if (this.chartContainer == null || getContext() == null || !isAdded()) {
                return;
            }
            if (getActivity() == null) {
                return;
            }
            int barWidth = dpToPx(8);
            dpToPx(0);
            int maxHeight = dpToPx(MAX_HEIGHT);
            try {
                final int targetHeight = Math.min(maxHeight, Math.max(dpToPx(15), (int) ((percent / 100.0f) * maxHeight)));
                final View bar = new View(getContext());
                LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(barWidth, withAnimation ? (int) (targetHeight * 0.1f) : targetHeight);
                barParams.setMargins(dpToPx(3), 0, dpToPx(3), 0);
                bar.setLayoutParams(barParams);
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(0);
                shape.setCornerRadius(dpToPx(4));
                try {
                    shape.setColor(getBarColor(state));
                    shape.setStroke(dpToPx(1), Color.parseColor("#EEEEEE"));
                    bar.setBackground(shape);
                    bar.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda4
                        @Override // android.view.View.OnClickListener
                        public final void onClick(View view) {
                            this.f$0.m97lambda$addChartData$4$comexamplekbatteryFragment1(percent, state, view);
                        }
                    });
                    if (this.chartContainer != null && this.chartContainer.getParent() != null) {
                        this.chartContainer.addView(bar);
                        if (withAnimation) {
                            bar.post(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda5
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f$0.m98lambda$addChartData$5$comexamplekbatteryFragment1(bar, targetHeight);
                                }
                            });
                        }
                        if (withAnimation) {
                            this.chartContainer.postDelayed(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda6
                                @Override // java.lang.Runnable
                                public final void run() {
                                    this.f$0.m99lambda$addChartData$6$comexamplekbatteryFragment1();
                                }
                            }, 1100L);
                        } else {
                            scrollToLatest();
                        }
                    }
                } catch (Exception e) {
                    e = e;
                    Log.e(TAG, "添加图表数据时发生错误", e);
                }
            } catch (Exception e2) {
                e = e2;
                Log.e(TAG, "添加图表数据时发生错误", e);
            }
        } catch (Exception e3) {
            e = e3;
        }
    }

    /* renamed from: lambda$addChartData$4$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m97lambda$addChartData$4$comexamplekbatteryFragment1(int finalPercent, int finalState, View v) {
        if (v != null && isAdded() && getContext() != null) {
            showBatteryInfoDialog(finalPercent, finalState);
        }
    }

    /* renamed from: lambda$addChartData$5$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m98lambda$addChartData$5$comexamplekbatteryFragment1(View finalBar, int finalTargetHeight) {
        if (finalBar != null && isAdded() && getActivity() != null) {
            animateBarHeight(finalBar, (int) (finalTargetHeight * 0.1f), finalTargetHeight);
        }
    }

    /* renamed from: lambda$addChartData$6$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m99lambda$addChartData$6$comexamplekbatteryFragment1() {
        if (isAdded() && getActivity() != null) {
            scrollToLatest();
        }
    }

    private void showBatteryInfoDialog(int percent, int state) {
        if (getContext() == null) {
            return;
        }
        DialogUtils.createInfoDialog(requireContext(), "电池信息", "电量: " + percent + "%\n状态: " + getStateString(state), "确定", null).show();
    }

    private void animateBarHeight(final View bar, int startHeight, final int targetHeight) {
        if (bar == null) {
            return;
        }
        try {
            final ValueAnimator animator = ValueAnimator.ofInt(startHeight, targetHeight);
            if (animator == null) {
                return;
            }
            animator.setDuration(1000L);
            animator.setInterpolator(new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f));
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda1
                @Override // android.animation.ValueAnimator.AnimatorUpdateListener
                public final void onAnimationUpdate(ValueAnimator valueAnimator) {
                    this.f$0.m100lambda$animateBarHeight$7$comexamplekbatteryFragment1(bar, valueAnimator);
                }
            });
            animator.addListener(new AnimatorListenerAdapter() { // from class: com.example.kbattery.Fragment1.6
                @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
                public void onAnimationEnd(Animator animation) {
                    ViewGroup.LayoutParams params;
                    super.onAnimationEnd(animation);
                    if (bar != null && Fragment1.this.isAdded() && Fragment1.this.getActivity() != null && (params = bar.getLayoutParams()) != null) {
                        params.height = targetHeight;
                        bar.setLayoutParams(params);
                    }
                }
            });
            getLifecycle().addObserver(new LifecycleObserver() { // from class: com.example.kbattery.Fragment1.7
                @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
                public void onDestroy() {
                    if (animator != null && animator.isRunning()) {
                        animator.cancel();
                    }
                }
            });
            animator.start();
        } catch (Exception e) {
            Log.e(TAG, "创建或启动动画时发生错误", e);
        }
    }

    /* renamed from: lambda$animateBarHeight$7$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m100lambda$animateBarHeight$7$comexamplekbatteryFragment1(View bar, ValueAnimator animation) {
        if (bar == null || animation == null) {
            return;
        }
        try {
            if (isAdded() && getActivity() != null) {
                Object animatedValueObj = animation.getAnimatedValue();
                if (animatedValueObj instanceof Integer) {
                    int animatedValue = ((Integer) animatedValueObj).intValue();
                    ViewGroup.LayoutParams params = bar.getLayoutParams();
                    if (params != null) {
                        params.height = animatedValue;
                        try {
                            bar.setLayoutParams(params);
                            bar.invalidate();
                        } catch (Exception e) {
                            Log.w(TAG, "更新视图布局参数失败", e);
                        }
                    }
                }
            }
        } catch (Exception e2) {
            Log.e(TAG, "动画更新监听发生错误", e2);
        }
    }

    private String getStateString(int state) {
        switch (state) {
            case 1:
                return "充电中";
            case 2:
                return "电量低";
            default:
                return "正常";
        }
    }

    private int getBarColor(int state) {
        switch (state) {
            case 1:
                return Color.parseColor("#4CAF50");
            case 2:
                return Color.parseColor("#F44336");
            default:
                return Color.parseColor("#2196F3");
        }
    }

    private void scrollToLatest() {
        try {
            if (this.chartContainer != null && isAdded() && getActivity() != null) {
                this.chartContainer.post(new Runnable() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda2
                    @Override // java.lang.Runnable
                    public final void run() {
                        this.f$0.m105lambda$scrollToLatest$8$comexamplekbatteryFragment1();
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "调度滚动任务时发生错误", e);
        }
    }

    /* renamed from: lambda$scrollToLatest$8$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m105lambda$scrollToLatest$8$comexamplekbatteryFragment1() {
        HorizontalScrollView scrollView;
        View child;
        try {
            if (this.chartContainer != null && this.chartContainer.getParent() != null) {
                ViewParent parent = this.chartContainer.getParent();
                for (int safetyCounter = 0; parent != null && !(parent instanceof HorizontalScrollView) && safetyCounter < 10; safetyCounter++) {
                    parent = parent.getParent();
                }
                if ((parent instanceof HorizontalScrollView) && (scrollView = (HorizontalScrollView) parent) != null && scrollView.getChildCount() > 0 && (child = scrollView.getChildAt(0)) != null) {
                    int scrollX = Math.max(0, child.getWidth() - scrollView.getWidth());
                    scrollView.smoothScrollTo(scrollX, 0);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "滚动到最新数据时发生错误", e);
        }
    }

    private void registerBatteryReceiver() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        try {
            if (getActivity() != null) {
                IntentFilter filter = new IntentFilter("android.intent.action.BATTERY_CHANGED");
                getActivity().registerReceiver(this.batteryReceiver, filter);
            }
        } catch (Exception e) {
            Log.e(TAG, "注册电池广播失败", e);
            showErrorDialog("广播注册失败", "无法监听电池状态变化：" + e.getMessage());
        }
    }

    private void setClickListeners(View view) {
        if (view == null) {
            return;
        }
        try {
            View btnMode = view.findViewById(R.id.btn_mode);
            View btnBattery = view.findViewById(R.id.btn_battery);
            if (btnMode != null) {
                btnMode.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda7
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        this.f$0.m107lambda$setClickListeners$9$comexamplekbatteryFragment1(view2);
                    }
                });
            }
            if (btnBattery != null) {
                btnBattery.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment1$$ExternalSyntheticLambda8
                    @Override // android.view.View.OnClickListener
                    public final void onClick(View view2) {
                        this.f$0.m106lambda$setClickListeners$10$comexamplekbatteryFragment1(view2);
                    }
                });
            }
        } catch (Exception e) {
            Log.e(TAG, "设置点击监听器时发生错误", e);
        }
    }

    /* renamed from: lambda$setClickListeners$9$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m107lambda$setClickListeners$9$comexamplekbatteryFragment1(View v) {
        try {
            if (isAdded() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                startActivity(new Intent(getActivity(), (Class<?>) ModeActivity.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "启动模式选择Activity失败", e);
        }
    }

    /* renamed from: lambda$setClickListeners$10$com-example-kbattery-Fragment1, reason: not valid java name */
    /* synthetic */ void m106lambda$setClickListeners$10$comexamplekbatteryFragment1(View v) {
        try {
            if (isAdded() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
                startActivity(new Intent(getActivity(), (Class<?>) BatteryActivity.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "启动电池信息Activity失败", e);
        }
    }

    private void showErrorDialog(String title, String message) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        if (getActivity() == null || !isAdded() || getActivity().isFinishing() || getActivity().isDestroyed()) {
            Log.w(TAG, "弹窗环境不安全，跳过显示：" + title);
        } else {
            MaterialAlertDialogBuilder builder = DialogUtils.createBlurInfoDialog(requireContext(), title, message, "确定", null).setCancelable(false);
            DialogUtils.showWithBlurEffect(builder);
        }
    }

    private int dpToPx(int dp) {
        Context context = getContext();
        return context == null ? dp : (int) TypedValue.applyDimension(1, dp, context.getResources().getDisplayMetrics());
    }

    public static class BatteryWorker extends Worker {
        private static final String CHART_DATA_KEY = "chart_data_list";
        private static final long INTERVAL_24_HOURS = 86400000;
        private static final String LAST_UPDATED_KEY = "last_updated_time";
        private static final int LOW_THRESHOLD = 20;
        private static final long MIN_RECORD_GAP = 60000;
        private static final String PREFS_NAME = "BatteryChartData";
        private static final int STATE_CHARGING = 1;
        private static final int STATE_LOW = 2;
        private static final int STATE_NORMAL = 0;
        private static final String TAG = "BatteryWorker";

        public BatteryWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override // androidx.work.Worker
        public ListenableWorker.Result doWork() {
            recordBatteryData();
            return ListenableWorker.Result.success();
        }

        private void recordBatteryData() {
            List<BatteryData> savedList;
            List<BatteryData> savedList2;
            Context context = getApplicationContext();
            Intent batteryIntent = context.registerReceiver(null, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
            if (batteryIntent == null) {
                return;
            }
            int level = batteryIntent.getIntExtra("level", -1);
            int scale = batteryIntent.getIntExtra("scale", -1);
            int currentPercent = (level == -1 || scale == -1) ? 50 : (level * 100) / scale;
            int state = getBatteryState(batteryIntent);
            long currentTime = System.currentTimeMillis();
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
            String json = prefs.getString(CHART_DATA_KEY, "");
            long lastRecordTime = prefs.getLong(LAST_UPDATED_KEY, 0L);
            if (currentTime - lastRecordTime < MIN_RECORD_GAP) {
                return;
            }
            Gson gson = new Gson();
            Type type = new TypeToken<List<BatteryData>>() { // from class: com.example.kbattery.Fragment1.BatteryWorker.1
            }.getType();
            List<BatteryData> chartDataList = new ArrayList<>();
            if (!json.isEmpty() && (savedList2 = (List) gson.fromJson(json, type)) != null) {
                List<BatteryData> chartDataList2 = filterExpiredData(savedList2);
                savedList = chartDataList2;
            } else {
                savedList = chartDataList;
            }
            savedList.add(new BatteryData(currentPercent, state, currentTime));
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(CHART_DATA_KEY, gson.toJson(savedList));
            editor.putLong(LAST_UPDATED_KEY, currentTime);
            editor.apply();
        }

        private int getBatteryState(Intent intent) {
            int plugged = intent.getIntExtra("plugged", 0);
            boolean isCharging = plugged != 0;
            int level = intent.getIntExtra("level", -1);
            int scale = intent.getIntExtra("scale", -1);
            int percent = (level == -1 || scale == -1) ? 100 : (level * 100) / scale;
            if (isCharging) {
                return 1;
            }
            return percent < 20 ? 2 : 0;
        }

        private List<BatteryData> filterExpiredData(List<BatteryData> dataList) {
            long currentTime = System.currentTimeMillis();
            List<BatteryData> validData = new ArrayList<>();
            for (BatteryData data : dataList) {
                if (currentTime - data.timestamp < INTERVAL_24_HOURS) {
                    validData.add(data);
                }
            }
            return validData;
        }
    }
}
