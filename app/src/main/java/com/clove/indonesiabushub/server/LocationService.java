package com.clove.indonesiabushub.server;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.clove.indonesiabushub.BusStation;
import com.clove.indonesiabushub.NavigationDrawerActivity;
import com.clove.indonesiabushub.R;
import com.clove.indonesiabushub.dataprovider.BusLineContentProvider;
import com.clove.indonesiabushub.operatdataviews.OperatorDataActivity;
import com.clove.indonesiabushub.settings.BusSettingsFragment;
import com.clove.indonesiabushub.settings.SettingsActivity;
import com.clove.indonesiabushub.utils.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationService extends Service {

    private static final String TAG = "LocationService";
    private LocationManager locationManager;
    private String locationProvider;
    private ContentResolver mContentResolver = null;
    private Cursor mCursor = null;
    public static List<BusStation> allBusStation = null;

    private TextToSpeech mSpeech;

    //位置信息改变回调接口
    private OnUpdateLocationViewListener onUpdateLocationViewListener = null;
    private OnArrivedListener onArrivedListener = null;

    Location location;
    private Handler mhadler = new Handler();

    /**
     * 没有获取到位置信息将会每隔3秒获取一次
     */
    private Runnable mGetLocationRunnable = new Runnable() {
        @Override
        public void run() {
            if (ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(LocationService.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            location = locationManager.getLastKnownLocation(locationProvider);
            if (location != null) {
                Log.d(TAG, "onCreate: location");
                //不为空,显示地理位置经纬度
                if (onUpdateLocationViewListener!=null){
                    onUpdateLocationViewListener.updateLocationView(location);
                }

                showLocation(location);
                mhadler.removeCallbacks(mGetLocationRunnable);
            }else{
                //为空，等待三秒获取位置
                mhadler.postDelayed(mGetLocationRunnable,3000);
            }
        }
    };

    HandlerThread checkArriveThread = null;
    Handler checkArriveHandler = null;

    public LocationService() {
    }

    private LocationServerBinder lsBinder = new LocationServerBinder();
    @Override
    public IBinder onBind(Intent intent) {
        return lsBinder;
    }

    public class LocationServerBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        checkArriveThread = new HandlerThread("check arrive thread");
        checkArriveThread.start();

        checkArriveHandler = new Handler(checkArriveThread.getLooper()){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what == 1){
                    Log.d(TAG,"checkArriveThread handler location changed");
                    if (allBusStation!=null){
                        for (int i = 0;i < allBusStation.size();i++ ){
                            if(allBusStation.get(i).arriveStation(location.getLatitude(),location.getLongitude())){
                                if (!allBusStation.get(i).isArrived()){
                                    allBusStation.get(i).setArrived(true);
                                    String name = Utils.getStationName(allBusStation.get(i).getName());
                                    Log.d(TAG,"checkArriveHandler station name :"+name);
                                    playTTS(name);
                                    onArrivedListener.updateArriveStatus();
                                }
                            }
                        }
                    }
                }
            }
        };

        speechInit();
        locationManager = (LocationManager) getSystemService(getApplicationContext().LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        //criteria.setAccuracy(Criteria.ACCURACY_COARSE);//低精度，如果设置为高精度，依然获取不了location。
        criteria.setAccuracy(Criteria.ACCURACY_FINE);//高精度
        criteria.setAltitudeRequired(false);//不要求海拔
        criteria.setBearingRequired(false);//不要求方位
        criteria.setCostAllowed(true);//允许有花费
        criteria.setPowerRequirement(Criteria.POWER_LOW);//低功耗

        //从可用的位置提供器中，匹配以上标准的最佳提供器
        locationProvider = locationManager.getBestProvider(criteria, true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = locationManager.getLastKnownLocation(locationProvider);
        Log.d(TAG, "onCreate: " + (location == null) + "..");
        if (location != null) {
            Log.d(TAG, "onCreate: location");
            if (onUpdateLocationViewListener!=null){
                onUpdateLocationViewListener.updateLocationView(location);
            }
            showLocation(location);
        }else{
            Log.d(TAG, "onCreate: location == null");
            mhadler.postDelayed(mGetLocationRunnable,3000);
        }
        //监视地理位置变化
        locationManager.requestLocationUpdates(locationProvider, 0, 0, locationListener);

        mContentResolver = getContentResolver();
        String line = getSharedPreferences(BusSettingsFragment.DEF_SP_NAME, Context.MODE_PRIVATE).getString(BusSettingsFragment.KEY_LINES_CHOOSE_VALUE,"");
        Log.d(TAG,"select line :"+line);
        if (line != null){
            if (!line.trim().equals("")){
                allBusStation = new ArrayList<>();
                mCursor = mContentResolver.query(BusLineContentProvider.LINES_CONTENT_URI, null, "line=?", new String[]{line}, null);
                while(mCursor.moveToNext()){
                    String mLine = mCursor.getString(mCursor.getColumnIndex("line"));
                    String mName = mCursor.getString(mCursor.getColumnIndex("name"));
                    double mlatitude = mCursor.getDouble(mCursor.getColumnIndex("latitude"));
                    double mLongitude = mCursor.getDouble(mCursor.getColumnIndex("longitude"));
                    BusStation busStation = new BusStation(mLine,mName,mlatitude,mLongitude);
                    Log.d(TAG,busStation.toString());
                    allBusStation.add(busStation);
                }
            }else/*没有选择行车路线*/{

            }
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        mhadler.removeCallbacks(mGetLocationRunnable);
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
        super.onDestroy();
    }

    /**
     * LocationListern监听器
     * 参数：地理位置提供器、监听位置变化的时间间隔、位置变化的距离间隔、LocationListener监听器
     */

    LocationListener locationListener = new LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled: " + provider + ".." + Thread.currentThread().getName());
        }

        @Override
        public void onLocationChanged(Location location) {
            LocationService.this.location = location;
            checkArriveHandler.sendEmptyMessage(1);
            Log.d(TAG, "onLocationChanged: " + ".." + Thread.currentThread().getName());
            //如果位置发生变化,重新显示
            if (onUpdateLocationViewListener!=null){
                onUpdateLocationViewListener.updateLocationView(location);
            }
            showLocation(location);
        }
    };

    private void showLocation(Location location) {
        Log.d(TAG,"定位成功------->"+"location------>经度为：" + location.getLatitude() + "\n纬度为" + location.getLongitude());
    }

    /**
     * 初始化TextToSpeech，在onCreate中调用
     */
    private void speechInit() {
        if (mSpeech != null) {
            mSpeech.stop();
            mSpeech.shutdown();
            mSpeech = null;
        }
        // 创建TTS对象
        mSpeech = new TextToSpeech(LocationService.this, new TTSListener());
        // 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
        mSpeech.setPitch(1.0f);
        // 设置语速
        mSpeech.setSpeechRate(1.0f);
    }

    /**
     * 将文本用TTS播放
     *
     * @param str 播放的文本内容
     */
    private void playTTS(String str) {
        if (mSpeech == null) mSpeech = new TextToSpeech(this, new TTSListener());
        mSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null);
        Log.i(TAG, "播放语音为：" + str);
    }

    private final class TTSListener implements TextToSpeech.OnInitListener {
        @Override
        public void onInit(int status) {
            Log.e(TAG, "初始化结果：" + (status == TextToSpeech.SUCCESS));
            int result = mSpeech.setLanguage(Locale.ENGLISH);
            //如果返回值为-2，说明不支持这种语言
            Log.e(TAG, "是否支持该语言：" + (result != TextToSpeech.LANG_NOT_SUPPORTED));
        }
    }

    public interface OnUpdateLocationViewListener {
        void updateLocationView(Location locationInfo);
    }

    public void setOnUpdateLocationViewListener(OnUpdateLocationViewListener listener){
        onUpdateLocationViewListener = listener;
    }

    public interface OnArrivedListener{
        void updateArriveStatus();
    }

    public void setOnArrivedListener(OnArrivedListener listener){
        onArrivedListener = listener;
    }

}
