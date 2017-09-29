package com.koroshiya.settings;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.koroshiya.settings.classes.Recent;
import com.koroshiya.settings.classes.Setting;

import java.io.File;

public class SettingsDb extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "JComic.db";
    private static final String[] SQL_CREATE_ENTRIES = {
            Recent.SQL, Setting.SQL
    };

    public SettingsDb(@NonNull Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        File cFile = new File(context.getCacheDir(), "cache.json");
        if (cFile.exists() && cFile.length() > 0){
            File[] files = context.getCacheDir().listFiles();
            if (files != null) { //In case of missing read permission
                for (File file : files) {
                    if (file.delete()) {
                        Log.v("SettingsDb", "Deleted cache file");
                    }
                }
            }
        }

    }

    public void onCreate(SQLiteDatabase db) {

        for (String sqlCreateEntry : SQL_CREATE_ENTRIES) {
            db.execSQL(sqlCreateEntry);
        }

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        switch (oldVersion){
            case 1:
                //What to do when upgrading from db version 1
                break;
        }

    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
