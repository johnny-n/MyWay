package com.projects.johnny.myway;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Johnny on 2/11/16.
 */
public class SignUpFragment extends Fragment {

    private EditText mFirstName;
    private EditText mLastName;
    private EditText mUsername;
    private TextView mUsernameRetryText;
    private EditText mPassword;
    private EditText mPasswordConfirm;
    private TextView mPasswordRetryText;
    private Button mCompleteButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_sign_up, container, false);

        // Set up Firebase database
        Firebase.setAndroidContext(getActivity());
        final Firebase mFirebaseRef = new Firebase("https://myways.firebaseIO.com/");


        // Username in this case would be the email
        mFirstName = (EditText) v.findViewById(R.id.sign_up_first_name_edit_text);
        mLastName = (EditText) v.findViewById(R.id.sign_up_last_name_edit_text);

        mUsername = (EditText) v.findViewById(R.id.username_create_edit_text);
        mUsernameRetryText = (TextView) v.findViewById(R.id.username_retry_text_view);
        mUsernameRetryText.setVisibility(View.GONE);
        mUsername.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (isValidUsername()) {
                        mUsernameRetryText.setVisibility(View.GONE);
                    } else {
                        mUsernameRetryText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mPassword = (EditText) v.findViewById(R.id.password_create_edit_text);
        mPasswordConfirm = (EditText) v.findViewById(R.id.password_confirm_edit_text);
        mPasswordRetryText = (TextView) v.findViewById(R.id.password_retry_text_view);
        mPasswordRetryText.setVisibility(View.GONE);
        // Resets password confirm entry field
        mPassword.addTextChangedListener(passwordTextWatcher());
        // Checks if both password fields match, alert user if they do not match
        mPasswordConfirm.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (passwordsMatch()) {
                        mPasswordRetryText.setVisibility(View.GONE);
                    } else {
                        mPasswordRetryText.setVisibility(View.VISIBLE);
                    }
                }
            }
        });

        mCompleteButton = (Button) v.findViewById(R.id.sign_up_complete_button);
        mCompleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isValidForm()) {
                    String username = mUsername.getText().toString();
                    String password = mPassword.getText().toString();
                    mFirebaseRef.createUser(username, password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                        @Override
                        public void onSuccess(Map<String, Object> result) {
                            Toast.makeText(getContext(), "Successfully created user account!", Toast.LENGTH_SHORT).show();

                            String UID = (String) result.get("uid");
                            mFirebaseRef.setValue(UID);
                            Firebase mFirebaseRefChild = mFirebaseRef.child(UID);
                            mFirebaseRefChild = mFirebaseRefChild.child("Name");
                            mFirebaseRefChild.child("FirstName").setValue(mFirstName.getText().toString());
                            mFirebaseRefChild.child("LastName").setValue(mLastName.getText().toString());

                            Intent intent = new Intent(getActivity(), MainActivity.class);
                            startActivity(intent);
                        }
                        @Override
                        public void onError(FirebaseError firebaseError) {
                            Toast.makeText(getContext(), firebaseError.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        return v;
    }

    // Checks whether the sign up form is correctly filled out
    private boolean isValidForm() {
        boolean first = mFirstName.getText().toString().length() > 0;
        boolean last = mLastName.getText().toString().length() > 0;
        return first && last && isValidUsername() && passwordsMatch();
    }

    private boolean passwordsMatch() {
        String password = mPassword.getText().toString();
        String passwordConfirm = mPasswordConfirm.getText().toString();
        return password.equals(passwordConfirm);
    }

    private boolean isValidUsername() {
        CharSequence email = mUsername.getText().toString();
        Pattern pattern = Patterns.EMAIL_ADDRESS;
        return pattern.matcher(email).matches();
    }

    private View.OnClickListener completeButtonListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        };
    }

    private TextWatcher passwordTextWatcher() {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mPasswordConfirm.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        };
    }
}
