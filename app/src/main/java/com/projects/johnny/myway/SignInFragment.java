package com.projects.johnny.myway;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.regex.Pattern;

public class SignInFragment extends Fragment {

    private EditText mUsername;
    private EditText mPassword;
    private TextView mUsernameRetry;
    private TextView mSignUpAccountTextView;
    private Button mSignInButton;

    public static final String USER_UID_KEY = "USER_UID_KEY";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_in, container, false);

        // Get reference to Firebase
        Firebase.setAndroidContext(getActivity());
        final Firebase mFirebaseRef = new Firebase("https://myways.firebaseIO.com/");

        mUsername = (EditText) v.findViewById(R.id.sign_in_username_edit_text);
        mPassword = (EditText) v.findViewById(R.id.sign_in_password_edit_text);
        mUsernameRetry = (TextView) v.findViewById(R.id.username_retry_text_view);
        mSignUpAccountTextView = (TextView) v.findViewById(R.id.sign_up_for_account_text_view);
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
        mSignUpAccountTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Complete sign up link (use startActivityForResult())
                Intent intent = new Intent(getActivity(), SignUpActivity.class);
                startActivity(intent);
            }
        });
        mSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValid()) {
                    String email = mUsername.getText().toString();
                    String password = mPassword.getText().toString();
                    mFirebaseRef.authWithPassword(email, password, new Firebase.AuthResultHandler() {
                        @Override
                        public void onAuthenticated(AuthData authData) {
                            // Use App class for session storage of UID
                            String UID = authData.getUid();
                            App app = (App) getActivity().getApplicationContext();
                            app.setUID(UID);
                            Intent intent = new Intent(getActivity(), DirectionsActivity.class);
                            startActivity(intent);
                        }

                        @Override
                        public void onAuthenticationError(FirebaseError firebaseError) {
                            Toast.makeText(getContext(), "Failed to log in...", Toast.LENGTH_SHORT).show();
                            System.out.println(firebaseError.getMessage());
                        }
                    });
                }
            }
        });

        return v;
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
