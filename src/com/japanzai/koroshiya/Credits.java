package com.japanzai.koroshiya;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.japanzai.koroshiya.reader.MainActivity;
import com.japanzai.koroshiya.settings.SettingsManager;

/**
 * @author Koroshiya
 * Used to display information about those who contributed to this application.
 * When implementing your own, make sure to change the marketAddress to point to your own application or developer profile.
 * Same deal with the author and potentially the license in the instantiate() method.
 * */
public class Credits extends Activity {
	
	private final String marketAddress = "market://details?id=com.japanzai.koroshiya";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        SettingsManager.setFullScreen(this);
        setContentView(R.layout.credits);
        instantiate();

    }
	
    @Override
    public void onResume(){
    	super.onResume();
    	MainActivity.mainActivity.getSettings().forceOrientation(this);
    }
	
	private void instantiate(){
		
		setText(R.id.txtApplication, getString(R.string.app_name));
		
		String versionName;
		try{
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "N/A"; // This should never happen anyway. Handling added just in case.
		}
		setText(R.id.txtVersion, R.string.version, " " + versionName);
		
		setText(R.id.txtAuthor, R.string.author, " Koro" + "\n");
		
		setText(R.id.txtContributions, R.string.contributions, "\n" + "http://jcomic.japanzai.com/index.php?sub=license" + "\n" +
										"Source: https://github.com/koroshiya/JComic" + "\n");
		
		setText(R.id.txtRating, R.string.rating, "\n" + getString(R.string.rating_problem) + "\n");

		findViewById(R.id.btnSendError).setOnClickListener(
                new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(marketAddress));
                        startActivity(intent);
                    }
                }
        );
		
		final Activity act = this;
		
		findViewById(R.id.btnError).setOnClickListener(
			new OnClickListener(){
				@Override
				public void onClick(View v) {
					Intent i = new Intent(act, ErrorReport.class);
					startActivity(i);
				}
			}
		);
		
	}
	
	/**
	 * @param id ID of the TextView for which to set the text
	 * @param resID ID of the String resource to display the text of
	 * @param res Text to display on the TextView
	 * */
	private void setText(int id, int resID, String res){
		setText(id, getString(resID) + res);
	}
	
	/**
	 * @param id ID of the TextView for which to set the text
	 * @param res Text to display on the TextView
	 * */
	private void setText(int id, String res){
		TextView tv = (TextView) findViewById(id);
		tv.setMovementMethod(LinkMovementMethod.getInstance());
		tv.setText(res);
	}
	
}
