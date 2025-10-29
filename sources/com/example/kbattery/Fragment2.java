package com.example.kbattery;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import com.example.kbattery.utils.DialogUtils;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import java.lang.reflect.InvocationTargetException;

/* loaded from: classes3.dex */
public class Fragment2 extends Fragment {
    @Override // androidx.fragment.app.Fragment
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_2, container, false);
        initCardClicks(view);
        return view;
    }

    private void initCardClicks(View view) {
        MaterialCardView authorCard = (MaterialCardView) view.findViewById(R.id.author_card);
        authorCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda0
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m108lambda$initCardClicks$0$comexamplekbatteryFragment2(view2);
            }
        });
        MaterialCardView donateCard = (MaterialCardView) view.findViewById(R.id.donate_card);
        donateCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda1
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m109lambda$initCardClicks$1$comexamplekbatteryFragment2(view2);
            }
        });
        MaterialCardView manualCard = (MaterialCardView) view.findViewById(R.id.manual_card);
        manualCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda2
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m110lambda$initCardClicks$2$comexamplekbatteryFragment2(view2);
            }
        });
        MaterialCardView feedbackCard = (MaterialCardView) view.findViewById(R.id.feedback_card);
        feedbackCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda3
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m111lambda$initCardClicks$3$comexamplekbatteryFragment2(view2);
            }
        });
        MaterialCardView qqGroupCard = (MaterialCardView) view.findViewById(R.id.qq_group_card);
        qqGroupCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda4
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) {
                this.f$0.m112lambda$initCardClicks$4$comexamplekbatteryFragment2(view2);
            }
        });
        MaterialCardView updateLogCard = (MaterialCardView) view.findViewById(R.id.update_log_card);
        updateLogCard.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda5
            @Override // android.view.View.OnClickListener
            public final void onClick(View view2) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
                this.f$0.m113lambda$initCardClicks$5$comexamplekbatteryFragment2(view2);
            }
        });
    }

    /* renamed from: lambda$initCardClicks$0$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m108lambda$initCardClicks$0$comexamplekbatteryFragment2(View v) {
        Intent intent = new Intent(requireActivity(), (Class<?>) AuthorActivity.class);
        startActivity(intent);
    }

    /* renamed from: lambda$initCardClicks$1$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m109lambda$initCardClicks$1$comexamplekbatteryFragment2(View v) {
        openUrl("https://afdian.com/a/1655064luobo");
    }

    /* renamed from: lambda$initCardClicks$2$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m110lambda$initCardClicks$2$comexamplekbatteryFragment2(View v) {
        openUrl("https://kbattery.mtaokj.com/");
    }

    /* renamed from: lambda$initCardClicks$3$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m111lambda$initCardClicks$3$comexamplekbatteryFragment2(View v) {
        openUrl("https://qm.qq.com/q/UnMxmdhQc0");
    }

    /* renamed from: lambda$initCardClicks$4$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m112lambda$initCardClicks$4$comexamplekbatteryFragment2(View v) {
        openUrl("https://qun.qq.com/universal-share/share?ac=1&authKey=wUhO9fhXM9qpGpfBfMQ17IO0FdwYn8jtGnBtz7sGakkAUx2aYLrbznnzIVx%2FpSr8&busi_data=eyJncm91cENvZGUiOiI5ODI2ODYzMjMiLCJ0b2tlbiI6IklEVDI3NjJyODFQVVlKWUlvZ0JCTjhWR1czbGhXaFdLWitVeEM4WHBBQnUvUk5ESkFnWFEvMHNHaGZrSVN4S2MiLCJ1aW4iOiI5NTk0MDIxNzEifQ%3D%3D&data=t_w07oPpca--ExrkLmmQYLSOGi7vskeIPb86fzGYwfsQeBmLYY75poXgaMtlWjX-91oXHF3HyxdsmyaX0MgL6A&svctype=4&tempid=h5_group_info");
    }

    /* renamed from: lambda$initCardClicks$5$com-example-kbattery-Fragment2, reason: not valid java name */
    /* synthetic */ void m113lambda$initCardClicks$5$comexamplekbatteryFragment2(View v) throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        showUpdateLogDialog();
    }

    private void showUpdateLogDialog() throws IllegalAccessException, NoSuchMethodException, SecurityException, IllegalArgumentException, InvocationTargetException {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext()).setTitle((CharSequence) "更新日志").setMessage((CharSequence) "1. v1.0（初始版本）\n• 全新Kbattery上线，含续航脚本2.4和还原脚本\n• 不支持系统优化类功能，关闭下载渠道（需私信获取）\n\n2. v1.1\n• 续航脚本升级至2.7，新增强制编译、防冲突指令\n• 修复冲突问题，计划加入还原功能、UI优化等\n• 推荐搭配润子流畅优化脚本使用（需三连+私信获取）\n\n3. v1.2（APP搭建版）\n• 完成APP搭建，待整合脚本\n• 计划11.15爱发电上架，11.20酷安上架\n• 支持脚本路径操作（启动/省电/还原）\n\n4. v1.3（Kbattery-S初期版）\n• 新增二级界面和远程更新功能\n• 基层开发由他人完成，UI/脚本@可白，网站@覆汐\n\n5. v1.4（Kbattery-S）\n• 状态栏适配不同设备，新增说明弹窗\n• 简化主页面UI，美化图标\n• 提供官网和网盘下载（更新前需备份）\n\n6. v1.4.1（测试版）\n• 新增光线追踪、FALCON引擎、V-sync帧同步\n• 优化UI，修复爆红问题，新增首次使用引导\n\n7. v1.4.2\n• 优化日志和弹窗显示，调整引导内容\n• 优化省电/虚拟显卡脚本，需安装shizukurunner\n\n8. v1.5\n• 新增净化系统功能（冻结冗余程序，部分机型不支持）\n• 增加脚本防冲突，优化CPU/GPU调度\n\n9. v1.5.1\n• 重构虚拟显卡脚本并加防冲突\n• 优化关于界面，开始转换平台开发\n\n10. v1.6\n• 新增monster+模式（获泊莹授权）、系统省电\n• 优化虚拟显卡/省电脚本，修复爆红问题\n\n11. v1.7\n• 下移导航栏适配操作习惯，更新弹窗加云端内容\n• 去广告，新增纯净状态栏，计划后续开源至github\n\n12. v1.8\n• 完善功能模块（性能模式/系统省电/虚拟显卡）\n• 新增官方群聊、官网、捐赠入口，开发者因中考暂停更新\n\n13. v1.9（正式版）\n• 加入脚本运行状态指示器（绿色小点=shizuku激活）\n\n14. v1.10（更名Kbattery）\n• 完成平台转换与基层重构\n• 新增硬件增强、多页面拓展、交互升级\n\n15. v1.11\n• 推出满血版功能（游戏优化/直驱供电/省电加强）\n• 免费无广告，支持加入交流群\n\n16. v1.12\n• 首发OS6帧率优化，升级UI美化\n• 重制续航页面，续航提升23%，修复ColorOS插件报错\n\n17. v1.17\n• 界面改用圆角设计，新增A+智能调度\n• 修复省电脚本异常，整合游戏魔盒，引入OPPO墓碑机制\n\n18. v1.18\n· 全局插帧，画面丝滑无卡顿\n· 高速充电提速，应急补电更稳\n· WLAN优化，弱网少断、功耗更低\n· 电量柱状图，耗电细节一键看\n· 导航栏新增，功能找起来更顺手\n· 模式脚本重构，老机型也流畅\n· A + 调度加速，操作响应秒级反馈\n· 首页加更新日志按钮，新功能早知道\n· 底层交互大改，按钮 / 步骤更顺手\n· 旧界面整合，布局清爽不杂乱\n\n19. V1.18.1\n· 修复 shizuku未激活闪退went\n· 修复 模式脚本还原问题\n· 修复 直驱无法关闭问题\n· 修复 WLAN问题\n\n20. V1.18.2\n· 修复 游戏优化无法生效问题\n· 修复 游戏优化无法还原问题\n· 软件进入缓慢更新状态\n· 更新 A+调度\n· 更新性能模式脚本\n· 增强 游戏优化\n· 优化 部分细节显示\n\n").setPositiveButton((CharSequence) "我知道了", new DialogInterface.OnClickListener() { // from class: com.example.kbattery.Fragment2$$ExternalSyntheticLambda6
            @Override // android.content.DialogInterface.OnClickListener
            public final void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        DialogUtils.showWithBlurEffect(builder);
    }

    private void openUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            Intent intent = new Intent("android.intent.action.VIEW", uri);
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
