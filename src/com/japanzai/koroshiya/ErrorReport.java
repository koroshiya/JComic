package com.japanzai.koroshiya;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * @author Koroshiya
 * Custom error reporting screen.
 * When implementing your own, make sure to change the email variable to your own email.
 * */
public class ErrorReport extends SherlockFragmentActivity implements OnClickListener{

	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.error);
        instantiate();

    }
	
    @Override
    public void onResume(){
    	super.onResume();
    	MainActivity.mainActivity.getSettings().forceOrientation(this);
    }
	
	private void instantiate(){
		
		TextView tx = (TextView)findViewById(R.id.errorText);
		tx.setText("\n" + getString(R.string.error_report_para_1) + "\n\n" + 
					getString(R.string.error_report_para_2) + "\n\n" + 
					getString(R.string.error_report_para_3) + "\n");
		
		Button error = (Button)findViewById(R.id.btnSendError);
		error.setOnClickListener(this);
		
	}
	
	@Override
	public void onClick(View v) {
		sendErrorLog();
	}
	
	private void sendErrorLog() {

        String email = "koro.jcomic@gmail.com";
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
		i.putExtra(Intent.EXTRA_SUBJECT, "Crash report");
		StringBuilder buffer = new StringBuilder();
		try{	
			PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			String version = pInfo.versionName;
			buffer.append("App version: ").append(version);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}finally{
			buffer.append("\n");
		}
		buffer.append("Android version: ").append(Build.VERSION.RELEASE);
		buffer.append("\n");
		buffer.append("Device: ").append(getDeviceName());
		buffer.append("\n");
		
		i.putExtra(Intent.EXTRA_TEXT, buffer.toString());
		try {
			startActivity(Intent.createChooser(i, "Send log..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "There are no email clients installed, or you aren't connected to the internet.",
					Toast.LENGTH_SHORT).show();
		}
		
	}
	
	public String getDeviceName() {
		String manufacturer = Build.MANUFACTURER;
		String model = Build.MODEL;
		return (model.startsWith(manufacturer) ? capitalize(model) : capitalize(manufacturer) + " " + model);
	}

	private String capitalize(String s) {
		if (s == null || s.length() == 0) {return "";}
		char first = s.charAt(0);
		return (Character.isUpperCase(first) ? s : Character.toUpperCase(first) + s.substring(1));
	}
	
}