package com.projects.johnny.myway;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.regex.Pattern;

// TODO: Merge SignInFragment and SignUpFragment into one Activity

public class SignInFragment extends Fragment {

    private EditText mUsername;
    private EditText mPassword;
    private TextView mUsernameRetry;
    private TextView mSignUpButton;
    private Button mSignInButton;

    public static final String USER_UID_KEY = "USER_UID_KEY";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Get reference to Firebase
        // TODO: Move Firebase to parent activity
        Firebase.setAndroidContext(getActivity());
        final Firebase mFirebaseRef = new Firebase("https://myways.firebaseIO.com/");

        mUsername = (EditText) v.findViewById(R.id.sign_in_username_edit_text);
        mPassword = (EditText) v.findViewById(R.id.sign_in_password_edit_text);
        mUsernameRetry = (TextView) v.findViewById(R.id.username_retry_text_view);
        mSignUpButton = (Button) v.findViewById(R.id.sign_up_button);
        mSignInButton = (Button) v.findViewById(R.id.sign_in_button);

        mUsernameRetry.setVisibility(View.GONE);

        mUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String username = mUsername.getText().toString();
                    if (isValidUsername(username)) {
                        mUsernameRetry.setVisibility(View.GONE);
                    } else {
                        mUsernameRetry.setVisibility(View.VISIBLE);
                    }
                }
            }
        });
        mSignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Automatically log user in after creating account
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
            }
        });
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    final String email = mUsername.getText().toString();
                    final String password = mPassword.getText().toString();


                    mFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            // Use App class for session storage of UID
                            String UID = authData.getUid();
                            App.Companion.setUID(UID);
                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                            getActivity().finish();
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to log in...", Toast.LENGTH_SHORT).show();
                            System.out.println(firebaseError.getMessage());
                        }
                    });
                }
            }
        });

        return v;
    }

    // Returns true/false depending on whether locations is turned on or off.
    private static boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }

            return locationMode != Settings.Secure.LOCATION_MODE_OFF;

        } else{
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    private boolean isValidUsername(CharSequence username) {
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(username).matches();
    }

    private boolean isValid() {
        String username = mUsername.getText().toString();
        String password = mPassword.getText().toString();
        if (username.length() > 0 && password.length() > 0) {
            return true;
        }
        return false;
    }
}
