package com.koroshiya.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.koroshiya.R;
import com.koroshiya.fragments.FileChooserFragment;
import com.koroshiya.fragments.ReadFragment;
import com.koroshiya.fragments.RecentFragment;
import com.koroshiya.fragments.SettingFragment;
import com.koroshiya.io_utils.ArchiveParser;
import com.koroshiya.io_utils.ImageParser;
import com.koroshiya.io_utils.StorageHelper;
import com.koroshiya.settings.classes.Recent;

import java.io.File;

public class Nav extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nav2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView mNavigationDrawerFragment = (NavigationView) findViewById(R.id.nav_view);
        if (mNavigationDrawerFragment != null){
            mNavigationDrawerFragment.setNavigationItemSelectedListener(this);

            View v = mNavigationDrawerFragment.getHeaderView(0);

            TextView actv = (TextView) v.findViewById(R.id.nav_header_main_title);
            if (actv != null){
                try {
                    PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                    String version = pInfo.versionName;
                    actv.setText(String.format("Version - %s", version));
                } catch (PackageManager.NameNotFoundException e) {
                    actv.setVisibility(View.GONE);
                }
            }
        }

        Intent i = getIntent();
        if (i.getData() != null){
            Uri uri = i.getData();
            handleFileInput(uri);
        }else if (savedInstanceState == null){
            selectNavItem(R.id.nav_select_comic);
        }

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        int resId = item.getItemId();
        return selectNavItem(resId);

    }

    public boolean selectNavItem (int resId){
        return selectNavItem(resId, null, -1);
    }

    private boolean selectNavItem(int resId, String fileName, int page){

        boolean success = true;

        if (resId == R.id.nav_error_report){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/koroshiya/JComic/issues"));
            startActivity(browserIntent);
        } else if (resId == R.id.nav_credits){
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/koroshiya/JComic/blob/master/Contributions.md"));
            startActivity(browserIntent);
        } else {
            FragmentManager fm = getFragmentManager();
            Fragment frag = null;

            if (drawer == null){
                success = false;
            }else {

                switch (resId) {
                    case R.id.nav_change_settings:
                        frag = new SettingFragment();
                        break;
                    case R.id.nav_continue_reading:
                        frag = ReadFragment.newInstance(fileName, page, this);
                        if (frag == null) {
                            Snackbar.make(drawer, "No recent comics found", Snackbar.LENGTH_SHORT).show();
                            success = false;
                        }
                        break;
                    case R.id.nav_recently_read:
                        if (Recent.count(this, true) > 0) {
                            frag = new RecentFragment();
                            Bundle b = new Bundle();
                            b.putBoolean(RecentFragment.ARG_RECENT, true);
                            frag.setArguments(b);
                        } else {
                            Snackbar.make(drawer, "No recent comics found", Snackbar.LENGTH_SHORT).show();
                            success = false;
                        }
                        break;
                    case R.id.nav_favorites:
                        if (Recent.count(this, false) > 0) {
                            frag = new RecentFragment();
                            Bundle b = new Bundle();
                            b.putBoolean(RecentFragment.ARG_RECENT, false);
                            frag.setArguments(b);
                        } else {
                            Snackbar.make(drawer, "No favorites found", Snackbar.LENGTH_SHORT).show();
                            success = false;
                        }
                        break;
                    case R.id.nav_select_comic:
                        if (!StorageHelper.isExternalStorageReadable()) {
                            Snackbar.make(drawer, "External storage not readable", Snackbar.LENGTH_SHORT).show();
                            success = false;
                        } else if (!hasStoragePermission()) {
                            askStoragePermission();
                            success = false;
                        } else {
                            frag = new FileChooserFragment();
                        }
                        break;
                    default:
                        Snackbar.make(drawer, "Not implemented yet", Snackbar.LENGTH_SHORT).show();
                        success = false;
                }

                if (frag != null) {
                    FragmentTransaction ft = fm.beginTransaction();
                    ft.replace(R.id.container, frag);
                    ft.addToBackStack(null);
                    ft.commit();
                }
            }
        }

        if (success){
            if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            }
        }

        return success;
    }

    public void fileChooserCallback(String filePath, int page){

        selectNavItem(R.id.nav_continue_reading, filePath, page);
    }

    @Override
    public void onBackPressed() {

        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        }else{
            FragmentManager fm = this.getFragmentManager();
            Fragment frag = fm.findFragmentById(R.id.container);
            boolean isFcf = frag instanceof FileChooserFragment;

            if (isFcf){
                FileChooserFragment fca = ((FileChooserFragment) frag);
                if (fca.goToPath(null, true)){
                    return;
                }
            }
            if (fm.getBackStackEntryCount() == 1) {
                if (isFcf){
                    if (drawer != null){
                        drawer.openDrawer(GravityCompat.START);
                    }else {
                        super.onBackPressed();
                    }
                }else {
                    fm.popBackStackImmediate();
                    selectNavItem(R.id.nav_select_comic);
                }
            } else {
                fm.popBackStackImmediate();
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermission(String permission){
        requestPermissions(new String[]{permission}, 0);
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
            }else if (ArchiveParser.isSupportedArchive(f)){
                fileChooserCallback(filePath, 0);
            }else if (ImageParser.isSupportedDirectory(f)){
                fileChooserCallback(filePath, 0);
            }else{
                Toast.makeText(this, R.string.str_unsupported_file_type_or_unreadable_directory, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void openSettingsPageStoragePermission(){
        Toast.makeText(this, R.string.you_must_give_jcomic_storage_permissions, Toast.LENGTH_LONG).show();
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    private boolean hasStoragePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        return ContextCompat.checkSelfPermission(this, perm) == PackageManager.PERMISSION_GRANTED;
    }

    private void askStoragePermission(){
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return;
        String perm = Manifest.permission.READ_EXTERNAL_STORAGE;
        ActivityCompat.requestPermissions(this, new String[]{perm}, 0);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean notGranted = grantResults.length == 0 || grantResults[0] != PackageManager.PERMISSION_GRANTED;
        if (notGranted){
            openSettingsPageStoragePermission();
        }else{
            selectNavItem(R.id.nav_select_comic);
        }
    }

}
