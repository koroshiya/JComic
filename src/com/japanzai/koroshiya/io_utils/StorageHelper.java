package com.japanzai.koroshiya.io_utils;

import android.os.Environment;

public class StorageHelper {

    private static boolean externalStorageReadable, externalStorageWritable;

    public static boolean isExternalStorageReadable() {
        checkStorage();
        return externalStorageReadable;
    }

    public static boolean isExternalStorageWritable() {
        checkStorage();
        return externalStorageWritable;
    }

    public static boolean isExternalStorageReadableAndWritable() {
        checkStorage();
        return externalStorageReadable && externalStorageWritable;
    }

    private static void checkStorage() {
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
                externalStorageReadable = externalStorageWritable = true;
                break;
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                externalStorageReadable = true;
                externalStorageWritable = false;
                break;
            default:
                externalStorageReadable = externalStorageWritable = false;
                break;
        }
    }

}
