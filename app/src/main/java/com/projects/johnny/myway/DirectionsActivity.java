package com.projects.johnny.myway;

import android.support.v4.app.Fragment;
import android.os.Bundle;

public class DirectionsActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return new DirectionsFragment();
    }

}
