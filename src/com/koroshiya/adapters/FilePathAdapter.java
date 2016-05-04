package com.koroshiya.adapters;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koroshiya.BR;
import com.koroshiya.R;
import com.koroshiya.pojo.POJOStringField;

import java.io.File;

public class FilePathAdapter extends RecyclerView.Adapter<FilePathAdapter.ViewHolder> {

    private String[] splitVals;
    private String currentDir;
    private int currentChunk;
    private final Handler.Callback permCallback;

    public static final String ARG_FILE_PATH_CHUNK = "filepathchunk";

    public FilePathAdapter(File filepath, Handler.Callback permCallback) {
        this.currentDir = filepath.getAbsolutePath();
        this.splitVals = currentDir.split("/");
        this.currentChunk = splitVals.length - 1;
        this.permCallback = permCallback;
    }

    public void setNewPath(String path){
        currentDir = path;

        String oldPath = "";
        for (int i = 0; i < splitVals.length; i++) {
            oldPath += splitVals[i] + "/";
        }
        if (oldPath.length() > 1) oldPath = oldPath.substring(0, oldPath.length() - 1); //remove trailing slash

        Log.i("FPA", "Comparing "+oldPath+" to "+currentDir);


        if (oldPath.startsWith(this.currentDir)){
            currentChunk = currentDir.split("/").length - 1;
            if (currentChunk == -1) currentChunk = 0; //For /
            Log.i("FPA", "Setting currentchunk to "+currentChunk);
        }else {
            splitVals = currentDir.split("/");
            currentChunk = splitVals.length - 1;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_breadcrumb, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.setDataOnView(position);
    }

    @Override
    public int getItemCount() {
        return splitVals.length;
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private final ViewDataBinding binding;
        private final CardView v;

        public ViewHolder(View v) {
            super(v);
            this.v = (CardView) v;

            binding = DataBindingUtil.bind(v);

        }

        public void setDataOnView(final int position) {

            String chunk = splitVals[position];
            if (chunk.length() == 0) chunk = "/";

            POJOStringField label = new POJOStringField(chunk);
            binding.setVariable(BR.label, label);

            if (position == currentChunk){
                //Set color
                int color = ContextCompat.getColor(v.getContext(), R.color.cardview_dark_background);
                AppCompatTextView actv = (AppCompatTextView) v.getChildAt(0);
                v.setCardBackgroundColor(color);
                actv.setTextColor(Color.WHITE);
            }else{
                int color = ContextCompat.getColor(v.getContext(), R.color.cardview_light_background);
                v.setCardBackgroundColor(color);
                AppCompatTextView actv = (AppCompatTextView) v.getChildAt(0);
                actv.setTextColor(Color.BLACK);
                //Remove color
                //int color = ContextCompat.getColor(v.getContext(), android.R.color.background_light);
                //v.setBackgroundColor(color);
            }

            binding.executePendingBindings();

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String path = "";
                    for (int i = 0; i <= position; i++) {
                        path += splitVals[i] + "/";
                    }

                    Log.i("FPA", path);

                    File newPath = new File(path);

                    if (newPath.canRead()){

                        Message m = new Message();
                        Bundle b = new Bundle();
                        b.putString(ARG_FILE_PATH_CHUNK, path);
                        m.setData(b);
                        permCallback.handleMessage(m);

                    }else{
                        Snackbar.make(v, "Cannot read directory - Permission denied", Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

        }

    }

}