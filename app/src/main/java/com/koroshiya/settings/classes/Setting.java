package com.koroshiya.settings.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.Nullable;

import com.koroshiya.settings.SettingsDb;

import java.util.Locale;

public class Setting {

    private static final String KEY = "key";
    private static final String VALUE = "value";

    private static final String TABLE_NAME = "Setting";
    public static final String SQL = String.format(
            Locale.ENGLISH,
            "CREATE TABLE %s (%s TEXT, %s TEXT)",
            TABLE_NAME, KEY, VALUE
    );

    public static void insertOrUpdate(Context context, String key, String value) {

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getWritableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ?", KEY);
        String[] whereArgs = {key};

        ContentValues cv = new ContentValues();
        cv.put(KEY, key);
        cv.put(VALUE, value);

        if (db.update(TABLE_NAME, cv, where, whereArgs) == 0){
            db.insert(TABLE_NAME, null, cv);
        }

    }

    @Nullable
    public static String getString(Context context, String key){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ?", KEY);
        String[] whereArgs = {key};

        Cursor c = null;
        String value = null;

        try {
            c = db.query(TABLE_NAME, new String[]{VALUE}, where, whereArgs, null, null, null, "1");
            if (c.moveToFirst()) {
                value = c.getString(c.getColumnIndex(VALUE));
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if (c != null) c.close();
        }

        return value;

    }

}
