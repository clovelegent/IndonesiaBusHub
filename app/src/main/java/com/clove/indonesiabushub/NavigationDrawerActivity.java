package com.clove.indonesiabushub;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.clove.indonesiabushub.dataprovider.BusLineContentProvider;
import com.clove.indonesiabushub.operatdataviews.OperatorDataActivity;
import com.clove.indonesiabushub.server.LocationService;
import com.clove.indonesiabushub.settings.SettingsActivity;
import com.clove.indonesiabushub.utils.Utils;

public class NavigationDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener , LocationService.OnUpdateLocationViewListener
        ,LocationService.OnArrivedListener,View.OnClickListener{
    private static final String TAG = "NavigationDrawer";

    public static final int DATA_OPERATOR_TYPE_IMPORT = 1;
    public static final int DATA_OPERATOR_TYPE_OUTPUT = 2;
    public static final int DATA_OPERATOR_TYPE_DELETE = 3;
    public static final String DATA_OPERATOR_TYPE = "data_operator_type";

    private LocationService mLocationService = null;

    private TextView firstUseNofity;
    private ListView lineDescriptionList;
    private LinearLayout navigationLL;
    private TextView lineIndicateTV;
    private LineStationAdapter lineStationAdapter;
    private Button reStart ;

    private ContentResolver mContentResolver = null;
    private Cursor mCursor = null;

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 1){
                if (lineStationAdapter == null){
                    if (LocationService.allBusStation != null){
                        lineStationAdapter = new LineStationAdapter(NavigationDrawerActivity.this,LocationService.allBusStation);
                    }
                }
                lineStationAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mContentResolver = getContentResolver();

       /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        firstUseNofity = (TextView) findViewById(R.id.first_use_notify);
        lineDescriptionList = (ListView) findViewById(R.id.line_description);
        navigationLL = (LinearLayout) findViewById(R.id.navigation_ll);
        lineIndicateTV = (TextView) findViewById(R.id.line_indicate_tv);
        reStart = (Button)findViewById(R.id.restart);
        reStart.setOnClickListener(this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        if (mLocationService == null) {
            Intent intent = new Intent(this, LocationService.class);
            if (null == startService(intent)) {
                finish();
                return;
            }

            if (!bindService(intent, mSConn, BIND_AUTO_CREATE)) {
                finish();
                return;
            }
        } else {
            initWhenHaveService();
        }

        lineIndicateTV.setText(Utils.getLine(this));

        if (LocationService.allBusStation != null){
            if(LocationService.allBusStation.size()>0){
                firstUseNofity.setVisibility(View.GONE);
                if (lineStationAdapter == null){
                    lineStationAdapter = new LineStationAdapter(NavigationDrawerActivity.this,LocationService.allBusStation);
                }
            }
        }

        if (lineStationAdapter != null){
            lineDescriptionList.setAdapter(lineStationAdapter);
        }


        String line = Utils.getLine(this);
        if(line == null || line.trim().equals("")){
            mCursor = mContentResolver.query(BusLineContentProvider.LINES_CONTENT_URI, null, null, null, null);
            if (!mCursor.moveToFirst())/*数据库没有数据*/{
                //进入导入数据界面
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("导入数据");
                alertDialogBuilder.setMessage("数据库为空，请导入数据");
                alertDialogBuilder.setPositiveButton("确定", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(NavigationDrawerActivity.this, OperatorDataActivity.class);
                                intent.putExtra(NavigationDrawerActivity.DATA_OPERATOR_TYPE,1);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                AlertDialog ad = alertDialogBuilder.create();
                ad.setCanceledOnTouchOutside(false); //点击外面区域不会让dialog消失
                ad.show();
            }else{
                //进入设置行车路线界面
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
                alertDialogBuilder.setTitle("设置路线");
                alertDialogBuilder.setMessage("请设置行车路线");
                alertDialogBuilder.setPositiveButton("确定", new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(NavigationDrawerActivity.this, SettingsActivity.class);
                                intent.putExtra(SettingsActivity.WITCH_SETTINGS,1);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                AlertDialog ad = alertDialogBuilder.create();
                ad.setCanceledOnTouchOutside(false); //点击外面区域不会让dialog消失
                ad.show();
            }
        }

    }

    private ServiceConnection mSConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (LocationService.allBusStation != null){
                if(LocationService.allBusStation.size()>0){
                    firstUseNofity.setVisibility(View.GONE);
                    lineStationAdapter = new LineStationAdapter(NavigationDrawerActivity.this,LocationService.allBusStation);
                    lineDescriptionList.setAdapter(lineStationAdapter);
                }
            }
            LocationService.LocationServerBinder mSSRBinder = (LocationService.LocationServerBinder) service;
            mLocationService = mSSRBinder.getService();
            initWhenHaveService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
        }
    };

    private void initWhenHaveService() {
        mLocationService.setOnUpdateLocationViewListener(this);
        mLocationService.setOnArrivedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.navigation_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        Log.d(TAG,"onOptionsItemSelected.....id = "+id);

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {


        // Handle navigation view item clicks here.
        int id = item.getItemId();
        Log.d(TAG,"onNavigationItemSelected.....id = "+id);

        if (id == R.id.nav_import) {
            startOperatorActivity(DATA_OPERATOR_TYPE_IMPORT);
        } else if (id == R.id.nav_output) {
            startOperatorActivity(DATA_OPERATOR_TYPE_OUTPUT);
        } /*else if (id == R.id.nav_slideshow) {

        }*/ else if (id == R.id.nav_delete) {
            startOperatorActivity(DATA_OPERATOR_TYPE_DELETE);
        } else if (id == R.id.nav_share) {

        } /*else if (id == R.id.nav_send) {

        }*/

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void startOperatorActivity(int operatorType) {
        Intent intent = new Intent(this, OperatorDataActivity.class);
        intent.putExtra(DATA_OPERATOR_TYPE,operatorType);
        startActivity(intent);
    }

    @Override
    public void updateLocationView(Location locationInfo) {
        Log.d(TAG,"NavigationDrawerActivity:"+locationInfo.getLongitude()+","+locationInfo.getLatitude());
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        //stopService(new Intent(this,LocationService.class));
        super.unbindService(conn);
    }

    @Override
    protected void onDestroy() {
        unbindService(mSConn);
        super.onDestroy();
    }

    @Override
    public void updateArriveStatus() {
        handler.sendEmptyMessage(1);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.restart:
                if (LocationService.allBusStation!=null){
                    for (int i= 0;i<LocationService.allBusStation.size();i++){
                        LocationService.allBusStation.get(i).setArrived(false);
                    }
                    lineStationAdapter.notifyDataSetChanged();
                }
                break;
            default:
                break;
        }
    }
}
