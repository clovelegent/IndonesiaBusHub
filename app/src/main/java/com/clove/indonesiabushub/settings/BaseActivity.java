package com.clove.indonesiabushub.settings;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;

import com.clove.indonesiabushub.R;

public abstract class BaseActivity extends Activity{

    @Override
    public void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(getResources().getColor(R.color.colorBackground));
        requestWindowFeature(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setMyContentView();
        initViewRes();
    }

    abstract void initViewRes();

    abstract void setMyContentView();
}
