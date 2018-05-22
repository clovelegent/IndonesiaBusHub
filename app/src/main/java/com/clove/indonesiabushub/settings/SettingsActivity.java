package com.clove.indonesiabushub.settings;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.clove.indonesiabushub.R;

public class SettingsActivity extends BaseActivity implements View.OnClickListener{

    private static final String TAG = "SettingsActivity";

    public static final String WITCH_SETTINGS = "witch_settings";

    LinearLayout pwdll;
    LinearLayout mediall;
    FragmentManager fragmentManager;
    ModifyPasswordFragment mpFragment;
    BusSettingsFragment busSettingsFragment;

    @Override
    void initViewRes() {
        fragmentManager = getFragmentManager();
        pwdll = (LinearLayout) findViewById(R.id.setting_pwd_ll);
        mediall = (LinearLayout) findViewById(R.id.setting_bus_ll);
        pwdll.setOnClickListener(this);
        mediall.setOnClickListener(this);
        int lunch = getIntent().getIntExtra(WITCH_SETTINGS,0);
        if(lunch == 1){
            busSettingsFragment = new BusSettingsFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container,busSettingsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    @Override
    void setMyContentView() {
        setContentView(R.layout.settings_activity);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.setting_pwd_ll){
            mpFragment = new ModifyPasswordFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container,mpFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }else if(v.getId() == R.id.setting_bus_ll){
            Log.d(TAG,"onClick...setting_bus_ll");
            busSettingsFragment = new BusSettingsFragment();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragment_container,busSettingsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
