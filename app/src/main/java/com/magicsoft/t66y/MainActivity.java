package com.magicsoft.t66y;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.google.android.gms.ads.MobileAds;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        MobileAds.initialize(this, "ca-app-pub-8231063758677606~5548535885");

        Intent intent = new Intent(this, EntryActivity.class);
        startActivity(intent);

        finish();
    }
}
