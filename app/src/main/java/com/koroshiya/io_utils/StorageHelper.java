package com.koroshiya.io_utils;

import android.os.Environment;

public class StorageHelper {

    private static boolean externalStorageReadable;

    public static boolean isExternalStorageReadable() {
        checkStorage();
        return externalStorageReadable;
    }

    private static void checkStorage() {
        String state = Environment.getExternalStorageState();
        switch (state) {
            case Environment.MEDIA_MOUNTED:
            case Environment.MEDIA_MOUNTED_READ_ONLY:
                externalStorageReadable = true;
                break;
            default:
                externalStorageReadable = false;
                break;
        }
    }

}
