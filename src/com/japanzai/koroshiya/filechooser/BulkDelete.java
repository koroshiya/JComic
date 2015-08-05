package com.japanzai.koroshiya.filechooser;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.View;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.controls.ResizingGridView;
import com.japanzai.koroshiya.dialog.MessageDialog;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.SettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class BulkDelete extends FragmentActivity {

    protected ResizingGridView v;
    File home;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.activity_bulk_delete);
        v = (ResizingGridView) findViewById(R.id.FileChooserPane);

        findViewById(R.id.btn_cancel).setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        instantiate();

        if (MainActivity.mainActivity.getSettings().isShowLastDelete()) {
            ArrayList<String> arr = new ArrayList<>();
            arr.add(getString(R.string.bulk_delete_prompt1));
            arr.add(getString(R.string.bulk_delete_prompt2));

            new MessageDialog(arr, getString(R.string.bulk_delete_btn)).show(this);
        }

    }

    public void instantiate(){
        File smHome = MainActivity.mainActivity.getSettings().getHomeDir();

        if (smHome != null && smHome.exists() && smHome.isDirectory()){
            this.home = smHome;
        }else{
            File home = Environment.getExternalStorageDirectory();
            if (home == null){
                String userHome = System.getProperty("user.home");
                home = new File(userHome);
            }
            this.home = home;
        }

        setAdapter();
    }

    public File getHomeAsFile(){
        return home;
    }

    public void setAdapter(){

        ArrayList<String> listItems = new ArrayList<>();
        FileItemAdapter aList;

        ArrayList<String> tempList = new ArrayList<>();
        for (File s : getHomeAsFile().listFiles()){
            if (FileChooser.isSupportedFile(s) && !s.isHidden()){tempList.add(s.getName());}
        }
        Object[] tempArray = tempList.toArray();
        Arrays.sort(tempArray);
        for (Object obj : tempArray){
            listItems.add(obj.toString());
        }

        View.OnClickListener ocl = new View.OnClickListener(){
            @Override
            public void onClick(View view) {
            if (new File(getHomeAsFile().getAbsolutePath() + "/" + view.getContentDescription()).delete()) setAdapter();
            }
        };

        aList = new FileItemAdapter(this, FileItem.getHashList(listItems, this), ocl, null);

        v.setAdapter(aList);

    }

}
