package com.projects.johnny.myway;

import android.animation.Animator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

import io.codetail.animation.ViewAnimationUtils;
import io.codetail.widget.RevealFrameLayout;

public class AddLocationFragment extends Fragment {

    private static final String TAG = "AddLocationFragment";

    public static AddLocationFragment newInstance() {
        return new AddLocationFragment();
    }

    private String locationAddress = "";
    private EditText mNicknameEditText;

    private RevealFrameLayout mContainer;
    private FrameLayout mCircularRevealView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_location, container, false);

        mNicknameEditText = (EditText) v.findViewById(R.id.add_location_nickname_edit_text);

        // Firebase
        App app = (App) getActivity().getApplicationContext();
        String UID = app.getUID();
        Firebase.setAndroidContext(getActivity().getApplicationContext());
        final Firebase mFirebaseRef = new Firebase("https://myways.firebaseIO.com/").child(UID).child("Locations");

        // Get reference to button and have it initially disabled
        final Button mConfirmAddPlaceButton = (Button) v.findViewById(R.id.confirm_add_place_button);
        mConfirmAddPlaceButton.setEnabled(false);

        // Because we are inside a fragment and we want to obtain a fragment,
        // we have to use the childFragmentManager.
        final FragmentManager fm = getChildFragmentManager();

        // Get reference to PlaceAutocomplete UI widget
        final PlaceAutocompleteFragment mPlaceAutocompleteFragment = (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_fragment);

        // Set listener for autocomplete widget
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {

            @Override
            public void onPlaceSelected(Place place) {
                // Obtain address from place selected
                String locationAddress = place.getAddress().toString();

                Log.d(TAG, "OnPlaceSelected " + locationAddress);

                mPlaceAutocompleteFragment.setText(locationAddress);
                // Enable button after selecting a place
                mConfirmAddPlaceButton.setEnabled(true);
            }

            @Override
            public void onError(Status status) {
                Log.i(TAG, "An error occurred: " + status);
            }
        });

        // Set listener for button
        mConfirmAddPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add location to storage with or without nickname, depending
                //  on what the user wants.
                String nickname;
                if (mNicknameEditText.getText().length() > 0) {
                    nickname = mNicknameEditText.getText().toString();
                    mFirebaseRef.child(nickname).setValue(locationAddress);
                } else {
                    nickname = locationAddress;
                    mFirebaseRef.child(nickname).setValue(locationAddress);
                }

                // Return to previous fragment
                Fragment fragment = DirectionsFragment.newInstance();
                fm.beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .commit();
            }
        });

        mPlaceAutocompleteFragment.setHint("Search Places");

        // For UP navigation button
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Animation stuff
        mContainer = (RevealFrameLayout) v.findViewById(R.id.add_location_container);
        mCircularRevealView = (FrameLayout) v.findViewById(R.id.add_location_circular_reveal_view);

        // Animate circular reveal
        mContainer.post(new Runnable() {
            @Override
            public void run() {
                // Determine center position for circular reveal (fab button)
                final int x = mContainer.getRight() / 2;
                final int y = mContainer.getBottom() / 2;

                // Determine radius sizes
                final int containerWidth = mContainer.getWidth();
                final int containerHeight = mContainer.getHeight();

                final float startingRadius = (float) Math.sqrt((containerWidth * containerWidth) + (containerHeight * containerHeight));;
                final float endRadius = 0;

                final Animator animator = ViewAnimationUtils.createCircularReveal(mCircularRevealView, x, y, startingRadius, endRadius);
                animator.setDuration(400);
                animator.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mCircularRevealView.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });

                animator.start();
            }
        });

        return v;
    }
}
