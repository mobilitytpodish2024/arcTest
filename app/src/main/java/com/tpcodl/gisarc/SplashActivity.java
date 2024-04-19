package com.tpcodl.gisarc;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;

import com.tpcodl.gisarc.prefrences.PreferenceHandler;
import com.tpcodl.gisarc.utils.ActivityUtils;


public class SplashActivity extends AppCompatActivity {
    private Context mContext;
    private PreferenceHandler phalder;
    private ActivityUtils util;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        phalder = new PreferenceHandler(this);
        util = ActivityUtils.getInstance();
        mContext = this;
        redirectToNextPage();
    }

    private void redirectToNextPage() {
        new Handler().postDelayed(this::checkLoginstatus, 2000);
    }



    private void checkLoginstatus() {
        phalder.getLoginData();
        startActivity(new Intent(this, LoginActivity.class));
        finish();

     /*   if (!TextUtils.isEmpty(util.getServreDate())) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        } else {
            startActivity(new Intent(this, UserVerification.class));
            finish();
        }*/
    }
}