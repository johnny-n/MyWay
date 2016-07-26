package com.projects.johnny.myway;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;

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

        // Automatically sign in if session UID not null
        // This is to automatically log in users right after they sign up for an account.
        App app = (App) getApplicationContext();
        if (app.getUID() != null) {
            Intent intent = new Intent(this, DirectionsActivity.class);
        }
    }
}
