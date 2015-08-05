package com.japanzai.koroshiya;

<<<<<<< HEAD
import android.app.Dialog;
import android.support.v4.app.FragmentActivity;
=======
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
import android.view.View;

public abstract class DrawerActivity extends FragmentActivity {

    public abstract void drawerClick(int i);

    public void instantiateDrawer(final String[] items){

<<<<<<< HEAD
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
=======
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

>>>>>>> b222c898f259e1dddaf8a70f3980f3ef5010c7a9
    }

}
