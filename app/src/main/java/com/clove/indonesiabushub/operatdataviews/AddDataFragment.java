package com.clove.indonesiabushub.operatdataviews;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.os.IBinder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.clove.indonesiabushub.R;
import com.clove.indonesiabushub.server.LocationService;
import com.clove.indonesiabushub.utils.Utils;

import org.w3c.dom.Text;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AddDataFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AddDataFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AddDataFragment extends Fragment implements View.OnClickListener,LocationService.OnUpdateLocationViewListener {
    private static final String TAG = "AddDataFragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "operator_type";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private int myOperatorType;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private TextView latitudeValue;
    private TextView longitudeValue;
    private EditText locationValue;
    private EditText lineValue;
    private Button cancelAdd;
    private Button confirmAdd;

    private Context context;

    private ContentResolver mContentResolver;
    private static final String AUTHORITY = "com.clove.indonesiabushub.buslinecontentprovider";
    private static final Uri LINES_CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/lines");

    private Location mLocation;
    private LocationService mLocationService = null;

    public AddDataFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AddDataFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AddDataFragment newInstance(int param1, String param2) {
        AddDataFragment fragment = new AddDataFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        mContentResolver = getActivity().getContentResolver();
        if (getArguments() != null) {
            myOperatorType = getArguments().getInt(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        if (mLocationService == null) {
            Intent intent = new Intent(context, LocationService.class);
            if (null == context.startService(intent)) {
                getActivity().finish();
                return;
            }

            if (!context.bindService(intent, mSConn, context.BIND_AUTO_CREATE)) {
                getActivity().finish();
                return;
            }
        } else {
            initWhenHaveService();
        }
    }

    private void initWhenHaveService() {
        mLocationService.setOnUpdateLocationViewListener(this);
    }

    private ServiceConnection mSConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            LocationService.LocationServerBinder mSSRBinder = (LocationService.LocationServerBinder) service;
            mLocationService = mSSRBinder.getService();
            initWhenHaveService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mLocationService = null;
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_data, container, false);
        latitudeValue = (TextView) view.findViewById(R.id.latitude_value);
        longitudeValue = (TextView) view.findViewById(R.id.longitude_value);
        locationValue = (EditText) view.findViewById(R.id.location_value);
        lineValue = (EditText) view.findViewById(R.id.line_value);
        cancelAdd = (Button) view.findViewById(R.id.cancel_add);
        confirmAdd = (Button) view.findViewById(R.id.confirm_add);
        cancelAdd.setOnClickListener(this);
        confirmAdd.setOnClickListener(this);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.unbindService(mSConn);
        getActivity().finish();
        mListener = null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.cancel_add:
                getActivity().finish();
                break;
            case R.id.confirm_add:
                //获取位置信息 加入数据库
                double latitude;
                double longitude;
                if(mLocation!=null){
                    latitude = mLocation.getLatitude();
                    longitude = mLocation.getLongitude();
                }else{
                     latitude = Double.parseDouble(latitudeValue.getText().toString());
                     longitude = Double.parseDouble(longitudeValue.getText().toString());
                }
                String line = lineValue.getText().toString();
                String location = locationValue.getText().toString();
                if(line == null || location == null){
                    Toast.makeText(context,context.getResources().getString(R.string.empty_notify),Toast.LENGTH_SHORT).show();
                    return;
                }else if(line.trim().equals("") || location.trim().equals("")){
                    Toast.makeText(context,context.getResources().getString(R.string.empty_notify),Toast.LENGTH_SHORT).show();
                    return;
                }
                //公交站点格式为：line:location --> 线路站点唯一存在，防止不同线路同一站点混淆
                location = line+":"+location;
                Log.d(TAG,"latitude:"+latitude+",longitude"+longitude+",location"+location+",line:"+line);
                if (insertValue(line,location,latitude,longitude) != null){
                    Utils.updateCurrentBusLineStation(context,mContentResolver);
                    locationValue.setText("");
                    Toast.makeText(context,context.getResources().getString(R.string.add_success),Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }

    public Uri insertValue(String line,String name,double latitude,double longitude) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("line",line);
        contentValues.put("name", name);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        return mContentResolver.insert(LINES_CONTENT_URI,contentValues);
    }

    @Override
    public void updateLocationView(Location locationInfo) {
        mLocation = locationInfo;
        longitudeValue.setText(locationInfo.getLongitude()+"");
        latitudeValue.setText(locationInfo.getLatitude()+"");
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
