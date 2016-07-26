package com.projects.johnny.myway;

import android.Manifest;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Johnny on 2/3/16.
 */
public abstract class SingleFragmentActivity extends AppCompatActivity {

    // Can only use lower 8-bits for LOCATION_REQUEST_CODE
    private static final int LOCATION_REQUEST_CODE = 2;


    protected abstract Fragment createFragment();

    /**
     * This onCreate() method will be the onCreate() method used for all classes
     * that extends SingleFragmentActivity.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_container);

        FragmentManager fm = getFragmentManager();
        Fragment fragment = fm.findFragmentById(R.id.fragment_container);


        // Always good to use this check because sometimes the †arget fragment might not exist.
        if (fragment == null) {
            fragment = createFragment();
            fm.beginTransaction().add(R.id.fragment_container, fragment).commit();
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
            } else {
                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this, new String[] {  android.Manifest.permission.ACCESS_COARSE_LOCATION }, LOCATION_REQUEST_CODE );
                Log.i("Location Check", "Completed");

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.

            }
        }
    }
}
