package com.cmri.tvdemo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import com.cmri.moudleapp.moudlevoip.ICmccManager;

/**
 * Created by fangwei on 2018/1/19.
 */

public class OttActivity extends AppCompatActivity {
    private EditText etLoginPhone, etCallPhone;
    private String mLoginPhone = "19999990001";
    private String mCallPhone = "19999990002";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ott_demo);
        initView();
    }

    private void initView() {
        etLoginPhone = (EditText) findViewById(R.id.ed_ott_login_num);
        etCallPhone = (EditText) findViewById(R.id.ed_call_num);

        etLoginPhone.setText(mLoginPhone);
        etCallPhone.setText(mCallPhone);
    }

    public void actionOttLogin(View view) {
        ICmccManager.getInstance().loginCmccOtt(this, etLoginPhone.getText().toString());
    }

    public void actionOttCallAudio(View view) {
        ICmccManager.getInstance().actionStartAudio1V1(this, etCallPhone.getText().toString());
    }

    public void actionOttCallVideo(View view) {
        ICmccManager.getInstance().actionStartVideo1V1(this, etCallPhone.getText().toString());
    }

    public void actionOttCallLogout(View view) {
        ICmccManager.getInstance().logoutCmcc(this);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        ICmccManager.getInstance().logoutCmcc(this);
    }
}
