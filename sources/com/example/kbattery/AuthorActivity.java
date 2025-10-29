package com.example.kbattery;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.card.MaterialCardView;

/* loaded from: classes3.dex */
public class AuthorActivity extends AppCompatActivity {
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
        setContentView(R.layout.activity_author);
        MaterialCardView card1 = (MaterialCardView) findViewById(R.id.author_card_1);
        MaterialCardView card2 = (MaterialCardView) findViewById(R.id.author_card_2);
        MaterialCardView card3 = (MaterialCardView) findViewById(R.id.author_card_3);
        MaterialCardView card4 = (MaterialCardView) findViewById(R.id.author_card_4);
        if (card1 != null) {
            card1.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.AuthorActivity.1
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    Intent coolApkIntent = new Intent("android.intent.action.VIEW", Uri.parse("http://www.coolapk.com/u/29253237"));
                    coolApkIntent.setPackage("com.coolapk.market");
                    if (coolApkIntent.resolveActivity(AuthorActivity.this.getPackageManager()) != null) {
                        AuthorActivity.this.startActivity(coolApkIntent);
                    } else {
                        Toast.makeText(AuthorActivity.this, "请先安装酷安APP", 0).show();
                    }
                }
            });
        }
        if (card2 != null) {
            card2.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.AuthorActivity.2
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    AuthorActivity.this.showToast("不想见到你，略略略");
                }
            });
        }
        if (card3 != null) {
            card3.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.AuthorActivity.3
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    AuthorActivity.this.openUrl("https://qm.qq.com/q/WrcymbcamO");
                }
            });
        }
        if (card4 != null) {
            card4.setOnClickListener(new View.OnClickListener() { // from class: com.example.kbattery.AuthorActivity.4
                @Override // android.view.View.OnClickListener
                public void onClick(View v) {
                    AuthorActivity.this.openUrl("https://v.douyin.com/UcfkEAa/");
                }
            });
        }
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void showToast(String message) {
        Toast.makeText(this, message, 0).show();
    }

    /* JADX INFO: Access modifiers changed from: private */
    public void openUrl(String url) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
