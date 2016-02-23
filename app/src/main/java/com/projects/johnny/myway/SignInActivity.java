package com.projects.johnny.myway;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by Johnny on 2/12/16.
 */
public class SignInActivity extends SingleFragmentActivity {
    @Override
    protected Fragment createFragment() {
        return new SignInFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
