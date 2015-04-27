package com.japanzai.koroshiya.filechooser;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.io_utils.ArchiveParser;
import com.japanzai.koroshiya.io_utils.ImageParser;

import java.util.ArrayList;

public class FileItem{

    private final String text;
    private final int res;
    private final Activity act;

    public FileItem(String text, int res, Activity act){
        this.text = text;
        this.res = res;
        this.act = act;
    }

    public String getText(){
        return text;
    }

    public Drawable getRes(){
        return android.os.Build.VERSION.SDK_INT >= 21 ? act.getDrawable(res) : act.getResources().getDrawable(res);
    }

    public static ArrayList<FileItem> getHashList(ArrayList<String> listItems, Activity act){

        ArrayList<FileItem> aList = new ArrayList<>();

        for (String s : listItems){

            int img;
            if (ImageParser.isSupportedImage(s)){
                img = R.drawable.image;
            }else if (ArchiveParser.isSupportedZipArchive(s)){
                img = R.drawable.zip;
            }else if (ArchiveParser.isSupportedRarArchive(s)){
                img = R.drawable.rar;
            }else{
                img = R.drawable.folder;
            }
            FileItem hm = new FileItem(s, img, act);
            aList.add(hm);
        }

        return aList;
    }
}