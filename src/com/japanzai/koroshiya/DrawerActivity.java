package com.japanzai.koroshiya;

import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class DrawerActivity extends FragmentActivity {

    public abstract void drawerClick(int i);

    public void instantiateDrawer(String[] items){

        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.list);
        dialog.setCancelable(true);

        ArrayAdapter<String> itemAdapter = new ArrayAdapter<>(this, R.layout.context_list_item, items);
        ListView lv = new ListView(this);
        lv.setAdapter(itemAdapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView av, View v, int i, long l) {
                drawerClick(i);
                dialog.dismiss();
            }
        });

        dialog.setContentView(lv);
        dialog.show();
    }

}
