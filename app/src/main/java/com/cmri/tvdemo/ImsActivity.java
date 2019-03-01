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

public class ImsActivity extends AppCompatActivity {
    private EditText etCallPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ims_demo);

        etCallPhone = (EditText) findViewById(R.id.ed_call_num);

        etCallPhone.setText("017557281696");
    }

    public void actionLogin(View view) {
        ICmccManager.getInstance().loginCmccIms(this,
                "",
                "ims.sd.chinamobile.com",
                "223.99.141.165",
                6000 ,
                "+8653158021024" ,
                "+8653158021024",
                "+8653158021024@ims.sd.chinamobile.com",
                "a7d6wU9Jr5LK7SI",false);

    }

    public void action1v1Call(View view) {
        ICmccManager.getInstance().actionStartImsAudio(this, etCallPhone.getText().toString());

    }

    public void actionLogout(View view) {
        ICmccManager.getInstance().logoutCmcc(this);
    }
}
