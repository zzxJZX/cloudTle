package com.cmri.tvdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.EditText;

import com.cmri.moudleapp.moudlevoip.ICmccManager;

/**
 * Created by Administrator on 2018/3/14.
 */

public class ImsCallAcitivity extends Activity {
    private EditText etCallPhone;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callout);
        etCallPhone = (EditText) findViewById(R.id.ed_call_num);
    }
    public void action1v1Call(View view) {
        ICmccManager.getInstance().actionStartImsAudio(this, "0"+etCallPhone.getText().toString());

    }
    public void actionyuyin(View view){
        Intent intent=new Intent();
        intent.setClass(this,SmartInteractive.class);
        startActivity(intent);
    }

}
