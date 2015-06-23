package com.japanzai.koroshiya;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.View;

public abstract class DrawerActivity extends FragmentActivity {

    public abstract void drawerClick(int i);

    public void instantiateDrawer(final String[] items){

        final Context context = this;

        findViewById(R.id.btn_toggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(new ContextThemeWrapper(context, R.style.Theme_AppCompat_Dialog))
                        .setItems(items,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        drawerClick(whichButton);
                                    }
                                })
                        .show();
            }
        });

    }

}
