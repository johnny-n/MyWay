package com.projects.johnny.myway;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;

/**
 * Created by Johnny on 2/9/16.
 */
public class AddLocationActivity extends AppCompatActivity {

    private final boolean BUTTON_ENABLED = true;
    private final boolean BUTTON_DISABLED = false;

    private MyLocationStorage mLocationStorage;
    private String locationAddress = "";
    private EditText mNicknameEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);
        mLocationStorage = MyLocationStorage.get(this);

        mNicknameEditText = (EditText) findViewById(R.id.add_location_nickname_edit_text);

        // Get reference to button and have it initially disabled
        final Button mConfirmAddPlaceButton = (Button) findViewById(R.id.confirm_add_place_button);
        mConfirmAddPlaceButton.setEnabled(BUTTON_DISABLED);

        // Obtain FragmentManager
        android.app.FragmentManager fm = getFragmentManager();
        // Get reference to PlaceAutocomplete UI widget
        final PlaceAutocompleteFragment mPlaceAutocompleteFragment =
                (PlaceAutocompleteFragment) fm.findFragmentById(R.id.place_autocomplete_fragment);
        // Set listener for autocomplete widget
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // Obtain address from place selected
                locationAddress = place.getAddress().toString();
                // Enable button after selecting a place
                mConfirmAddPlaceButton.setEnabled(BUTTON_ENABLED);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i("Unable to find location", "An error occurred: " + status);
            }
        });

        // Set listener for button
        mConfirmAddPlaceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Add location to storage with or without nickname, depending
                //  on what the user wants.
                if (mNicknameEditText.getText().length() > 0) {
                    String nickname = mNicknameEditText.getText().toString();
                    mLocationStorage.addLocation(new MyLocation(nickname, locationAddress));
                } else {
                    mLocationStorage.addLocation(new MyLocation(locationAddress));
                }
                Intent intent = new Intent(getApplicationContext(), DirectionsActivity.class);
                startActivity(intent);
            }
        });

        mPlaceAutocompleteFragment.setHint("Search Places");
    }
}
