package com.japanzai.koroshiya.filechooser;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.io_utils.ImageParser;

import java.io.File;
import java.util.ArrayList;

public class FileItem{

    private final String text;
    private final int res;

    public FileItem(String text, int res){
        this.text = text;
        this.res = res;
    }

    public String getText(){
        return text;
    }

    public int getRes(){
        return res;
    }

    public static ArrayList<FileItem> getHashList(ArrayList<String> listItems){

        ArrayList<FileItem> aList = new ArrayList<>();

        for (String s : listItems){

            int img;
            if (ImageParser.isSupportedImage(s)){
                img = R.drawable.file_media;
            }else if (new File(s).isDirectory()){
                img = R.drawable.file_directory;
            }else{
                img = R.drawable.file_zip;
            }
            FileItem hm = new FileItem(s, img);
            aList.add(hm);
        }

        return aList;
    }
}