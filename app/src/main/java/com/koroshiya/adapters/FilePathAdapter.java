package com.koroshiya.adapters;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.koroshiya.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

public class FilePathAdapter extends RecyclerView.Adapter<FilePathAdapter.ViewHolder> {

    private String[] splitVals;
    private String currentDir;
    private int currentChunk;
    private final ArrayList<String> pathHistory = new ArrayList<>();
    private final ArrayList<Integer> chunkHistory = new ArrayList<>();
    private final Handler.Callback permCallback;

    public static final String ARG_FILE_PATH_CHUNK = "filepathchunk";
    public static final String ARG_GOING_BACK = "history";

    public FilePathAdapter(File filepath, Handler.Callback permCallback) {
        this.currentDir = filepath.getAbsolutePath();
        if (this.currentDir.length() > 1) {
            this.splitVals = currentDir.split("/");
        }else{
            this.splitVals = new String[]{"/"};
        }
        this.currentChunk = splitVals.length - 1;
        this.permCallback = permCallback;
        this.pathHistory.add(currentDir);
        this.chunkHistory.add(currentChunk);
    }

    public String getCurrentDir(){
        return this.currentDir;
    }

    public int getCurrentChunk(){
        return this.currentChunk;
    }

    public boolean setNewPath(String path, boolean goingBack){

        if (goingBack){
            if (pathHistory.size() > 1 && chunkHistory.size() > 1){
                int len = pathHistory.size();
                pathHistory.remove(len - 1);
                currentDir = pathHistory.get(len - 2);

                len = chunkHistory.size();
                chunkHistory.remove(len - 1);
                currentChunk = chunkHistory.get(len - 2);
            }else{
                return false;
            }
        }else {
            currentDir = path;

            StringBuilder oldPath = new StringBuilder();
            for (String splitVal : splitVals) {
                oldPath.append(String.format(Locale.getDefault(), "%s/", splitVal));
            }
            int len = oldPath.length();
            if (len > 1) {
                oldPath.delete(len - 1, len); //remove trailing slash
            }

            String oPath = oldPath.toString();

            Log.i("FPA", String.format(Locale.getDefault(), "Comparing %s to %s", oPath, currentDir));


            if (oPath.startsWith(this.currentDir)) {
                currentChunk = currentDir.split("/").length - 1;
            } else {
                splitVals = currentDir.split("/");
                currentChunk = splitVals.length - 1;
            }

            chunkHistory.add(currentChunk);
            pathHistory.add(currentDir);

        }

        return true;

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

        private final CardView v;
        private final TextView tv;

        public ViewHolder(View v) {
            super(v);
            this.v = (CardView) v;
            this.tv = (TextView) v.findViewById(R.id.list_item_breadcrumb_txt);

        }

        public void setDataOnView(final int position) {

            String chunk = splitVals[position];
            if (chunk.length() == 0) chunk = "/";

            if (tv != null){
                tv.setText(chunk);
            }

            if (position == currentChunk || (position == 0 && currentChunk == -1)){ //Check -1 for root
                //Set color
                int color = ContextCompat.getColor(v.getContext(), R.color.cardview_dark_background);
                TextView actv = (TextView) v.getChildAt(0);
                v.setCardBackgroundColor(color);
                actv.setTextColor(Color.WHITE);
            }else{
                int color = ContextCompat.getColor(v.getContext(), R.color.cardview_light_background);
                v.setCardBackgroundColor(color);
                TextView actv = (TextView) v.getChildAt(0);
                actv.setTextColor(Color.BLACK);
                //Remove color
                //int color = ContextCompat.getColor(v.getContext(), android.R.color.background_light);
                //v.setBackgroundColor(color);
            }

            v.setOnClickListener(v1 -> {

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
                    b.putBoolean(ARG_GOING_BACK, false);
                    m.setData(b);
                    permCallback.handleMessage(m);

                }else{
                    Snackbar.make(v1, "Cannot read directory - Permission denied", Snackbar.LENGTH_SHORT).show();
                }
            });

        }

    }

}