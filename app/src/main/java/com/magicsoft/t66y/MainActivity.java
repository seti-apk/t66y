package com.magicsoft.t66y;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.gms.ads.MobileAds;
import com.magicsoft.t66y.page.CLPage;

public class MainActivity extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final String HOMEPAGE = CLPage.HOSTNAME + "index.php";
        MobileAds.initialize(this, "ca-app-pub-8231063758677606/9898844435");

//*
        Intent intent = new Intent(this, CLWebViewActivity.class);
        intent.putExtra("url", HOMEPAGE);
/*/
        Intent intent = new Intent(this, ImageViewActivity.class);
        intent.putExtra("src", CLPage.HOSTNAME);
//*/
        startActivity(intent);
        finish();
    }
}
