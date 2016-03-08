package com.japanzai.koroshiya.activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.japanzai.koroshiya.R;
import com.japanzai.koroshiya.fragments.CreditsFragment;
import com.japanzai.koroshiya.fragments.FileChooserFragment;
import com.japanzai.koroshiya.fragments.ReadFragment;
import com.japanzai.koroshiya.fragments.RecentFragment;
import com.japanzai.koroshiya.fragments.SettingFragment;
import com.japanzai.koroshiya.settings.SettingsManager;

public class Nav extends AppCompatActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, ActivityCompat.OnRequestPermissionsResultCallback {

    private final int READ = 0;
    private final int CONTINUE = 1;
    private final int RECENT = 2;
    private final int FAVORITES = 3;
    private final int SETTINGS = 4;
    private final int HELP = 5;
    private final int CREDITS = 6;
    private final int REPORT_ERROR = 7;

    private NavigationDrawerFragment mNavigationDrawerFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nav2);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getFragmentManager().findFragmentById(R.id.navigation_drawer);

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout)
        );

    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        if (position == REPORT_ERROR){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/koroshiya/JComic/issues"));
            startActivity(browserIntent);
        }else {
            FragmentManager fragmentManager = getFragmentManager();
            Fragment frag;
            View v = findViewById(R.id.navigation_drawer);
            SettingsManager prefs = new SettingsManager(this, true);

            switch (position){
                case SETTINGS:
                    frag = new SettingFragment();
                    break;
                case CONTINUE:
                    frag = ReadFragment.newInstance(null, -1, prefs, getCacheDir());
                    if (frag == null){
                        Snackbar.make(v, "No recent comics found", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case RECENT:
                    if (prefs.getRecentAndFavorites(true).size() > 0){
                        frag = new RecentFragment();
                        Bundle b = new Bundle();
                        b.putBoolean(RecentFragment.ARG_RECENT, true);
                        frag.setArguments(b);
                    }else{
                        Snackbar.make(v, "No recent comics found", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case FAVORITES:
                    if (prefs.getRecentAndFavorites(false).size() > 0){
                        frag = new RecentFragment();
                        Bundle b = new Bundle();
                        b.putBoolean(RecentFragment.ARG_RECENT, false);
                        frag.setArguments(b);
                    }else{
                        Snackbar.make(v, "No favorites found", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case READ:
                    frag = new FileChooserFragment();
                    break;
                case HELP:
                    //TODO: link to git help page
                case CREDITS:
                    frag = new CreditsFragment();
                    break;
                default:
                    Snackbar.make(v, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
                    return;
            }

            fragmentManager.beginTransaction()
                    .replace(R.id.container, frag)
                    .commit();
        }
    }

    public void fileChooserCallback(String filePath, int page){
        Fragment frag = ReadFragment.newInstance(filePath, page, null, null);
        getFragmentManager().beginTransaction()
                .replace(R.id.container, frag)
                .commit();
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(String permission){
        requestPermissions(new String[]{permission}, 0);
    }




    public void creditsBtnRateApp(View v){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.japanzai.koroshiya"));
        startActivity(intent);
    }

    public void creditsBtnReportError(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/koroshiya/JComic/issues"));
        startActivity(browserIntent);
    }

}
