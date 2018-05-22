package com.clove.indonesiabushub.settings;

import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.clove.indonesiabushub.R;

public class ModifyPasswordFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "ModifyPasswordFragment";

    static final String PWD_SP_NAME = "password_sp_name";
    static final String MY_PASSWORD = "my_password";
    EditText resetPwd;
    EditText confirmPwd;
    Button confirm;
    SharedPreferences sharedPreferences;
    Context context;
    String resetText = null;
    String confirmText = null;

    FrameLayout frameLayout;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity();
        sharedPreferences = getActivity().getSharedPreferences(PWD_SP_NAME,Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.modefy_password_fragment, null);
        resetPwd = (EditText) view.findViewById(R.id.reset_password_view);
        confirmPwd = (EditText)view.findViewById(R.id.confirm_password_view);
        confirm = (Button) view.findViewById(R.id.modify_confirm_view);
        frameLayout = (FrameLayout) getActivity().findViewById(R.id.fragment_container);
        frameLayout.setVisibility(View.VISIBLE);
        confirm.setOnClickListener(this);
        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        frameLayout.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.modify_confirm_view) {
            resetText = resetPwd.getText().toString();
            confirmText = confirmPwd.getText().toString();
            if (resetText != null && confirmText != null) {
                if ((!resetText.trim().equals("")) && (!confirmText.trim().equals(""))) {

                    if (resetText.toString().length() == 6){
                        if (resetText.trim().equals(confirmText.trim())){
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString(MY_PASSWORD,resetText);
                            editor.commit();
                            Toast.makeText(context,"修改成功",Toast.LENGTH_LONG).show();
                            getActivity().finish();
                        }else{
                            Toast.makeText(context,"修改密码失败，前后密码不匹配！",Toast.LENGTH_LONG).show();
                        }
                    }else {
                        Toast.makeText(context,"修改密码失败,密码长度应为6位数！",Toast.LENGTH_LONG).show();
                    }

                }
            }
        }
    }

}
