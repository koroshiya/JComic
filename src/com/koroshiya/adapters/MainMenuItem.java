package com.koroshiya.adapters;

public class MainMenuItem{

    private final int strResId;
    private final int imgResId;

    public MainMenuItem(int strResId, int imgResId){
        this.strResId = strResId;
        this.imgResId = imgResId;
    }

    public int getStrResId(){
        return this.strResId;
    }

    public int getImageResId(){
        return this.imgResId;
    }

}
