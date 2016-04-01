package com.japanzai.koroshiya.activities;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
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
    private final int CREDITS = 5;
    private final int REPORT_ERROR = 6;

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
            FragmentManager fm = getFragmentManager();
            Fragment frag;
            View v = findViewById(R.id.navigation_drawer);

            switch (position){
                case SETTINGS:
                    frag = new SettingFragment();
                    break;
                case CONTINUE:
                    frag = ReadFragment.newInstance(null, -1, this);
                    if (frag == null){
                        Snackbar.make(v, "No recent comics found", Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    break;
                case RECENT:
                    if (SettingsManager.getRecentAndFavorites(this, true).size() > 0){
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
                    if (SettingsManager.getRecentAndFavorites(this, false).size() > 0){
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
                case CREDITS:
                    frag = new CreditsFragment();
                    break;
                default:
                    Snackbar.make(v, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
                    return;
            }

            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container, frag);
            ft.addToBackStack(null);
            ft.commit();
        }
    }

    @Override
    public void selectNavDrawerItem(int position){
        mNavigationDrawerFragment.selectItem(position);
    }

    public void fileChooserCallback(String filePath, int page){
        Fragment frag = ReadFragment.newInstance(filePath, page, this);
        FragmentManager fm = this.getFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.container, frag);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onBackPressed() {
        if (mNavigationDrawerFragment.isDrawerOpen()) {
            mNavigationDrawerFragment.closeDrawer();
        }else{
            FragmentManager fm = this.getFragmentManager();
            if (fm.getBackStackEntryCount() == 1) {
                super.onBackPressed();
            } else {
                fm.popBackStackImmediate();
            }
        }
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
