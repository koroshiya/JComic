package com.japanzai.koroshiya;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * Purpose: Used to display information about this application
 * */
public class About extends Activity {
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.about);
        instantiate();

    }
	
    @Override
    public void onResume(){
    	super.onResume();
    	MainActivity.mainActivity.getSettings().forceOrientation(this);
    }
	
	private void instantiate(){
		
		TextView manual = (TextView)findViewById(R.id.txtManual);
		
		manual.setEnabled(true);
		manual.setMovementMethod(LinkMovementMethod.getInstance());
		
		InputStream inputStream = this.getResources().openRawResource(R.raw.manual);
		
		InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        String line;
        StringBuilder text = new StringBuilder();

        try {
        	while (( line = buffreader.readLine()) != null) {
            	text.append(line);
            	text.append("<br />");
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
		manual.setText(Html.fromHtml(text.toString()));
		
	}
	
}
