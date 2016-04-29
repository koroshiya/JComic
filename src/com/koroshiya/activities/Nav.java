package com.koroshiya.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.koroshiya.R;
import com.koroshiya.fragments.CreditsFragment;
import com.koroshiya.fragments.FileChooserFragment;
import com.koroshiya.fragments.ReadFragment;
import com.koroshiya.fragments.RecentFragment;
import com.koroshiya.fragments.SettingFragment;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.io_utils.StorageHelper;
import com.koroshiya.settings.SettingsManager;

import java.io.File;

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

        Intent i = getIntent();
        if (i.getData() != null){
            Uri uri = i.getData();
            handleFileInput(uri);
        }

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
            if (v == null) return;

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
                    if (!StorageHelper.isExternalStorageReadable()){
                        Snackbar.make(v, "External storage not readable", Snackbar.LENGTH_SHORT).show();
                        return;
                    }else if (!hasStoragePermission()){
                        askStoragePermission();
                        return;
                    }else{
                        frag = new FileChooserFragment();
                    }
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

    private void handleFileInput(Uri uri){ //TODO: check for storage permissions
        File f = new File(uri.getPath());
        String filePath = f.getAbsolutePath();
        Log.i("Nav", "Trying to load: "+filePath);
        boolean supported = ImageParser.isSupportedFile(f);

        if (!supported){
            Toast.makeText(this, R.string.str_unsupported_file_type_or_unreadable_directory, Toast.LENGTH_SHORT).show();
        }else{
            if (ImageParser.isSupportedImage(f)){
                File[] files = f.getParentFile().listFiles(ImageParser.fnf);
                String name = f.getName();
                int i = -1;
                for (int i1 = 0; i1 < files.length; i1++) {
                    File file = files[i1];
                    if (file.getName().equals(name)){
                        i = i1;
                        break;
                    }
                }
                fileChooserCallback(filePath, i);
            }else if (ArchiveParser.isSupportedArchive(filePath)){
                fileChooserCallback(filePath, 0);
            }else if (ImageParser.isSupportedDirectory(f)){
                fileChooserCallback(filePath, 0);
            }else{
                Toast.makeText(this, R.string.str_unsupported_file_type_or_unreadable_directory, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSettingsPage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    public boolean hasStoragePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    public void askStoragePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(this, new String[]{perm}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean notGranted = grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED;
        if (notGranted){
            openSettingsPage("You must give JComic storage permissions");
        }else{
            onNavigationDrawerItemSelected(READ);
        }
    }

}