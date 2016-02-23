package com.projects.johnny.myway;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class DirectionsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DirectionsFragment();
    }
}
