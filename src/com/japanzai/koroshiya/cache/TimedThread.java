package com.japanzai.koroshiya.cache;

import android.os.Handler;
import android.os.Looper;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.reader.Progress;
import com.japanzai.koroshiya.reader.ToastThread;

public class TimedThread extends Thread {

    protected boolean isFinished = false;

    @Override
    public void run(){
        final Thread th = this;
        new Thread(new Runnable() {
            public void run() {
                Looper.prepare();
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        if (!isFinished && isAlive()) {
                            try {
                                th.interrupt();
                                th.stop();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (Progress.self.isVisible) {
                                try {
                                    Progress.self.oldFinish();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                            MainActivity main = MainActivity.getMainActivity();
                            main.runOnUiThread(new ToastThread(R.string.image_read_error, main));
                        }
                    }
                }, 20000); //20 seconds
                Looper.loop();
            }
        }).start();
    }

}
