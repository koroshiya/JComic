package com.japanzai.koroshiya;

import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public abstract class DrawerActivity extends FragmentActivity {

    protected DrawerLayout mDrawerLayout;

    public abstract void drawerClick(int i);

    public void instantiateDrawer(String[] items){

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        findViewById(R.id.btn_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawerLayout.openDrawer(Gravity.START);
            }
        });

        ListView mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items));
        mDrawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                drawerClick(i);
            }
        });
    }

}
