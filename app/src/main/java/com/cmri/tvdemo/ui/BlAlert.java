package com.cmri.tvdemo.ui;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cmri.tvdemo.Person;
import com.cmri.tvdemo.R;

import java.util.ArrayList;

/**
 * Created by Administrator on 2018/5/24.
 */

public class BlAlert {
    public interface OnAlertSelectId1 {
        void onClick(Person person);
    }
    public static Dialog showAlert(final Context context , final ArrayList<Person> items, final OnAlertSelectId1 alertDo, DialogInterface.OnCancelListener cancelListener) {
        final Dialog dlg = new Dialog(context, R.style.MMTheme_DataSheet1);
        // R.style.MMTheme_DataSheet
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.alert_dialog_menu_layout, null);
        final int cFullFillWidth = 10000;
        layout.setMinimumWidth(cFullFillWidth);
        final ListView list = (ListView) layout.findViewById(R.id.content_list);
        AlertAdapter adapter = new AlertAdapter(context, items);
        list.setAdapter(adapter);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    alertDo.onClick(items.get(position));
                    dlg.dismiss();
                    list.requestFocus();

            }
        });

        TextView cancelText = (TextView) layout.findViewById(R.id.cancel_text);
        cancelText.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                dlg.dismiss();
            }
        });

        // set a large value put it in bottom
        Window w = dlg.getWindow();
        WindowManager.LayoutParams lp = w.getAttributes();
        lp.x = 0;
        final int cMakeBottom = -1000;
        lp.y = cMakeBottom;
        lp.gravity = Gravity.BOTTOM;
        dlg.onWindowAttributesChanged(lp);
        dlg.setCanceledOnTouchOutside(true);
        if (cancelListener != null) {
            dlg.setOnCancelListener(cancelListener);
        }
        dlg.setContentView(layout);
        dlg.show();
        return dlg;
    }

}

class AlertAdapter extends BaseAdapter {
    public static final int TYPE_BUTTON = 0;
    public static final int TYPE_TITLE = 1;
    public static final int TYPE_EXIT = 2;
    private ArrayList<Person> items;
    private int[] types;
    private boolean isTitle = false;
    private Context context;

    public AlertAdapter(Context context, ArrayList<Person> items) {
        if (items == null || items.size() == 0) {
            this.items = new ArrayList<Person>();
        } else {
            this.items=items;
        }

        this.context = context;

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final Person person = (Person) getItem(position);
        ViewHolder holder;

        if (convertView == null){
            holder = new ViewHolder();

            convertView = View.inflate(context, R.layout.alert_dialog_menu_list_layout, null);

            holder.text = (TextView) convertView.findViewById(R.id.popup_text);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(person.getName()+"  "+person.getIdcard());
        return convertView;
    }

    static class ViewHolder {
        TextView text;

    }
}