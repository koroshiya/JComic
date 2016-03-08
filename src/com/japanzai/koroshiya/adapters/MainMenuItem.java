package com.japanzai.koroshiya.adapters;

public class MainMenuItem{

    private final String str;
    private final int i;

    public MainMenuItem(String str, int i){
        this.str = str;
        this.i = i;
    }

    public String getText(){
        return this.str;
    }

    public int getResId(){
        return this.i;
    }

}
