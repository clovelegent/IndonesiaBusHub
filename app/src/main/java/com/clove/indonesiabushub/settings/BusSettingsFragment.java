package com.clove.indonesiabushub.settings;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.clove.indonesiabushub.R;
import com.clove.indonesiabushub.dataprovider.BusLineContentProvider;
import com.clove.indonesiabushub.server.LocationService;
import com.clove.indonesiabushub.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BusSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String TAG = "BusSettingsFragment";

    FrameLayout frameLayout;
    public static final String KEY_LINES_CHOOSE = "key_lines_choose";
    public static final String KEY_LINES_CHOOSE_VALUE = "key_lines_choose_value";
    public static final String DEF_SP_NAME = "com.clove.indonesiabushub_preferences";
    private ListPreference line_pref;
    private ContentResolver mContentResolver = null;
    private Cursor mCursor = null;
    private ArrayList<String> AllLines = null;
    private static Context context;
    static SharedPreferences defSP =null;
    String prefDefValue = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        defSP = context.getSharedPreferences(DEF_SP_NAME,Context.MODE_PRIVATE);
        mContentResolver = getActivity().getContentResolver();
        AllLines = new ArrayList<>();
        frameLayout = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        frameLayout.setBackgroundColor(getActivity().getResources().getColor(R.color.colorWhite));
        frameLayout.setVisibility(View.VISIBLE);
        addPreferencesFromResource(R.xml.pref_bus_setting);

        mCursor = mContentResolver.query(BusLineContentProvider.LINES_CONTENT_URI, null, null, null, null);
        while(mCursor.moveToNext()){
            String currentLines = mCursor.getString(mCursor.getColumnIndex("line"));
            Log.d(TAG,currentLines);
            if (AllLines.size() == 0){
                AllLines.add(currentLines);
            }else if (!AllLines.contains(currentLines)){
                AllLines.add(currentLines);
            }
        }

        Log.d(TAG,"all lines"+AllLines.toArray(new String[0]).toString());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        line_pref = (ListPreference)findPreference(KEY_LINES_CHOOSE);
        line_pref.setOnPreferenceChangeListener(this);
        line_pref.setEntries(AllLines.toArray(new String[0]));
        line_pref.setEntryValues(AllLines.toArray(new String[0]));
        String value = getPrefValue(KEY_LINES_CHOOSE_VALUE,prefDefValue);
        if (value.equals(prefDefValue)){
            line_pref.setSummary("请选择行驶路线");
        }else{
            line_pref.setSummary("正在行驶路线："+value);
        }
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String preferenceKey = preference.getKey();
        Log.d(TAG, "preferenceKey = " + preferenceKey + ",newValue = " + newValue);

        if (preferenceKey.equals(KEY_LINES_CHOOSE)){
            line_pref.setSummary("正在行驶路线："+(String)newValue);
            SharedPreferences sp = line_pref.getSharedPreferences();
            SharedPreferences.Editor editor = sp.edit();
            editor.putString(KEY_LINES_CHOOSE_VALUE,(String)newValue);
            editor.commit();
            Utils.updateCurrentBusLineStation(context,context.getContentResolver());
        }

        return false;
    }

    public String getPrefValue(String key,String defValue){
        String value = defSP.getString(key,defValue);
        return value;
    }
}
