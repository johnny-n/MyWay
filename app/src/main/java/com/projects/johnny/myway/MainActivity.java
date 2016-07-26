package com.projects.johnny.myway;


import android.app.Fragment;

public class MainActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DirectionsFragment();
    }
}
