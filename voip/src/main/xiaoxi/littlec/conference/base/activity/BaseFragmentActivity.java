package littlec.conference.base.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
/**
 * 应用程序FragmentActivity的基类
 */
public abstract class BaseFragmentActivity extends FragmentActivity {

    public void startTargetActivity(Class targetClass) {
        Intent mIntent = new Intent(getApplicationContext(), targetClass);
        startActivity(mIntent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    protected void showToast(String str) {
        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_SHORT).show();
    }
}
