package com.projects.johnny.myway;


import android.app.Fragment;

public class DirectionsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DirectionsFragment();
    }
}
