package com.koroshiya.settings.classes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.koroshiya.settings.SettingsDb;
import com.koroshiya.settings.SettingsManager;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

public class Recent {

    private static final String JSON_ARG_FILE = "file";
    private static final String JSON_ARG_PAGE = "page";
    private static final String JSON_ARG_UUID = "uuid";

    private static final String TABLE_NAME = "Recent";
    public static final String SQL = String.format(
            Locale.ENGLISH,
            "CREATE TABLE %s (%s TEXT, %s INTEGER, %s INTEGER)",
            TABLE_NAME, JSON_ARG_FILE, JSON_ARG_PAGE, JSON_ARG_UUID
    );

    private final String path;
    private int pageNumber;
    private final long uuid;

    public Recent(String path, int pageNumber){
        this.path = path;
        this.pageNumber = pageNumber;
        this.uuid = UUID.randomUUID().getMostSignificantBits();
    }

    private Recent(Cursor c){
        this.path = c.getString(c.getColumnIndex(JSON_ARG_FILE));
        this.pageNumber = c.getInt(c.getColumnIndex(JSON_ARG_PAGE));
        this.uuid = c.getLong(c.getColumnIndex(JSON_ARG_UUID));
    }

	public String getPath(){
		return this.path;
	}

    public long getUuid(){return this.uuid;}

    private void insertOrUpdate(Context context) {

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getWritableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ?", JSON_ARG_UUID);
        String[] whereArgs = {Long.toString(uuid)};

        ContentValues cv = new ContentValues();
        cv.put(JSON_ARG_FILE, path);
        cv.put(JSON_ARG_PAGE, pageNumber);
        cv.put(JSON_ARG_UUID, uuid);

        if (db.update(TABLE_NAME, cv, where, whereArgs) == 0){
            db.insert(TABLE_NAME, null, cv);

            long difference = count(context, pageNumber >= 0) - SettingsManager.getMaxRecent(context);
            if (difference > 0){
                Cursor c = db.query(TABLE_NAME, null, null, null, null, null, null, Long.toString(difference));
                if (c.moveToFirst()) {
                    do {
                        Recent r = new Recent(c);
                        SettingsManager.deleteRecent(context, r);
                    }while(c.moveToNext());
                }
                c.close();
            }
        }

    }

    public void delete(Context context) {

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getWritableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ?", JSON_ARG_UUID);
        String[] whereArgs = {Long.toString(uuid)};

        db.delete(TABLE_NAME, where, whereArgs);

        File f = new File(context.getCacheDir(), Long.toString(uuid) + ".webp");
        if (f.exists()){
            if (f.delete()){
                Log.v("Recent", "Deleted: "+f.getAbsolutePath());
            }
        }

    }

    @Nullable
    public static Recent get(Context context, String path, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ? AND ", JSON_ARG_FILE);
        String[] whereArgs = {path};

        if (isRecent){
            where += JSON_ARG_PAGE + " >= 0";
        }else{
            where += JSON_ARG_PAGE + " < 0";
        }

        Recent r = null;
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, null, where, whereArgs, null, null, null, "1");
            if (c.moveToFirst()) {
                r = new Recent(c);
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (c != null) c.close();
        }

        return r;

    }

    public static void delete(Context context, String path, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getWritableDatabase();

        String where = String.format(Locale.ENGLISH, "%s = ? AND ", JSON_ARG_FILE);
        String[] whereArgs = {path};

        if (isRecent){
            where += JSON_ARG_PAGE + " >= 0";
        }else{
            where += JSON_ARG_PAGE + " < 0";
        }

        try {
            db.delete(TABLE_NAME, where, whereArgs);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    public static void delete(Context context, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getWritableDatabase();

        String where;
        if (isRecent){
            where = JSON_ARG_PAGE + " >= 0";
        }else{
            where = JSON_ARG_PAGE + " < 0";
        }

        try {
            db.delete(TABLE_NAME, where, null);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @NonNull
    public static ArrayList<String> getPaths(Context context, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String where;

        if (isRecent){
            where = JSON_ARG_PAGE + " >= 0";
        }else{
            where = JSON_ARG_PAGE + " < 0";
        }

        ArrayList<String> r = new ArrayList<>();
        ArrayList<Recent> toDelete = new ArrayList<>();
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, null, where, null, null, null, null);
            if (c.moveToFirst()){
                do {
                    String path = c.getString(c.getColumnIndex(JSON_ARG_FILE));
                    if (new File(path).exists()) {
                        r.add(path);
                    }else{
                        toDelete.add(new Recent(c));
                    }
                }while(c.moveToNext());
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (c != null) c.close();
        }

        for (Recent recent : toDelete) recent.delete(context);

        return r;

    }

    @NonNull
    public static ArrayList<Recent> get(Context context, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String where;

        if (isRecent){
            where = JSON_ARG_PAGE + " >= 0";
        }else{
            where = JSON_ARG_PAGE + " < 0";
        }

        ArrayList<Recent> r = new ArrayList<>();
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, null, where, null, null, null, null);
            if (c.moveToFirst()){
                do {
                    r.add(new Recent(c));
                }while(c.moveToNext());
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (c != null) c.close();
        }

        return r;

    }

    public int getPageNumber(){
        return this.pageNumber;
    }

    public static int getPageNumber(Context context, String path, int defaultVal){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String[] selection = {JSON_ARG_PAGE};
        String where = String.format(Locale.ENGLISH, "%s = ? AND %s >= 0", JSON_ARG_FILE, JSON_ARG_PAGE);
        String[] whereArgs = {path};

        int result = defaultVal;
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, selection, where, whereArgs, null, null, null, "1");
            if (c.moveToFirst()){
                result = c.getInt(c.getColumnIndex(JSON_ARG_PAGE));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (c != null) c.close();
        }

        return result;

    }

    public static long getUuid(Context context, String path, boolean isRecent, long defaultVal){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String[] selection = {JSON_ARG_UUID};

        String where = String.format(Locale.ENGLISH, "%s = ? AND ", JSON_ARG_FILE);

        if (isRecent){
            where += JSON_ARG_PAGE + " >= 0";
        }else{
            where += JSON_ARG_PAGE + " < 0";
        }

        String[] whereArgs = {path};

        long result = defaultVal;
        Cursor c = null;
        try {
            c = db.query(TABLE_NAME, selection, where, whereArgs, null, null, null, "1");
            if (c.moveToFirst()){
                result = c.getLong(c.getColumnIndex(JSON_ARG_UUID));
            }
        } catch (Exception e){
            e.printStackTrace();
        } finally{
            if (c != null) c.close();
        }

        return result;

    }

    public static long count(Context context, boolean isRecent){

        SettingsDb sdb = new SettingsDb(context);
        SQLiteDatabase db = sdb.getReadableDatabase();

        String where;

        if (isRecent){
            where = JSON_ARG_PAGE + " >= 0";
        }else{
            where = JSON_ARG_PAGE + " < 0";
        }

        return DatabaseUtils.queryNumEntries(db, TABLE_NAME, where);

    }

    public void setPageNumber(Context context, int pageNumber) {
        this.pageNumber = pageNumber;
        this.insertOrUpdate(context);
    }

}
