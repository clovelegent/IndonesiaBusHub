package com.clove.indonesiabushub.operatdataviews;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.clove.indonesiabushub.NavigationDrawerActivity;
import com.clove.indonesiabushub.R;

public class OperatorDataActivity extends AppCompatActivity implements AddDataFragment.OnFragmentInteractionListener {
    private static final String TAG = "DataOperator";
    private static int CURRENT_DATA_OPERATOR_TYPE = NavigationDrawerActivity.DATA_OPERATOR_TYPE_IMPORT;

    private FragmentManager mFragmentManager;
    private FragmentTransaction fragmentTransaction;
    private AddDataFragment addDataFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mFragmentManager = getFragmentManager();
        Intent intent = getIntent();
        CURRENT_DATA_OPERATOR_TYPE = intent.getIntExtra(NavigationDrawerActivity.DATA_OPERATOR_TYPE, CURRENT_DATA_OPERATOR_TYPE);
        setContentView(R.layout.activity_data_operator);

        switch (CURRENT_DATA_OPERATOR_TYPE) {
            case NavigationDrawerActivity.DATA_OPERATOR_TYPE_IMPORT:
                startAddDataFragment();
                break;
            case NavigationDrawerActivity.DATA_OPERATOR_TYPE_OUTPUT:
                break;
            case NavigationDrawerActivity.DATA_OPERATOR_TYPE_DELETE:
                break;
            default:
                finish();
                break;
        }
    }

    private void startAddDataFragment() {
        addDataFragment = AddDataFragment.newInstance(1,"");
        fragmentTransaction = mFragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.operator_content, addDataFragment, addDataFragment.getClass().getName());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}
